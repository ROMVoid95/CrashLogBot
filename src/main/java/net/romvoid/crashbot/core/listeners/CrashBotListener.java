package net.romvoid.crashbot.core.listeners;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.romvoid.crashbot.CrashBot;
import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.core.EventManager;
import net.romvoid.crashbot.core.listeners.entities.CachedMessage;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.db.ManagedDatabase;

public class CrashBotListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CrashBotListener.class);
    private static final MainConfig CONFIG = CrashBotData.config().get();
    private static final ManagedDatabase DATABASE = CrashBotData.db();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern MODIFIER_PATTERN = Pattern.compile("\\b\\p{L}*:\\b");
    // Channels we could send the greet message to.
    private static final List<String> CHANNEL_NAMES = List.of("general", "general-chat", "chat", "lounge", "main-chat", "main");
    
    private final ExecutorService threadPool;
    private final Cache<Long, Optional<CachedMessage>> messageCache;
    private final CrashBot bot;
	
    public CrashBotListener(ExecutorService threadPool, Cache<Long, Optional<CachedMessage>> messageCache) {
        this.threadPool = threadPool;
        this.messageCache = messageCache;
        bot = CrashBot.getInstance();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            threadPool.execute(() -> this.updateStats(event.getJDA()));
            return;
        }

        if (event instanceof GuildMessageReceivedEvent) {
            return;
        }
        
        // After this point we always use this variable.
        final var shardManager = bot.getShardManager();
        if (event instanceof GuildJoinEvent) {
            var joinEvent = (GuildJoinEvent) event;
            var self = joinEvent.getGuild().getSelfMember();
            if (self.getTimeJoined().isBefore(OffsetDateTime.now().minusSeconds(30))) {
                return;
            }

//            if (CrashBotCore.hasLoadedCompletely()) {
//                Metrics.GUILD_COUNT.set(shardManager.getGuildCache().size());
//                Metrics.USER_COUNT.set(shardManager.getUserCache().size());
//            }
            return;
        }

        if (event instanceof GuildLeaveEvent) {
            onLeave((GuildLeaveEvent) event);
//            if (CrashBotCore.hasLoadedCompletely()) {
//                Metrics.GUILD_COUNT.set(shardManager.getGuildCache().size());
//                Metrics.USER_COUNT.set(shardManager.getUserCache().size());
//            }
            return;
        }
        // !! Events needed for the log feature end

        if (event instanceof DisconnectEvent) {
            //Metrics.SHARD_EVENTS.labels("disconnect").inc();
            onDisconnect((DisconnectEvent) event);
            return;
        }

        if (event instanceof ResumedEvent) {
           // Metrics.SHARD_EVENTS.labels("resume").inc();
            return;
        }

        if (event instanceof HttpRequestEvent) {
            // We've fucked up big time if we reach this
            final var httpRequestEvent = (HttpRequestEvent) event;
            if (httpRequestEvent.isRateLimit()) {
                LOG.error("!!! Reached 429 on: {}", httpRequestEvent.getRoute());
                //Metrics.HTTP_429_REQUESTS.inc();
            }
            //Metrics.HTTP_REQUESTS.inc();
        }
        // !! Internal event end
    }

//    /**
//     * Handles automatic deliver of patreon keys. Should only deliver keys when
//     * - An user was already in the guild or just joined and got the "Patreon" role assigned by the Patreon bot
//     * - The user hasn't re-joined to get the role re-assigned
//     * - The user hasn't received any keys
//     * - The user pledged, obviously
//     *
//     * @param event The event that says that a role got added, obv.
//     */
//    private void handleNewPatron(GuildMemberRoleAddEvent event) {
//        //Only in mantaro's guild...
//        if (event.getGuild().getIdLong() == 213468583252983809L) {
//            threadPool.execute(() -> {
//                var hasPatronRole = event.getMember().getRoles().stream().anyMatch(r -> r.getId().equals("290257037072531466"));
//                // No patron role to be seen here.
//                if (!hasPatronRole) {
//                    return;
//                }
//
//                // We don't need to fetch anything unless the user got a Patron role.
//                var user = event.getUser();
//                var dbUser = DATABASE.getUser(user);
//                var currentKey = DATABASE.getPremiumKey(dbUser.getData().getPremiumKey());
//
//                // Already received key.
//                if (dbUser.getData().hasReceivedFirstKey()) {
//                    return;
//                }
//
//                // They still have a valid key.
//                if (currentKey != null && currentKey.validFor() > 20) {
//                    return;
//                }
//
//                user.openPrivateChannel().queue(channel -> channel.sendMessage(
//                        EmoteReference.EYES + "Thanks you for donating, we'll deliver your premium key shortly! :heart:"
//                ).queue(message -> {
//                    message.editMessage(
//                            """
//                            %1$sYou received a premium key due to your donation to Mantaro. 
//                            If you have any doubts or questions, please contact Kodehawa#3457 or ask in the support server.
//                            
//                            Instructions: **Apply this key to yourself!**. This key is a subscription to Mantaro Premium, and will last as long as you pledge.
//                            If you want more keys (>$2 donation) or want to enable the patreon bot (>$4 donation) you need to contact Kodehawa to deliver your keys.
//                            To apply this key, run the following command in any channel where Mantaro can reply: `~>activatekey %2$s`
//                            
//                            Thanks you so much for pledging and helping to keep Mantaro alive and well :heart:
//                            You should now see a #donators channel in Mantaro Hub. Thanks again for your help!
//                            """.formatted(
//                                    EmoteReference.POPPER, PremiumKey.generatePremiumKey(user.getId(), PremiumKey.Type.USER, false).getId()
//                            )
//                    ).queue(sent -> {
//                                dbUser.getData().setHasReceivedFirstKey(true);
//                                dbUser.saveUpdating();
//                            }
//                    );
//
//                    Metrics.PATRON_COUNTER.inc();
//                    //Celebrate internally! \ o /
//                    LogUtils.log("Delivered premium key to " + user.getAsTag() + "(" + user.getId() + ")");
//                }));
//            });
//        }
//    }

    private void onDisconnect(DisconnectEvent event) {
        if (event.isClosedByServer()) {
            final var clientCloseFrame = event.getClientCloseFrame();
            if (clientCloseFrame == null) {
                LOG.warn("!! SHARD DISCONNECT [SERVER] CODE: [null close frame], disconnected with code {}",
                        event.getCloseCode());
            } else {
                LOG.warn("!! SHARD DISCONNECT [SERVER] CODE: [%d] %s%n"
                        .formatted(clientCloseFrame.getCloseCode(), event.getCloseCode()));
            }
        } else {
            final var clientCloseFrame = event.getClientCloseFrame();
            if (clientCloseFrame == null) {
                LOG.warn("!! SHARD DISCONNECT [CLIENT] CODE: [null close frame?]");
            } else {
                LOG.warn("!! SHARD DISCONNECT [CLIENT] CODE: [%d] %s%n"
                        .formatted(clientCloseFrame.getCloseCode(), clientCloseFrame.getCloseReason()));
            }
        }
    }

//    private void onJoin(GuildJoinEvent event) {
//        final var guild = event.getGuild();
//        final var jda = event.getJDA();
//        // Post bot statistics to the main API.
//        this.updateStats(jda);
//
//        try {
//            // Don't send greet message for MP. Not necessary.
//            if (!CONFIG.isPremiumBot()) {
//                final var embedBuilder = new EmbedBuilder()
//                        .setThumbnail(jda.getSelfUser().getEffectiveAvatarUrl())
//                        .setColor(Color.PINK)
//                        .setDescription("""
//                                Welcome to **Mantaro**, a fun, quirky and complete Discord bot! Thanks for adding me to your server, I highly appreciate it <3
//                                We have music, currency (money/economy), games and way more stuff you can check out!
//                                Make sure you use the `~>help` command to make yourself comfy and to get started with the bot!
//                                If you're interested in supporting Mantaro, check out our Patreon page below, it'll greatly help to improve the bot. 
//                                Check out the links below for some help resources and quick start guides.
//                                This message will only be shown once.""")
//                        .addField("Important Links",
//                        """
//                                [Support Server](https://support.mantaro.site) - The place to check if you're lost or if there's an issue with the bot.
//                                [Official Wiki](https://github.com/Mantaro/MantaroBot/wiki/) - Good place to check if you're lost.
//                                [Custom Commands](https://github.com/Mantaro/MantaroBot/wiki/Custom-Command-%22v3%22) - Great customizability for your server needs!
//                                [Currency Guide](https://github.com/Mantaro/MantaroBot/wiki/Currency-101) - A lot of fun to be had!
//                                [Configuration](https://github.com/Mantaro/MantaroBot/wiki/Configuration) -  Customizability for your server needs!
//                                [Patreon](https://patreon.com/mantaro) - Help Mantaro's development directly by donating a small amount of money each month.
//                                [Official Website](https://mantaro.site) - A cool website.""",
//                                true
//                        ).setFooter("We hope you enjoy using Mantaro! For any questions, go to our support server.");
//
//                final var dbGuild = DATABASE.getGuild(guild);
//                final var guildData = dbGuild.getData();
//                final var guildChannels = guild.getChannels();
//
//                // Find a suitable channel to greeet send the message to.
//                guildChannels.stream().filter(
//                        channel -> channel.getType() == ChannelType.TEXT &&
//                        CHANNEL_NAMES.contains(channel.getName())
//                ).findFirst().ifPresentOrElse(ch -> {
//                    var channel = (TextChannel) ch;
//                    if (channel.canTalk() && !guildData.hasReceivedGreet()) {
//                        channel.sendMessage(embedBuilder.build()).queue();
//                        guildData.setHasReceivedGreet(true);
//                        dbGuild.save();
//                    }
//                }, () -> {
//                    // Attempt to find the first channel we can talk to.
//                    var channel = (TextChannel) guildChannels.stream()
//                            .filter(guildChannel -> guildChannel.getType() == ChannelType.TEXT && ((TextChannel) guildChannel).canTalk())
//                            .findFirst()
//                            .orElse(null);
//
//                    // Basically same code as above, but w/e.
//                    if (channel != null && !guildData.hasReceivedGreet()) {
//                        channel.sendMessage(embedBuilder.build()).queue();
//                        guildData.setHasReceivedGreet(true);
//                        dbGuild.save();
//                    }
//                });
//            }
//        } catch (InsufficientPermissionException | NullPointerException | IllegalArgumentException ignored) {
//            // We don't need to catch those
//        } catch (Exception e) {
//            LOG.error("Unexpected error while processing a join event", e);
//        }
//    }

    private void onLeave(GuildLeaveEvent event) {
        try {
            final var jda = event.getJDA();
            final var guild = event.getGuild();

            // Post bot statistics to the main API.
            this.updateStats(jda);
            //Metrics.GUILD_ACTIONS.labels("leave").inc();
        } catch (NullPointerException | IllegalArgumentException ignored) {
            // ignore
        } catch (Exception e) {
            LOG.error("Unexpected error while processing a leave event", e);
        }
    }

    private void updateStats(JDA jda) {
        // This screws up with our shard stats, so we just need to ignore it.
        if (jda.getStatus() == JDA.Status.INITIALIZED) {
            return;
        }

        try(var jedis = CrashBotData.getDefaultJedisPool().getResource()) {
            var json = new JSONObject()
                    .put("guild_count", jda.getGuildCache().size())
                    .put("cached_users", jda.getUserCache().size())
                    .put("gateway_ping", jda.getGatewayPing())
                    .put("shard_status", jda.getStatus())
                    .put("last_ping_diff", ((EventManager) jda.getEventManager()).lastJDAEventDiff())
                    //.put("node_number", bot.getNodeNumber())
                    .toString();

            jedis.hset("shardstats-" + CONFIG.getClientId(), String.valueOf(jda.getShardInfo().getShardId()), json);
            LOG.debug("Sent process shard stats to redis -> {}", json);
        }
    }
}