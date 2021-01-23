package net.romvoid.crashbot.logging;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import lombok.var;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.utilities.Commons;

public class LogUtils {

	private static final Logger log = LoggerFactory.getLogger(LogUtils.class);
    private static final String WEBHOOK_START = "https://discordapp.com/api/webhooks/";
    private static final String ICON_URL = "https://i.imgur.com/jMQ1XiP.png";
    private static WebhookClient LOGBACK_WEBHOOK;
    private static WebhookClient SHARD_WEBHOOK;

    static {
        var shardWebhook = CrashBotData.config().get().getShardWebhookUrl();
        var logWebhook = CrashBotData.config().get().getWebhookUrl();
        
        if (shardWebhook != null) {
            var parts = shardWebhook.replace(WEBHOOK_START, "").split("/");
            SHARD_WEBHOOK = new WebhookClientBuilder(Long.parseLong(parts[0]), parts[1]).build();
        }

        if (logWebhook != null) {
            var parts = logWebhook.replace(WEBHOOK_START, "").split("/");
            LOGBACK_WEBHOOK = new WebhookClientBuilder(Long.parseLong(parts[0]), parts[1]).build();
        } else {
            log.error("Webhook URL is null. Webhooks won't be posted at all to status channels.");
        }
    }
    
    public static void shard(String message) {
        if (SHARD_WEBHOOK == null) {
            return;
        }

        try {
            SHARD_WEBHOOK.send(new WebhookEmbed(
                    null, Color.PINK.getRGB(), message,
                    null, null,
                    new WebhookEmbed.EmbedFooter(Commons.formatDate(OffsetDateTime.now()), ICON_URL),
                    new WebhookEmbed.EmbedTitle("Shard", null), null,
                    new ArrayList<>()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String title, String message) {
        if (LOGBACK_WEBHOOK == null) {
            return;
        }

        try {
            LOGBACK_WEBHOOK.send(new WebhookEmbed(
                    null, Color.PINK.getRGB(), message,
                    null, null,
                    new WebhookEmbed.EmbedFooter(Commons.formatDate(OffsetDateTime.now()), ICON_URL),
                    new WebhookEmbed.EmbedTitle(title, null), null,
                    new ArrayList<>()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        if (LOGBACK_WEBHOOK == null) {
            return;
        }

        try {
            LOGBACK_WEBHOOK.send(new WebhookEmbed(
                    null, Color.PINK.getRGB(), message,
                    null, null,
                    new WebhookEmbed.EmbedFooter(Commons.formatDate(OffsetDateTime.now()), ICON_URL),
                    new WebhookEmbed.EmbedTitle("Log", null), null,
                    new ArrayList<>()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void simple(String message) {
        if (LOGBACK_WEBHOOK == null) {
            return;
        }

        try {
            LOGBACK_WEBHOOK.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shardSimple(String message) {
        if (SHARD_WEBHOOK == null) {
            return;
        }

        try {
            SHARD_WEBHOOK.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
