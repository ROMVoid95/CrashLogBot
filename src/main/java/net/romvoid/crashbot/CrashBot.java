package net.romvoid.crashbot;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.core.CrashBotCore;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.logging.LogFilter;
import net.romvoid.crashbot.logging.LogUtils;

import static net.romvoid.crashbot.codes.ShutdownCodes.FATAL_FAILURE;

public class CrashBot {
    private static final Logger log = LoggerFactory.getLogger(CrashBot.class);
    private static CrashBot instance;

    static {
        log.info("Starting up Mantaro {}, Git revision: {}", CrashBotInfo.VERSION, CrashBotInfo.GIT_REVISION);
        log.info("Reporting UA {} for HTTP requests.", CrashBotInfo.USER_AGENT);

        RestAction.setPassContext(true);
        if (XtraRuntimeOpts.DEBUG) {
            log.info("Running in debug mode!");
        } else {
            RestAction.setDefaultFailure(ErrorResponseException.ignore(
                    RestAction.getDefaultFailure(),
                    ErrorResponse.UNKNOWN_MESSAGE
            ));
        }

        log.info("Filtering all logs below {}", LogFilter.LEVEL);
    }
    
    private final CrashBotCore core;
    private final MainConfig config = CrashBotData.config().get();
    
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
            3, new ThreadFactoryBuilder().setNameFormat("CrashBot Scheduled Executor Thread-%d").build()
    );
    
    private CrashBot() throws Exception {
    	if(XtraRuntimeOpts.PRINT_VARIABLES || XtraRuntimeOpts.DEBUG) {
    		 printStartVariables();
    	}
    	
    	instance = this;
    	
    	core = new CrashBotCore(config, XtraRuntimeOpts.DEBUG);
        LogUtils.log("Startup",
                "Starting up Mantaro %s (Git: %s) in Node %s\nHold your seatbelts! <3"
                        .formatted(CrashBotInfo.VERSION, CrashBotInfo.GIT_REVISION, getNodeNumber())
        );
        
        core.setCommandsPackage("net.kodehawa.mantarobot.commands").start();

        log.info("Finished loading basic components. Current status: {}", CrashBotCore.getLoadState());
        CrashBotData.config().save();
        this.startExecutors();
    }
    
    public static void main(String[] args) {
        // Attempt to start the bot process itself.
        try {
            new CrashBot();
        } catch (Exception e) {
            log.error("Could not complete Main Thread routine!", e);
            log.error("Cannot continue! Exiting program...");
            System.exit(FATAL_FAILURE);
        }
    }
    
    public int getNodeNumber() {
        return XtraRuntimeOpts.NODE_NUMBER.orElse(0);
    }

	public static boolean isDebug() {
        return XtraRuntimeOpts.DEBUG;
    }

    public static boolean isVerbose() {
        return XtraRuntimeOpts.VERBOSE;
    }

    public static CrashBot getInstance() {
        return CrashBot.instance;
    }
    
    public CrashBotCore getCore() {
        return this.core;
    }

    public ShardManager getShardManager() {
        return core.getShardManager();
    }

    public JDA getShard(int id) {
        return getShardManager().getShardById(id);
    }

    public void restartShard(int shardId) {
        getShardManager().restart(shardId);
    }

    public JDA getShardGuild(String guildId) {
        return getShardGuild(MiscUtil.parseSnowflake(guildId));
    }

    public JDA getShardGuild(long guildId) {
        return getShardManager().getShardById(
                (int) ((guildId >> 22) % getShardManager().getShardsTotal())
        );
    }

    public int getShardIdForGuild(long guildId) {
        return (int) ((guildId >> 22) % getShardManager().getShardsTotal());
    }

    // You would ask, doesn't ShardManager#getShardsTotal do that? Absolutely not. It's screwed. Fucked. I dunno why.
    // DefaultShardManager overrides it, nvm, ouch.
    public int getManagedShards() {
        return getShardManager().getShardsRunning() + getShardManager().getShardsQueued();
    }
    
    public boolean isMasterNode() {
        if (XtraRuntimeOpts.SHARD_SUBSET && XtraRuntimeOpts.FROM_SHARD.isPresent()) {
            return XtraRuntimeOpts.FROM_SHARD.getAsInt() == 0;
        }

        return true;
    }

    public String getShardSlice() {
        if (XtraRuntimeOpts.SHARD_SUBSET) {
            //noinspection OptionalGetWithoutIsPresent
            return XtraRuntimeOpts.FROM_SHARD.getAsInt() + " to " + XtraRuntimeOpts.TO_SHARD.getAsInt();
        } else {
            return "0 to " + getShardManager().getShardsTotal();
        }
    }

    public ScheduledExecutorService getExecutorService() {
        return this.executorService;
    }

    public List<JDA> getShardList() {
        return IntStream.range(0, getManagedShards())
                .mapToObj(this::getShard)
                .collect(Collectors.toList());
    }
    
    private void startExecutors() {
        log.info("Starting executors...");
    }
    
    // This will print if the MANTARO_PRINT_VARIABLES env variable is present.
    private void printStartVariables() {
        log.info("""
                Environment variables set on this startup:
                VERBOSE_SHARD_LOGS = {}
                DEBUG = {}
                DEBUG_LOGS = {}
                LOG_DB_ACCESS = {}
                TRACE_LOGS = {}
                VERBOSE = {}
                VERBOSE_SHARD_LOGS = {}
                FROM_SHARD = {}
                TO_SHARD = {}
                SHARD_COUNT = {}
                NODE_NUMBER = {}""",
                XtraRuntimeOpts.VERBOSE_SHARD_LOGS,
                XtraRuntimeOpts.DEBUG,
                XtraRuntimeOpts.DEBUG_LOGS,
                XtraRuntimeOpts.LOG_DB_ACCESS,
                XtraRuntimeOpts.TRACE_LOGS,
                XtraRuntimeOpts.VERBOSE,
                XtraRuntimeOpts.VERBOSE_SHARD_LOGS,
                XtraRuntimeOpts.FROM_SHARD,
                XtraRuntimeOpts.TO_SHARD,
                XtraRuntimeOpts.SHARD_COUNT,
                XtraRuntimeOpts.NODE_NUMBER
        );
    }
}
