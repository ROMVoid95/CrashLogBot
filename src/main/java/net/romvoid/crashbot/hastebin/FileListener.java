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
package net.romvoid.crashbot.hastebin;

import static net.romvoid.crashbot.utilities.FileUtil.CRASHLOG;
import static net.romvoid.crashbot.utilities.FileUtil.LOGS_TXT;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.romvoid.crashbot.Bot;
import net.romvoid.crashbot.utilities.EmbedUtil;
import net.romvoid.crashbot.utilities.FileUtil;
import net.romvoid.crashbot.utilities.GithubConnect;

/**
 * The listener interface for receiving file events. The class that is
 * interested in processing a file event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addFileListener<code> method. When the file event occurs,
 * that object's appropriate method is invoked.
 *
 * @see FileEvent
 */
public class FileListener extends ListenerAdapter {
	private static StringBuilder builder = new StringBuilder();
	private static String hasteString;
	private static URI url;
	static final Map<String, String> cache = new HashMap<>();
	String messageId;
	TextChannel channel;
	Message message;
	String name;
	String author;

	/**
	 * On message received.
	 *
	 * @param event the onMessageReceived event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		List<Attachment> list = new ArrayList<Attachment>();
		list.addAll(event.getMessage().getAttachments());
		this.message = event.getMessage();
		this.channel = event.getTextChannel();
		this.messageId = event.getMessageId();
		this.author = event.getAuthor().getAsTag();
		list.forEach(a -> {
			this.name = a.getFileName();
			switch (a.getFileExtension()) {
			case "txt":
				if (FileUtil.matchToExt(CRASHLOG, a.getFileName())) {
					Bot.logMsg(messageId, author + " submitted " + name);
					getFileContent(a, event, messageId);
				}
				break;
			case "log":
				if (FileUtil.matchToExt(LOGS_TXT, a.getFileName())) {
					Bot.logMsg(messageId, author + " submitted " + name);
					getFileContent(a, event, messageId);
				}
				break;
			case "finder":
				if (event.getGuild().getOwnerId().equals(event.getAuthor().getId())) {
					String out = FilenameUtils.removeExtension(a.getFileName());
					
					message.getAttachments().get(0).downloadToFile("finders/" + out)
							.thenAccept(file -> {
								try {
									GithubConnect.send(file);
									Bot.logMsg(messageId, "Uploaded Finder [" + out + "] to Github");
								} catch (IOException e) {
									e.printStackTrace();
								}
								Bot.logMsg(messageId, "Added Finder [" + out + "] to database. Submitted by " + author);
								event.getMessage().delete().queue();
								EmbedUtil.sendAndDelete(channel, sendFinderAccept(), 15, TimeUnit.SECONDS);
							})
							.exceptionally(t ->
					         { // handle failure
					             t.printStackTrace();
					             return null;
					         });

				}
			}
		});

	}

	/**
	 * Make embed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @return the embed builder
	 */
	private static EmbedBuilder makeEmbed(TextChannel channel, Message message, String filename, URI url) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.cyan);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.addField(user + "'s Crash Log", "[" + filename + "](" + url + ")", false);
		embedBuilder.setFooter("Links do not expire\nCrash-Log Uploading Service for " + channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;

	}

	/**
	 * Make embed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @return the embed builder
	 */
	private static EmbedBuilder makeEmbedWithSolution(String fix) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.yellow);
		embedBuilder.setTitle("Possible Solutions Found");
		embedBuilder.setDescription("`" + fix + "`");
		embedBuilder.setFooter(
				"Solutions are provided on a best attempt basis and are not guaranteed to work everytime\nAuthor: ROM#0590");
		return embedBuilder;
	}
	
	/**
	 * Make embed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @return the embed builder
	 */
	private static EmbedBuilder sendFinderAccept() {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.green);
		embedBuilder.setTitle("New Finder Added!");
		embedBuilder.setDescription("Finder has been sucessfully added to the database");
		return embedBuilder;
	}

	public static void sendEmbed(TextChannel channel, EmbedBuilder embed) {
		channel.sendMessage(embed.build()).queue();
	}

	public static void sendPing(TextChannel channel, String asMention) {
		channel.sendMessage(asMention).queue();
	}

	/**
	 * Gets the content of the log for processing to upload to hastebin
	 *
	 * @param event the onMessageReceived event
	 * @return the file content
	 */
	private void getFileContent(Attachment doc, MessageReceivedEvent event, String messageId) {
		doc.retrieveInputStream().thenAccept(in -> {
			builder = new StringBuilder();
			byte[] buf = new byte[1024];
			int count = 0;
			try {
				while ((count = in.read(buf)) > 0) {
					builder.append(new String(buf, 0, count));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			hasteString = Hastebin.paste(builder.toString());
			Bot.logMsg(messageId, hasteString);
			try {
				url = new URI(hasteString + ".yml");
				sendEmbed(channel, makeEmbed(channel, message, name, url));

				for (File file : getFiles()) {
					for (String[] lines : getFinder(file)) {
						if (find(url, lines[0], messageId)) {
							sendEmbed(channel, makeEmbedWithSolution(lines[1]));
						}
					}
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			event.getMessage().delete().queue();
		}).exceptionally(t -> { // handle failure
			t.printStackTrace();
			return null;
		});
	}

	@SuppressWarnings("unused")
	public static boolean find(URI url, String entry, String messageId) {
		String id = hasteString.replace(Hastebin.getPasteURL(), "");
		String URLString = Hastebin.getPasteURL() + "raw/" + id + "/";
		boolean result = false;
		try {
			URL URL = new URL(URLString);
			HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
			connection.setDoOutput(true);
			connection.setConnectTimeout(10000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			int currentLine = 0;
			while ((line = reader.readLine()) != null) {
				currentLine ++;
				if (line.contains(entry)) {
					Bot.logMsg(messageId, "Possible Solution Found");
					return true;
				}
			}
			reader.close();
		} catch (IOException e) {

		}
		return result;
	}

	private static List<File> getFiles() {
		File folder = new File("finders/");
		File[] files = folder.listFiles();
		List<File> list = new ArrayList<File>();
		for (File f : files) {

			list.add(f);
		}

		return list;
	}

	private static List<String[]> getFinder(File file) {
		List<String[]> lines = new ArrayList<>();
		;
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				lines.add(scanner.nextLine().split(";;"));
			}
			scanner.close();
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
