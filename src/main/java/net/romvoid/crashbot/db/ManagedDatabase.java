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

package net.romvoid.crashbot.db;

import static com.rethinkdb.RethinkDB.r;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rethinkdb.net.Connection;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.XtraRuntimeOpts;
import net.romvoid.crashbot.db.entities.CrashBotObject;
import net.romvoid.crashbot.db.entities.DBGuild;

public class ManagedDatabase {
    private static final Logger log = LoggerFactory.getLogger(ManagedDatabase.class);
    private final Connection conn;

    public ManagedDatabase(@Nonnull Connection conn) {
        this.conn = conn;
    }

    private static void log(String message, Object... fmtArgs) {
        if (XtraRuntimeOpts.LOG_DB_ACCESS) {
            log.info(message, fmtArgs);
        }
    }

    private static void log(String message) {
        if (XtraRuntimeOpts.LOG_DB_ACCESS) {
            log.info(message);
        }
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull String guildId) {
        log("Requesting guild {} from rethink", guildId);
        DBGuild guild = r.table(DBGuild.DB_TABLE).get(guildId).runAtom(conn, DBGuild.class);
        return guild == null ? DBGuild.of(guildId) : guild;
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull Guild guild) {
        return getGuild(guild.getId());
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull Member member) {
        return getGuild(member.getGuild());
    }

    @Nonnull
    @CheckReturnValue
    public DBGuild getGuild(@Nonnull GuildMessageReceivedEvent event) {
        return getGuild(event.getGuild());
    }

    @Nonnull
    @CheckReturnValue
    public CrashBotObject getCrashBotData() {
        log("Requesting CrashBotObject from rethink");
        CrashBotObject obj = r.table(CrashBotObject.DB_TABLE).get("crashbot").runAtom(conn, CrashBotObject.class);
        return obj == null ? CrashBotObject.create() : obj;
    }

    public void save(@Nonnull ManagedObject object) {
        log("Saving {} {}:{} to rethink (replacing)", object.getClass().getSimpleName(), object.getTableName(), object.getDatabaseId());

        r.table(object.getTableName())
                .insert(object)
                .optArg("conflict", "replace")
                .runNoReply(conn);
    }

    public void saveUpdating(@Nonnull ManagedObject object) {
        log("Saving {} {}:{} to rethink (updating)", object.getClass().getSimpleName(), object.getTableName(), object.getDatabaseId());

        r.table(object.getTableName())
                .insert(object)
                .optArg("conflict", "update")
                .runNoReply(conn);
    }

    public void delete(@Nonnull ManagedObject object) {
        log("Deleting {} {}:{} from rethink", object.getClass().getSimpleName(), object.getTableName(), object.getDatabaseId());

        r.table(object.getTableName())
                .get(object.getId())
                .delete()
                .runNoReply(conn);
    }
}
