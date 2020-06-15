package net.romvoid.crashbot.hastebin;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.romvoid.crashbot.commands.json.CommonError;
import net.romvoid.crashbot.commands.json.CommonError.Deserializer;

public class NewFileListener  extends ListenerAdapter {

	private static final Gson GSON = new GsonBuilder().registerTypeAdapter(CommonError.class, new CommonError.Deserializer()).create();

	/**
	 * On message received.
	 *
	 * @param event the onMessageReceived event
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		List<Attachment> list = new ArrayList<Attachment>();
		list.addAll(event.getMessage().getAttachments());
		list.forEach(ee -> {
			if (isValidFile(ee)) {
				getFileContent(event);
			}
		});

	}

	/**
	 * Checks if is valid file. This checks if the file has the extensions .txt or .log
	 * which are common Minecraft crash-log and log file extensions.
	 *
	 * @param attachment the attachment
	 * @return true, if is valid file
	 */
	private static boolean isValidFile(Message.Attachment attachment) {
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
	 * @param channel the channel
	 * @param message the message
	 * @param filename the filename
	 * @param url the URL
	 * @return the embed builder
	 */
	private static EmbedBuilder makeEmbed(TextChannel channel, Message message, String filename, String url) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.cyan);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setFooter("Crash-Log Uploading Service for" + channel.getGuild().getName());
		embedBuilder.addField(user + "'s Crash Log", "[" + filename + "](" + url + ")", false);
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;

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
			StringBuilder builder = new StringBuilder();
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

			String s = builder.toString();
			try {
				URL URL = new URL("https://raw.githubusercontent.com/ROMVoid95/CrashBot/master/common_errors.json");
				HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
				connection.setDoOutput(true);
				connection.setConnectTimeout(0);
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            String paste = "";
	            while (reader.ready()) {
	                String line = reader.readLine();
	                if (paste.equals("")) paste = line;
	                else paste = paste + "\n" + line;
	            }
				System.out.println(paste);
				//System.out.println(GSON.fromJson(b.toString(), CommonError.class));
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			String hasteString = Hastebin.paste(s);
			try {
				event.getChannel().sendMessage(makeEmbed(channel, message, name, new URI(hasteString) + ".yml").build())
						.queue();
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
