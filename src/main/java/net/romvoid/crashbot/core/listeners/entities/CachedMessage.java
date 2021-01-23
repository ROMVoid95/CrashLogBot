package net.romvoid.crashbot.core.listeners.entities;

import lombok.var;
import net.dv8tion.jda.api.entities.User;
import net.romvoid.crashbot.CrashBot;

public class CachedMessage {
    private final long guildId;
    private final long author;
    private final String content;

    public CachedMessage(long guildId, long author, String content) {
        this.guildId = guildId;
        this.author = author;
        this.content = content;
    }

    public User getAuthor() {
        var guild = CrashBot.getInstance().getShardManager().getGuildById(guildId);
        User user = null;

        if (guild != null)  {
            user = guild.retrieveMemberById(author).complete().getUser();
        }

        return user;
    }

    public String getContent() {
        return this.content;
    }
}
