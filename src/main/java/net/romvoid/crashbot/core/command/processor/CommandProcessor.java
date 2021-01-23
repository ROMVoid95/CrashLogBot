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

package net.romvoid.crashbot.core.command.processor;

import static net.romvoid.crashbot.utilities.StringUtils.splitArgs;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.core.CommandRegistry;
import net.romvoid.crashbot.data.CrashBotData;

public class CommandProcessor {
    public static final CommandRegistry REGISTRY = new CommandRegistry();

    public boolean run(GuildMessageReceivedEvent event) {
        final var config = CrashBotData.config().get();
        // The command executed, in raw form.
        var rawCmd = event.getMessage().getContentRaw();
        // Lower-case raw cmd check, only used for prefix checking.
        final var lowerRawCmd = rawCmd.toLowerCase();

        // Mantaro prefixes.
        String[] prefix = config.prefix;
        // Guild-specific prefix.
        final var dbGuild = CrashBotData.db().getGuild(event.getGuild());
        var customPrefix = dbGuild.getData().getGuildCustomPrefix();
        // Possible mentions
        boolean isMention = false;
        String[] mentionPrefixes = {
                "<@%s> ".formatted(config.getClientId()),
                "<@!%s> ".formatted(config.getClientId())
        };

        // What prefix did this person use.
        String usedPrefix = null;
        for (String mention : mentionPrefixes) {
            if (lowerRawCmd.startsWith(mention)) {
                usedPrefix = mention;
                isMention = true;
            }
        }

        for (String s : prefix) {
            if (lowerRawCmd.startsWith(s)) {
                usedPrefix = s;
            }
        }

        // Remove prefix from arguments.
        if (usedPrefix != null && lowerRawCmd.startsWith(usedPrefix.toLowerCase())) {
            rawCmd = rawCmd.substring(usedPrefix.length());
        } else if (customPrefix != null && lowerRawCmd.startsWith(customPrefix.toLowerCase())) {
            rawCmd = rawCmd.substring(customPrefix.length());
            usedPrefix = customPrefix;
        } else if (usedPrefix == null) {
            return false;
        }

        // This could be done using a lock, but that would be a little too blocking. So just set a flag.
        try (var jedis = CrashBotData.getDefaultJedisPool().getResource()) {
            jedis.set("commands-running-" + event.getAuthor().getId(), String.valueOf(1));
        }

        // The command arguments to parse.
        String[] parts = splitArgs(rawCmd, 2);
        String cmdName = parts[0], content = parts[1];

        // Run the actual command here.
        REGISTRY.process(event, dbGuild, cmdName, content, usedPrefix, isMention);

        return true;
    }
}
