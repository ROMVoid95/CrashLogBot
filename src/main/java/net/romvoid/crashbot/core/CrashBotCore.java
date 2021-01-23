package net.romvoid.crashbot.core;

import static net.romvoid.crashbot.codes.ShutdownCodes.SHARD_FETCH_FAILURE;
import static net.romvoid.crashbot.core.BotState.*;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import java.lang.annotation.Annotation;
//import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;

import lombok.var;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.romvoid.crashbot.CrashBot;
import net.romvoid.crashbot.XtraRuntimeOpts;
import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.core.command.processor.CommandProcessor;
import net.romvoid.crashbot.core.listeners.CommandListener;
import net.romvoid.crashbot.core.listeners.CrashBotListener;
import net.romvoid.crashbot.core.modules.Module;
import net.romvoid.crashbot.core.opts.InteractiveOpts;
import net.romvoid.crashbot.core.opts.core.InteractiveOpt;
import net.romvoid.crashbot.core.shard.SessionControllerWrapper;
import net.romvoid.crashbot.core.shard.Shard;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.logging.LogUtils;
import net.romvoid.crashbot.utilities.Commons;
import okhttp3.Request;

public class CrashBotCore {
    private static final Logger log = LoggerFactory.getLogger(CrashBotCore.class);
    
    private static BotState loadState = PRELOAD;
    private final Map<Integer, Shard> shards = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("CrashBot Thread-%d").build()
    );
    private final MainConfig config;
    private final boolean isDebug;
    private String commandsPackage;
    private final CommandProcessor commandProcessor = new CommandProcessor();
    private EventBus shardEventBus;
    private ShardManager shardManager;
    
    public CrashBotCore(MainConfig config, boolean isDebug) {
    	this.config = config;
    	this.isDebug = isDebug;
    }
    
    public static boolean hasLoadedCompletely() {
        return getLoadState().equals(POSTLOAD);
    }

    public static BotState getLoadState() {
        return loadState;
    }
    
    private static int getInstanceShards(String token) {
        if (XtraRuntimeOpts.SHARD_SUBSET) {
            return XtraRuntimeOpts.TO_SHARD.orElseThrow() - XtraRuntimeOpts.FROM_SHARD.orElseThrow();
        }

        if (XtraRuntimeOpts.SHARD_COUNT.isPresent()) {
            return XtraRuntimeOpts.SHARD_COUNT.getAsInt();
        }

        try {
            var shards = new Request.Builder()
                    .url("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + token)
                    .header("Content-Type", "application/json")
                    .build();

            try (var response = Commons.httpClient.newCall(shards).execute()) {
                var body = response.body();

                if (body == null) {
                    throw new IllegalStateException("Error requesting shard count: " + response.code() + " " + response.message());
                }

                var shardObject = new JSONObject(body.string());
                return shardObject.getInt("shards");
            }

        } catch (Exception e) {
            log.error("Unable to fetch shard count", e);
            System.exit(SHARD_FETCH_FAILURE);
        }
        return 1;
    }
    
    private void startShardedInstance() {
        loadState = LOADING;

        SessionController controller;
        if (isDebug) {
            // Bucketed controller still prioritizes home guild and reconnecting shards.
            // Only really useful in the node that actually contains the guild, but worth keeping.
            controller = new SessionControllerWrapper(1, 213468583252983809L);
        } else {
            var bucketFactor = config.getBucketFactor();
            if (bucketFactor > 1) {
                log.info("Using buckets of {} shards to start the bot! Assuming we're on big bot sharding." , bucketFactor);
                log.info("If you're self-hosting, set bucketFactor in config.json to 1 and isSelfHost to true.");
            }

            controller = new SessionControllerWrapper(bucketFactor, 213468583252983809L);
        }

        var gatewayThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("GatewayThread-%d")
                .setDaemon(true)
                .setPriority(Thread.MAX_PRIORITY)
                .build();
        var requesterThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("RequesterThread-%d")
                .setDaemon(true)
                .build();

        try {
            // Don't allow mentioning @everyone, @here or @role (can be overriden in a per-command context, but we only ever re-enable role)
            var deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE);
            MessageAction.setDefaultMentions(EnumSet.complementOf(deny));

            // Gateway Intents to enable.
            // We used to have GUILD_PRESENCES here for caching before, since chunking wasn't possible, but we needed to remove it.
            // So we have no permanent cache anymore.
            GatewayIntent[] toEnable = {
                    GatewayIntent.GUILD_MESSAGES, // Recieve guild messages, needed to, well operate at all.
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,  // Receive message reactions, used for reaction menus.
                    GatewayIntent.GUILD_MEMBERS, // Receive member events, needed for mod features *and* welcome/leave messages.
                    GatewayIntent.GUILD_VOICE_STATES, // Receive voice states, needed so Member#getVoiceState doesn't return null.
            };

            log.info("Using intents {}", Arrays.stream(toEnable)
                    .map(Enum::name)
                    .collect(Collectors.joining(", "))
            );

            // This is used so we can fire PostLoadEvent properly.
            var shardStartListener = new ShardStartListener();

            var shardManager = DefaultShardManagerBuilder.create(config.token, Arrays.asList(toEnable))
                    // Can't do chunking with Gateway Intents enabled, fun, but don't need it anymore.
                    .setChunkingFilter(ChunkingFilter.NONE)
                    .setSessionController(controller)
                    .addEventListeners(
                            InteractiveOpts.listener(),
                            shardStartListener
                    )
                    .addEventListenerProviders(List.of(
                            id -> new CommandListener(commandProcessor, threadPool, getShard(id).getMessageCache()),
                            id -> new CrashBotListener(threadPool, getShard(id).getMessageCache()),
                            id -> getShard(id).getListener()
                    ))
                    .setEventManagerProvider(id -> getShard(id).getManager())
                    // Don't spam on mass-prune.
                    .setBulkDeleteSplittingEnabled(false)
                    // We technically don't need it, as we don't ask for either GUILD_PRESENCES nor GUILD_EMOJIS anymore.
                    .disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS))
                    .setActivity(Activity.playing("Hold on to your seatbelts!"));
            
            /* only create eviction strategies that will get used */
            List<Integer> shardIds;
            int latchCount;

            if (isDebug) {
                var shardCount = 2;
                shardIds = List.of(0, 1);
                latchCount = shardCount;
                shardManager.setShardsTotal(shardCount);
                // TODO: test if this works
                //.setGatewayPool(Executors.newSingleThreadScheduledExecutor(gatewayThreadFactory), true)
                //.setRateLimitPool(Executors.newScheduledThreadPool(2, requesterThreadFactory), true);
                log.info("Debug instance, using {} shards", shardCount);
            } else {
                int shardCount;
                // Count specified in config.
                if (config.totalShards != 0) {
                    shardCount = config.totalShards;
                    shardManager.setShardsTotal(config.totalShards);
                    log.info("Using {} shards from config (totalShards != 0)", shardCount);
                } else {
                    //Count specified on runtime options or recommended count by Discord.
                    shardCount = XtraRuntimeOpts.SHARD_COUNT.orElseGet(() -> getInstanceShards(config.token));
                    shardManager.setShardsTotal(shardCount);
                    if (XtraRuntimeOpts.SHARD_COUNT.isPresent()) {
                        log.info("Using {} shards from ExtraRuntimeOptions", shardCount);
                    } else {
                        log.info("Using {} shards from discord recommended amount", shardCount);
                    }
                }

                // Using a shard subset. FROM_SHARD is inclusive, TO_SHARD is exclusive (else 0 to 448 would start 449 shards)
                if (XtraRuntimeOpts.SHARD_SUBSET) {
                    if (XtraRuntimeOpts.SHARD_SUBSET_MISSING) {
                        throw new IllegalStateException("Both mantaro.from-shard and mantaro.to-shard must be specified " +
                                "when using shard subsets. Please specify the missing one.");
                    }

                    var from = XtraRuntimeOpts.FROM_SHARD.orElseThrow();
                    var to = XtraRuntimeOpts.TO_SHARD.orElseThrow() - 1;
                    shardIds = IntStream.rangeClosed(from, to).boxed().collect(Collectors.toList());
                    latchCount = to - from + 1;

                    log.info("Using shard range {}-{}", from, to);
                    shardManager.setShards(from, to);
                } else {
                    shardIds = IntStream.range(0, shardCount).boxed().collect(Collectors.toList());
                    latchCount = shardCount;
                }
    
                // We need to use latchCount instead of shardCount
                // latchCount is the number of shards on this process
                // shardCount is the total number of shards in all processes
                // TODO: test if this works?
                /*
                var gatewayThreads = Math.max(1, latchCount / 16);
                var rateLimitThreads = Math.max(2, latchCount * 5 / 4);
                log.info("Gateway pool: {} threads", gatewayThreads);
                log.info("Rate limit pool: {} threads", rateLimitThreads);
                shardManager.setGatewayPool(Executors.newScheduledThreadPool(gatewayThreads, gatewayThreadFactory), true)
                        .setRateLimitPool(Executors.newScheduledThreadPool(rateLimitThreads, requesterThreadFactory), true);
                */
            }

            // If this isn't true we have a big problem
            if (shardIds.size() != latchCount) {
                throw new IllegalStateException("Shard ids list must have the same size as latch count");
            }
    
            CrashBotCore.setLoadState(BotState.LOADING_SHARDS);

            log.info("Spawning {} shards...", latchCount);
            var start = System.currentTimeMillis();
            shardStartListener.setLatch(new CountDownLatch(latchCount));
            this.shardManager = shardManager.build();

            //This is so it doesn't block command registering, lol.
            threadPool.submit(() -> {
                log.info("CountdownLatch started: Awaiting for {} shards to be counted down to start PostLoad.", latchCount);

                try {
                    shardStartListener.latch.await();
                    var elapsed = System.currentTimeMillis() - start;

                    log.info("All shards logged in! Took {} seconds", TimeUnit.MILLISECONDS.toSeconds(elapsed));
                    this.shardManager.removeEventListener(shardStartListener);
                    startPostLoadProcedure(elapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (LoginException e) {
            throw new IllegalStateException(e);
        }

        loadState = LOADED;
    }

    public static void setLoadState(BotState loadState) {
    	CrashBotCore.loadState = loadState;
    }
    
    public CrashBotCore setCommandsPackage(String commandsPackage) {
        this.commandsPackage = commandsPackage;
        return this;
    }
    
    public void markAsReady() {
        loadState = POSTLOAD;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Shard getShard(int id) {
        return shards.computeIfAbsent(id, Shard::new);
    }

    public Collection<Shard> getShards() {
        return Collections.unmodifiableCollection(shards.values());
    }
    
    private Set<Class<?>> lookForAnnotatedOn(String packageName, Class<? extends Annotation> annotation) {
        return new ClassGraph()
                .acceptPackages(packageName)
                .enableAnnotationInfo()
                .scan(2)
                .getAllClasses().stream().filter(classInfo -> classInfo.hasAnnotation(annotation.getName())).map(ClassInfo::loadClass)
                .collect(Collectors.toSet());
    }
    
    public EventBus getShardEventBus() {
        return this.shardEventBus;
    }
    
    private void startPostLoadProcedure(long elapsed) {
        var bot = CrashBot.getInstance();

        // Start the reconnect queue.
        bot.getCore().markAsReady();

        // Get the amount of clusters
        int clusterTotal = 1;
        try(var jedis = CrashBotData.getDefaultJedisPool().getResource()) {
            var clusters = jedis.hgetAll("node-stats-" + config.getClientId());
            clusterTotal = clusters.size();
        }

        log.info("Not aware of anything holding off boot now, considering bot as started up");
        LogUtils.shard(
                """
                Loaded all %d shards and %d commands.
                Took %s to start this node (%d). Total nodes: %d.
                Cross-node shard count is %d.""".formatted(
                        shardManager.getShardsRunning(), CommandProcessor.REGISTRY.commands().size(),
                        Commons.formatDuration(elapsed), bot.getNodeNumber(), clusterTotal,
                        shardManager.getShardsTotal()
                )
        );

        log.info("Loaded all shards successfully! Current status: {}", CrashBotCore.getLoadState());
    }
    
    public void start() {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null!");
        }

        if (commandsPackage == null) {
            throw new IllegalArgumentException("Cannot look for commands if you don't specify where!");
        }

        var commands = lookForAnnotatedOn(commandsPackage, Module.class);

        shardEventBus = new EventBus();

        // Start the actual bot now.
        startShardedInstance();

        for (var commandClass : commands) {
            try {
                shardEventBus.register(commandClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("Invalid module: no zero arg public constructor found for " + commandClass);
            }
        }

        new Thread(() -> {

            log.info("Registering all commands (@Module)");
            shardEventBus.post(CommandProcessor.REGISTRY);
            log.info("Registered all commands (@Module)");

        }, "Mantaro EventBus-Post").start();
    }
    
    private static class ShardStartListener implements EventListener {
        private CountDownLatch latch;

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onEvent(@Nonnull GenericEvent event) {
            if (event instanceof ReadyEvent) {
                var sm = event.getJDA().getShardManager();
                if (sm == null) { // We have a big problem if this happens.
                    throw new AssertionError();
                }

                latch.countDown();
            }
        }
    }
}
