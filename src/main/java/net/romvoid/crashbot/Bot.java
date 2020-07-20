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

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.GuildlistCommand;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.JDAImpl;
import net.romvoid.crashbot.commands.GithubCommand;
import net.romvoid.crashbot.commands.InviteCommand;
import net.romvoid.crashbot.config.Configuration;
import net.romvoid.crashbot.config.Setup;
import net.romvoid.crashbot.hastebin.FileListener;

/**
 * The Main Bot Class.
 */
public class Bot {

	private static Bot instance;
	private JDAImpl jda;
	private static final SimpleDateFormat timeStampFormatter = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
	/** The Constant CONFIG_KEYS. */
	private static final String[] CONFIG_KEYS = { "token", "prefix" };

	/** The configuration. */
	private final Configuration configuration;

	private static EventWaiter waiter;

	public static CommandClientBuilder client;

	/** The prefix. */
	private static String prefix;

	private static Set<GatewayIntent> intents = new HashSet<>();

	public static final Logger LOG = (Logger) LoggerFactory.getLogger(Bot.class);

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

		prefix = instance.configuration.getString("prefix");
		setIntents();
		client = new CommandClientBuilder();
		waiter = new EventWaiter();
		client.setOwnerId("393847930039173131");
		client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");
		client.setPrefix(prefix);

		client.addCommands(new GithubCommand(), new InviteCommand(), new GuildlistCommand(waiter));
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

		try {
			instance.jda = (JDAImpl) JDABuilder.create(instance.configuration.getString("token"), intents)
					.setStatus(OnlineStatus.DO_NOT_DISTURB)
					.setActivity(Activity.playing("Galacticraft").asRichPresence())
					.addEventListeners(waiter, client.build(), new FileListener())
					.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE).build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
	}

	private void setIntents() {
		intents.add(GUILD_MESSAGES);
		intents.add(GUILD_MEMBERS);
		intents.add(GUILD_PRESENCES);
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
	public static JDAImpl getJDA() {
		return instance == null ? null : instance.jda;
	}

	/**
	 * @return a freshly generated timestamp in the 'dd.MM.yyyy HH:mm:ss' format.
	 */
	public static String getNewTimestamp() {
		return timeStampFormatter.format(new Date());
	}
	
	public static void logMsg(String messageId, String conent) {
		String msg = "[" + messageId + "] " + conent;
		Bot.LOG.info(msg);
	}
}
