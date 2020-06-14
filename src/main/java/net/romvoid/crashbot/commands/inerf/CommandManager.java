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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.utilities.ReflectUtil;

/**
 * The Class CommandManager.
 */
public class CommandManager {

	/** The commands. */
	private Map<String, Command> commands;

	/**
	 * Instantiates a new command manager.
	 */
	public CommandManager() {
		commands = new HashMap<>();
		getCmds();
	}

	/**
	 * Handle command.
	 *
	 * @param commandName the command name
	 * @param event       the event
	 */
	public void handleCommand(String commandName, GuildMessageReceivedEvent event) {
		Optional<Command> commandOptional = commandFromName(commandName);

		// Adds any space separated strings to the parameter list
		commandOptional.ifPresent(command -> {
			String[] tokens = event.getMessage().getContentRaw().substring(1).toLowerCase().split(" ", 2);
			List<String> paramList = new ArrayList<>();
			if (hasParams(tokens)) {
				final String params = tokens[1].trim();
				paramList = new ArrayList<>(Arrays.asList(params.split(" ")));
			}
			command.executeAndHandle(event, paramList, null, null);
			System.out.println(paramList);
		});
	}

	private void getCmds() {
		for (Class<? extends Command> clazz : ReflectUtil.getCommandClasses()) {
			register(clazz);
		}
	}

	/**
	 * Register.
	 *
	 * @param clazz the command
	 */
	public void register(Class<? extends Command> clazz) {
		Command command;
		try {
			command = clazz.newInstance();

			commands.put(command.getName().toLowerCase(), command);

			for (String alias : command.getAliases())
				commands.put(alias.toLowerCase(), command);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Checks for params.
	 *
	 * @param tokens the tokens
	 * @return true, if successful
	 */
	private boolean hasParams(String[] tokens) {
		return tokens.length > 1;
	}

	/**
	 * Command from name.
	 *
	 * @param name the name
	 * @return the optional
	 */
	public Optional<Command> commandFromName(String name) {
		return Optional.ofNullable(commands.get(name));
	}
}
