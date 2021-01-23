package net.romvoid.crashbot.core.shard;

import static net.romvoid.crashbot.data.CrashBotData.config;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.var;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.romvoid.crashbot.CrashBot;
import net.romvoid.crashbot.core.EventManager;
import net.romvoid.crashbot.core.listeners.entities.CachedMessage;
import net.romvoid.crashbot.utilities.APIUtils;

public class Shard {

    private static final Logger log = LoggerFactory.getLogger(Shard.class);
    private final Cache<Long, Optional<CachedMessage>> messageCache =
            CacheBuilder.newBuilder().concurrencyLevel(5).maximumSize(2500).build();

    private final EventManager manager = new EventManager();
    private final int id;
    private final EventListener listener;
    private ScheduledFuture<?> statusChange;
    private JDA jda;
    
    public Shard(int id) {
        this.id = id;
        this.listener = event -> {
            if(event instanceof ReadyEvent) {
                synchronized(this) {
                    jda = event.getJDA();
                    if (statusChange != null) {
                        statusChange.cancel(true);
                    }
            
                    statusChange = CrashBot.getInstance()
                            .getExecutorService()
                            .scheduleAtFixedRate(Shard.this::changeStatus, 0, 3, TimeUnit.HOURS);
                }
            }
        };
    }

    @CheckReturnValue
    public int getId() {
        return id;
    }

    @Nonnull
    @CheckReturnValue
    public Cache<Long, Optional<CachedMessage>> getMessageCache() {
        return messageCache;
    }

    @Nonnull
    @CheckReturnValue
    public EventManager getManager() {
        return manager;
    }

    @Nonnull
    @CheckReturnValue
    public EventListener getListener() {
        return listener;
    }

    @Nullable
    @CheckReturnValue
    public JDA getNullableJDA() {
        return jda;
    }

    @Nonnull
    @CheckReturnValue
    public JDA getJDA() {
        return Objects.requireNonNull(jda, "Shard has not been started yet");
    }

    private void changeStatus() {

        AtomicInteger users = new AtomicInteger(0), guilds = new AtomicInteger(0);
        if (CrashBot.getInstance() != null) {
        	CrashBot.getInstance().getShardManager().getShardCache().forEach(jda -> {
                users.addAndGet((int) jda.getUserCache().size());
                guilds.addAndGet((int) jda.getGuildCache().size());
            });
        }

        JSONObject reply;

        try {
            var body = APIUtils.getFrom("/api/bot/splashes/random");
            reply = new JSONObject(new JSONTokener(body));
        } catch (Exception e) {
            reply = new JSONObject().put("splash", "With a missing status!");
        }

        String newStatus = reply.getString("splash")
                //Replace fest.
                .replace("%ramgb%", String.valueOf(((long) (Runtime.getRuntime().maxMemory() * 1.2D)) >> 30L))
                .replace("%usercount%", users.toString())
                .replace("%guildcount%", guilds.toString())
                .replace("%shardcount%", String.valueOf(CrashBot.getInstance().getShardManager().getShardsTotal()))
                .replace("%prettyusercount%", String.valueOf(users.get()))
                .replace("%prettyguildcount%", String.valueOf(guilds.get()));
        
        String p = config().get().getPrefix()[0];

        getJDA().getPresence().setActivity(Activity.playing(String.format("%shelp | %s | [%d]", p, newStatus, getId())));
        log.debug("Changed status to: " + newStatus);
    }
	
}
