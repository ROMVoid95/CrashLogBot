package net.romvoid.crashbot.core.listeners;

import com.google.common.cache.Cache;
import com.rethinkdb.gen.exc.ReqlError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.romvoid.crashbot.core.command.processor.CommandProcessor;
import net.romvoid.crashbot.core.listeners.entities.CachedMessage;
import net.romvoid.crashbot.core.opts.InteractiveOpts;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.utilities.Snow64;
import net.romvoid.crashbot.utilities.commands.EmoteReference;
import net.romvoid.crashbot.utilities.commands.RateLimiter;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IllegalFormatException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandListener implements EventListener {
    private static final RateLimiter experienceRatelimiter = new RateLimiter(TimeUnit.SECONDS, 18);
    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
    // Commands ran this session.
    private static int commandTotal = 0;
    private final Random random = new Random();
    private final CommandProcessor commandProcessor;
    private final ExecutorService threadPool;
    private final Cache<Long, Optional<CachedMessage>> messageCache;

    public CommandListener(CommandProcessor processor, ExecutorService threadPool, Cache<Long, Optional<CachedMessage>> messageCache) {
        this.commandProcessor = processor;
        this.threadPool = threadPool;
        this.messageCache = messageCache;
    }

    public static int getCommandTotal() {
        return commandTotal;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildMessageReceivedEvent) {
            var msg = (GuildMessageReceivedEvent) event;
            // Ignore myself and bots.
            // Technically ignoring oneself is an extra step -- we're a bot, and we ignore bots.
            var isSelf = msg.getAuthor().getIdLong() == msg.getJDA().getSelfUser().getIdLong();
            if (msg.getAuthor().isBot() || msg.isWebhookMessage() || isSelf) {
                return;
            }

            // Inserts a cached message into the cache. This only holds the id and the content, and is way lighter than saving the entire jda object.
            messageCache.put(msg.getMessage().getIdLong(), Optional.of(
                    new CachedMessage(msg.getGuild().getIdLong(), msg.getAuthor().getIdLong(), msg.getMessage().getContentDisplay()))
            );

            // We can't talk here, so we don't need to run anything.
            // Run this check before executing on the pool to avoid wasting a thread.
            if (!msg.getChannel().canTalk()) {
                return;
            }

            threadPool.execute(() -> onCommand(msg));
        }
    }

    private void onCommand(GuildMessageReceivedEvent event) {
        try {
            if (commandProcessor.run(event)) {
                // Remove running flag
                try (var jedis = CrashBotData.getDefaultJedisPool().getResource()) {
                    jedis.del("commands-running-" + event.getAuthor().getId());
                }

                commandTotal++;
            } else {
                try {
                    // Only run experience if no command has been executed, avoids weird race conditions when saving player status.
                    // Only run experience if the user is not rate limited (clears every 30 seconds) and if the member is not null.
                    // This will never get here if it's a bot or a webhook message due to the check we do on line 78.
                    if (random.nextInt(15) > 7 && event.getMember() != null && experienceRatelimiter.process(event.getAuthor())) {
                        // If a command is running on another node, don't handle (this is an issue due to multiple different Player objects)
                        try (var jedis = CrashBotData.getDefaultJedisPool().getResource()) {
                            var running = jedis.get("commands-running-" + event.getAuthor().getId());
                            if (running != null) {
                                return;
                            }
                        }

                        // Don't run the experience handler on this channel if there's an InteractiveOperation running as there might be issues with
                        // some nasty race conditions involving player save.
                        if (InteractiveOpts.get(event.getChannel()).size() > 0) {
                            return;
                        }
                    }
                } catch (Exception ignored) { }
            }
        } catch (IllegalFormatException e) {
            var id = Snow64.toSnow64(event.getMessage().getIdLong());
            event.getChannel().sendMessageFormat(
                    "%sWe found at error when trying to format a String. Please report on the support server (At <https://support.mantaro.site>) with error ID `%s`.",
                    EmoteReference.ERROR, id, EmoteReference.ZAP
            ).queue();

            log.warn("Wrong String format. Check this. ID: {}", id, e);
        } catch (IndexOutOfBoundsException e) {
            var id = Snow64.toSnow64(event.getMessage().getIdLong());
            event.getChannel().sendMessageFormat(
                    "%sYour query returned no results or you used the incorrect arguments, seemingly (Error ID: `%s`). Just in case, check command help!",
                    EmoteReference.ERROR, id
            ).queue();

            log.warn("Exception caught and alternate message sent. We should look into this, anyway (ID: {})", id, e);
        } catch (PermissionException e) {
            if (e.getPermission() != Permission.UNKNOWN) {
                event.getChannel().sendMessageFormat(
                        "%sI don't have permission to do this :(\nI need the permission: **%s**",
                        EmoteReference.ERROR, e.getPermission().getName()
                ).queue();
            } else {
                event.getChannel().sendMessage(
                        EmoteReference.ERROR +
                        "I cannot perform this action due to the lack of permission! Is the role I might be trying to assign " +
                        "higher than my role? Do I have the correct permissions/hierarchy to perform this action?"
                ).queue();
            }
        } catch (IllegalArgumentException e) { //NumberFormatException == IllegalArgumentException
            var id = Snow64.toSnow64(event.getMessage().getIdLong());
            event.getChannel().sendMessageFormat(
                    "%sI think you forgot something on the floor. (Error ID: `%s`)\n" +
                    "%sCould be an internal error, but check the command arguments or maybe the message I'm trying to send exceeds 2048 characters, " +
                    "Just in case, check command help! (If you need further help, go to <https://support.mantaro.site>)",
                    EmoteReference.ERROR, id, EmoteReference.WARNING
            ).queue();

            log.warn("Exception caught and alternate message sent. We should look into this, anyway (ID: {})", id, e);
        } catch (ReqlError e) {
            // So much just went wrong...
            e.printStackTrace();
        } catch (Exception e) {
//            var context = I18n.of(event.getGuild());
            var id = Snow64.toSnow64(event.getMessage().getIdLong());
//            var player = CrashBotData.db().get(event.getAuthor());
//
//            event.getChannel().sendMessageFormat(
//                    "%s%s (Unexpected error, ID: `%s`)\n%s",
//                    EmoteReference.ERROR, context.get("general.boom_quotes"), id, context.get("general.generic_error")
//            ).queue();

            log.error("Error happened on command: {} (Error ID: {})", event.getMessage().getContentRaw(), id, e);
        }
    }
}
