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

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.romvoid.crashbot.utilities.FinderUtils;

/**
 * The listener interface for receiving file events. The class that is interested in processing a file event implements this interface, and the object created with that class is registered with a component using the component's <code>addFileListener<code> method. When the file event occurs, that object's appropriate method is invoked.
 *
 * @see FileEvent
 */
public class FileListener extends ListenerAdapter {
	private static StringBuilder builder = new StringBuilder();
	private static String hasteString;
	private static URI url;
	static final Map<String, String> cache = new HashMap<>();

	/**
	 * On message received.
	 *
	 * @param event the onMessageReceived event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot())
			return;
		System.out.println("Message Detected - Checking for Attachments");
		List<Attachment> list = new ArrayList<Attachment>();
		list.addAll(event.getMessage().getAttachments());
		if(!list.isEmpty()) {
			System.out.println("Message Contains Attachment - Checking if Valid File");
			list.forEach(ee -> {
				if (isValidFile(ee)) {
					System.out.println("Message File is Valid");
					getFileContent(event);
				}
			});
		}


	}

	/**
	 * Checks if is valid file. This checks if the file has the extensions .txt or .log which are common Minecraft crash-log and log file extensions.
	 *
	 * @param attachment the attachment
	 * @return true, if is valid file
	 */
	private static boolean isValidFile(Attachment attachment) {
		String[] cancelWords = { "txt", "log" };
		boolean found = false;
		for (String cancelWord : cancelWords) {
			found = attachment.getFileExtension().equals(cancelWord);
			if (found)
				break;
		}
		return found;
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
		embedBuilder.setFooter("Solutions are provided on a best attempt basis and are not guaranteed to work everytime\nAuthor: ROM#0590");
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
	private static void getFileContent(MessageReceivedEvent event) {
		TextChannel channel = event.getTextChannel();
		Message message = event.getMessage();
		String name = event.getMessage().getAttachments().get(0).getFileName();
		event.getMessage().getAttachments().get(0).retrieveInputStream().thenAccept(in -> {
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
			try {
				url = new URI(hasteString + ".yml");
				sendEmbed(channel, makeEmbed(channel, message, name, url));
				FinderUtils.finderSet.forEach((key, value) -> {
					if (FinderUtils.find(hasteString, url, key))
						sendEmbed(channel, makeEmbedWithSolution(value));
				});
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			event.getMessage().delete().queue();
		}).exceptionally(t -> { // handle failure
			t.printStackTrace();
			return null;
		});
	}


}
