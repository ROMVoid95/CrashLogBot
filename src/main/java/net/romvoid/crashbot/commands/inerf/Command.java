/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 ROMVoid
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.romvoid.crashbot.commands.inerf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/**
 * The Class Command.
 */
public abstract class Command {

    /** The command name. */
    protected String name;
    
    /** The commands parameters. */
    protected List<String> parameters;
    
    /** The commands aliases. */
    protected Set<String> aliases;

    /**
     * Instantiates a new command.
     *
     * @param name the name
     */
    public Command(String name) {
        this.name = name;
        this.parameters = new ArrayList<>(3);
        this.aliases = new HashSet<>();
    }

    /**
     * Adds the alias.
     *
     * @param alias the alias
     * @return the command
     */
    public Command addAlias(String alias) {
        aliases.add(alias);
        return this;
    }

    /**
     * @param args         arguments for the command
     * @param channel      channel where the command is executed
     * @param author       who invoked the command
     * @param inputMessage the incoming message object
     * @return the message to output or an empty string for nothing
     */
    public abstract void executeAndHandle(GuildMessageReceivedEvent event, List<String> params, User author, Message inputMessage);
    

    /**
     * Gets the name.
     *
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public abstract String getDescription();

    /**
     * Gets the parameters.
     *
     * @return {@link #parameters}
     */
    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Gets the aliases.
     *
     * @return {@link #aliases}
     */
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }
}
