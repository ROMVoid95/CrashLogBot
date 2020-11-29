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
package net.romvoid.crashbot.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.romvoid.crashbot.Bot;
import net.romvoid.crashbot.file.solution.FileUtil;
import net.romvoid.crashbot.file.solution.Parser;
import net.romvoid.crashbot.file.solution.Solution;
import net.romvoid.crashbot.utilities.HasteUtil;

/**
 * The listener interface for receiving file events. The class that is
 * interested in processing a file event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addFileListener<code> method. When the file event occurs,
 * that object's appropriate method is invoked.
 *
 * @see FileEvent
 */
public class FileHandler {
	private static StringBuilder builder = new StringBuilder();
	static final Map<String, String> cache = new HashMap<>();
	private MessageReceivedEvent event;
	List<Attachment> list;
	private String messageId;
	private TextChannel channel;
	private Message message;
	private String name;
	private String author;
	
	public FileHandler(MessageReceivedEvent event) {
		this.event = event;
		
		this.list = new ArrayList<Attachment>(event.getMessage().getAttachments());
		this.message = event.getMessage();
		this.channel = event.getTextChannel();
		this.messageId = event.getMessageId();
		this.author = event.getAuthor().getAsTag();
		
		this.handleFile();
	}
	
	public FileHandler(MessageReceivedEvent event, String url) {
		this.event = event;
		this.message = event.getMessage();
		this.channel = event.getTextChannel();
		this.messageId = event.getMessageId();
		this.author = event.getAuthor().getAsTag();
		
		this.getContentFromUbuntu(url, this.messageId);
	}
	
	public FileHandler(MessageReceivedEvent event, String url, String id) {
		this.event = event;
		this.message = event.getMessage();
		this.channel = event.getTextChannel();
		this.messageId = event.getMessageId();
		this.author = event.getAuthor().getAsTag();
		
		this.getContent(url, id , this.messageId);
	}

	/**
	 * On message received.
	 *
	 * @param event the onMessageReceived event
	 */
	private void handleFile() {

		list.forEach(file -> {
			this.name = file.getFileName();
			switch (file.getFileExtension()) {
			case "txt":
				if (FileUtil.matchToExt(Patterns.Filenames.CRASHLOG, file.getFileName())) {
					Bot.logMsg(messageId, author + " submitted " + name);
					getFileContent(file, messageId);
				}
				break;
			case "log":
				if (FileUtil.matchToExt(Patterns.Filenames.LOGS_TXT, file.getFileName())) {
					Bot.logMsg(messageId, author + " submitted " + name);
					getFileContent(file, messageId);
				}
				break;
			}
		});

	}

	public static void sendEmbed(TextChannel channel, EmbedBuilder embed) {
		channel.sendMessage(embed.build()).queue();
	}
//
//	public static void sendPing(TextChannel channel, String asMention) {
//		channel.sendMessage(asMention).queue();
//	}

	/**
	 * Gets the content of the log for processing to upload to hastebin
	 *
	 * @param event the onMessageReceived event
	 * @return the file content
	 */
	private void getFileContent(Attachment doc, String messageId) {
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
			String msg = builder.toString();
			HasteUtil.paste(msg);
			Solution foundSolution = Parser.find(msg);
			boolean unsupported = Parser.checkVersion(msg);
			System.out.println("Supported: " + unsupported);
			EmbedBuilder embed;
			
			if(!unsupported) {
				embed = Embeds.crashlogNotSupportedEmbed(channel, message, name, HasteUtil.getLink(), Parser.version);
				sendEmbed(channel, embed);
			} else {
				if(foundSolution != null) {
					embed = Embeds.crashlogWithSolutionEmbed(channel, message, name, HasteUtil.getLink(), foundSolution.getReply());
					sendEmbed(channel, embed);
				} else {
					embed = Embeds.crashlogOnlyEmbed(channel, message, name, HasteUtil.getLink());
					sendEmbed(channel, embed);
				}
			}
			this.event.getMessage().delete().queue();
		}).exceptionally(t -> { // handle failure
			t.printStackTrace();
			return null;
		});
	}
	
	private void getContent(String url, String id, String messageId) {
		String URLString = url + "raw/" + id + "/";
		String msg = HasteUtil.getOtherPaste(URLString);
		HasteUtil.paste(msg);
		Solution foundSolution = Parser.find(msg);
		boolean unsupported = Parser.checkVersion(msg);
		System.out.println("Supported: " + unsupported);
		EmbedBuilder embed;
		String name = HasteUtil.getLink().toString();
		
		if(!unsupported) {
			embed = Embeds.crashlogNotSupportedEmbed(channel, message, name, Parser.version);
			sendEmbed(channel, embed);
		} else {
			if(foundSolution != null) {
				embed = Embeds.crashlogWithSolutionEmbed(channel, message, name, foundSolution.getReply());
				sendEmbed(channel, embed);
			} else {
				embed = Embeds.crashlogOnlyEmbed(channel, message, name);
				sendEmbed(channel, embed);
			}
		}
		this.event.getMessage().delete().queue();
	}
	
	private void getContentFromUbuntu(String url, String messageId) {
		String msg = HasteUtil.getCrashFromUbuntuPaste(url);
		HasteUtil.paste(msg);
		Solution foundSolution = Parser.find(msg);
		boolean unsupported = Parser.checkVersion(msg);
		System.out.println("Supported: " + unsupported);
		EmbedBuilder embed;
		
		String name = HasteUtil.getLink().toString();
		
		if(!unsupported) {
			embed = Embeds.crashlogNotSupportedEmbed(channel, message, name, Parser.version);
			sendEmbed(channel, embed);
		} else {
			if(foundSolution != null) {
				embed = Embeds.crashlogWithSolutionEmbed(channel, message, name, foundSolution.getReply());
				sendEmbed(channel, embed);
			} else {
				embed = Embeds.crashlogOnlyEmbed(channel, message, name);
				sendEmbed(channel, embed);
			}
		}
		this.event.getMessage().delete().queue();
	}
}
