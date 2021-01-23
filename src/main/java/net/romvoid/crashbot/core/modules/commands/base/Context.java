/*
 * Copyright (C) 2016-2021 David Rubio Escares / Kodehawa
 *
 *  Mantaro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  Mantaro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro. If not, see http://www.gnu.org/licenses/
 */

package net.romvoid.crashbot.core.modules.commands.base;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.romvoid.crashbot.CrashBot;
import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.db.ManagedDatabase;
import net.romvoid.crashbot.db.entities.DBGuild;
import net.romvoid.crashbot.utilities.StringUtils;
import net.romvoid.crashbot.utilities.commands.DiscordFindUtil;
import net.romvoid.crashbot.utilities.commands.EmoteReference;
import redis.clients.jedis.JedisPool;

public class Context {
    private final CrashBot bot = CrashBot.getInstance();
    private final ManagedDatabase managedDatabase = CrashBotData.db();
    private final MainConfig config = CrashBotData.config().get();

    private final GuildMessageReceivedEvent event;
    private final String content;
    private final boolean isMentionPrefix;

    public Context(GuildMessageReceivedEvent event, String content, boolean isMentionPrefix) {
        this.event = event;
        this.content = content;
        this.isMentionPrefix = isMentionPrefix;
    }

    public CrashBot getBot() {
        return bot;
    }

    public MainConfig getConfig() {
        return config;
    }

    public ManagedDatabase db() {
        return managedDatabase;
    }

    public GuildMessageReceivedEvent getEvent() {
        return event;
    }

    public JDA getJDA() {
        return getEvent().getJDA();
    }

    public List<User> getMentionedUsers() {
        final var mentionedUsers = getEvent().getMessage().getMentionedUsers();
        if (isMentionPrefix) {
            final var mutable = new LinkedList<>(mentionedUsers);
            return mutable.subList(1, mutable.size());
        }

        return mentionedUsers;
    }

    public List<Member> getMentionedMembers() {
        final var mentionedMembers = getEvent().getMessage().getMentionedMembers();
        if (isMentionPrefix) {
            final var mutable = new LinkedList<>(mentionedMembers);
            return mutable.subList(1, mutable.size());
        }

        return mentionedMembers;
    }

    public Member getMember() {
        return event.getMember();
    }

    public User getUser() {
        return event.getAuthor();
    }

    public User getAuthor() {
        return getUser();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public SelfUser getSelfUser() {
        return event.getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return getGuild().getSelfMember();
    }

    public TextChannel getChannel() {
        return event.getChannel();
    }

    public ShardManager getShardManager() {
        return getBot().getShardManager();
    }

    public DBGuild getDBGuild() {
        return managedDatabase.getGuild(getGuild());
    }

    public boolean hasReactionPerms() {
        return getSelfMember().hasPermission(getChannel(), Permission.MESSAGE_ADD_REACTION) &&
                // Somehow also needs this?
                getSelfMember().hasPermission(getChannel(), Permission.MESSAGE_HISTORY);
    }

    public String getContent() {
        return content;
    }

    public String[] getArguments() {
        return StringUtils.advancedSplitArgs(content, 0);
    }

    public Map<String, String> getOptionalArguments() {
        return StringUtils.parseArguments(getArguments());
    }

    public void send(Message message) {
        getChannel().sendMessage(message).queue();
    }

    public void send(String message) {
        getChannel().sendMessage(message).queue(success -> {success.addReaction(EmoteReference.ERROR.getDiscordNotation());});
    }

//    public void sendFormat(String message, Object... format) {
//        getChannel().sendMessage(
//                String.format(Commons.getLocaleFromLanguage(message, format)).queue());
//    }

    public void send(MessageEmbed embed) {
        // Sending embeds while supressing the failure callbacks leads to very hard
        // to debug bugs, so enable it.
        getChannel().sendMessage(embed).queue(success -> {}, Throwable::printStackTrace);
    }

//    public void sendLocalized(String localizedMessage, Object... args) {
//        // Stop swallowing issues with String replacements (somehow really common)
//        getChannel().sendMessage(
//                String.format(Utils.getLocaleFromLanguage(getLanguageContext()), languageContext.get(localizedMessage), args)
//        ).queue(success -> {}, Throwable::printStackTrace);
//    }
//
//    public void sendLocalized(String localizedMessage) {
//        getChannel().sendMessage(languageContext.get(localizedMessage)).queue();
//    }

    public void sendStripped(String message) {
        getChannel().sendMessage(message)
                .allowedMentions(EnumSet.noneOf(Message.MentionType.class))
                .queue();
    }

//    public void sendStrippedLocalized(String localizedMessage, Object... args) {
//        getChannel().sendMessage(String.format(
//                Utils.getLocaleFromLanguage(getLanguageContext()), languageContext.get(localizedMessage), args)
//        ).allowedMentions(EnumSet.noneOf(Message.MentionType.class)).queue();
//    }

    public Task<List<Member>> findMember(String query, Consumer<List<Member>> success) {
        return DiscordFindUtil.lookupMember(getGuild(), this, query).onSuccess(s -> {
            try {
                success.accept(s);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    public User retrieveUserById(String id) {
        User user = null;
        try {
            user = CrashBot.getInstance().getShardManager().retrieveUserById(id).complete();
        } catch (Exception ignored) { }

        return user;
    }

    public Member retrieveMemberById(Guild guild, String id, boolean update) {
        Member member = null;
        try {
            member = guild.retrieveMemberById(id, update).complete();
        } catch (Exception ignored) { }

        return member;
    }

    public Member retrieveMemberById(String id, boolean update) {
        Member member = null;
        try {
            member = getGuild().retrieveMemberById(id, update).complete();
        } catch (Exception ignored) { }

        return member;
    }

    public boolean isMentionPrefix() {
        return isMentionPrefix;
    }

    public JedisPool getJedisPool() {
        return CrashBotData.getDefaultJedisPool();
    }
}
