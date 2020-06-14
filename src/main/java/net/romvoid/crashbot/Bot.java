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
package net.romvoid.crashbot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.commands.inerf.CommandManager;
import net.romvoid.crashbot.config.Configuration;
import net.romvoid.crashbot.config.Setup;
import net.romvoid.crashbot.hastebin.FileListener;

/**
 * The Main Bot Class.
 */
public class Bot {

	private static Bot instance;
	private JDA jda;
	private static final SimpleDateFormat timeStampFormatter = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
	/** The command manager. */
	private CommandManager commandManager;

	/** The Constant CONFIG_KEYS. */
	private static final String[] CONFIG_KEYS = { "token", "prefix" };

	/** The configuration. */
	private final Configuration configuration;
	

	/** The prefix. */
	private static String prefix;


	/**
	 * Instantiates a new bot.
	 */
	private Bot() {
		instance = this;
		configuration = new Configuration(new File("config.json"));
		for (String configKey : CONFIG_KEYS) {
			if (!configuration.has(configKey)) {
				String input = Setup.prompt(configKey);
				configuration.set(configKey, input);
			}
		}
		commandManager = new CommandManager();
		prefix = instance.configuration.getString("prefix");
		initJDA();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		if (instance != null)
			throw new RuntimeException("CrashBot has already been initialized in this VM.");
		new Bot();
	}

	/**
	 * Initiates the JDA Instance and builder.
	 */
	public static void initJDA() {
		if (instance == null)
			throw new NullPointerException("CrashBot has not been initialized yet.");

		JDABuilder builder = new JDABuilder(AccountType.BOT);
		builder.setToken(instance.configuration.getString("token"));
		builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
		builder.setActivity(Activity.playing("Galacticraft"));
		builder.addEventListeners(new MessageListener(), new FileListener());

		try {
			instance.jda = builder.build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
		getJDA().setAutoReconnect(true);
	}

	/**
	 * Handle command event.
	 *
	 * @param event the event
	 */
	public void handleCommandEvent(GuildMessageReceivedEvent event) {
		// If the event message is, e.g. !cmd testing testing, commandName is set to
		// "cmd"
		String prefix = getPrefix();
		String commandName = event.getMessage().getContentRaw().substring(prefix.length()).split(" ")[0].toLowerCase();
		commandManager.handleCommand(commandName, event);
	}

	/**
	 * Gets the single instance of Bot.
	 *
	 * @return single instance of Bot
	 */
	public static Bot getInstance() {
		if (instance == null)
			throw new IllegalStateException("Bot has not been initialised. Please use Bot#init() to create the bot");
		return instance;
	}

	/**
	 * Gets the prefix.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public static Configuration getConfiguration() {
		return instance == null ? null : instance.configuration;
	}

	/**
	 * Gets the JDA Instance.
	 *
	 * @return the JDA Instance
	 */
	public static JDA getJDA() {
		return instance == null ? null : instance.jda;
	}
	
	  /**
	   * @return a freshly generated timestamp in the 'dd.MM.yyyy HH:mm:ss' format.
	   */
	  public static String getNewTimestamp() {
	    return timeStampFormatter.format(new Date());
	  }
}
