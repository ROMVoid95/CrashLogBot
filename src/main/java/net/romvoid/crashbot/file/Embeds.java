package net.romvoid.crashbot.file;

import java.awt.Color;
import java.net.URI;
import java.time.Instant;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Embeds {
	
	private static final String SOLUTION_FOOTER = "Solutions are provided on a best-attempt basis and are not guaranteed";

	/**
	 * Make crashlogWithSolutionEmbed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @param fix      the fix
	 * @return the embed builder
	 */
	public static EmbedBuilder crashlogWithSolutionEmbed(TextChannel channel, Message message, String filename, URI url, String fix) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setDescription("`Links do not expire`");
		embedBuilder.addField(user + "'s Crash Log", "[" + filename + "](" + url + ")", false);
		embedBuilder.addField("Possible Fixes", "`" + fix + "`", false);
		embedBuilder.setFooter(SOLUTION_FOOTER + "\nCrash-Log Service for " + channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}
	
	/**
	 * Make crashlogOnlyEmbed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @param fix      the fix
	 * @return the embed builder
	 */
	public static EmbedBuilder crashlogOnlyEmbed(TextChannel channel, Message message, String filename, URI url) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.cyan);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setDescription("`Links do not expire`");
		embedBuilder.addField(user + "'s Crash Log", "[" + filename + "](" + url + ")", false);
		embedBuilder.setFooter("Crash-Log Service for " + channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}
	
	/**
	 * Make crashlogNotSupportedEmbed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @param version  the version
	 * @return the embed builder
	 */
	public static EmbedBuilder crashlogNotSupportedEmbed(TextChannel channel, Message message, String filename, URI url, String version) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.RED);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setDescription("`Links do not expire`");
		embedBuilder.addField(user + "'s Crash Log", "[" + filename + "](" + url + ")", false);
		embedBuilder.addField("Version Not Supported","`" + version + "` is no longer supported."
				+ "\nYour URL is still provided in the case any community members may know a fix", false);
		embedBuilder.setFooter(channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}
	
	/**
	 * Make crashlogWithSolutionEmbed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @param fix      the fix
	 * @return the embed builder
	 */
	public static EmbedBuilder crashlogWithSolutionEmbed(TextChannel channel, Message message, String url, String fix) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setDescription("`Links do not expire`");
		embedBuilder.addField(user + "'s Crash Log", "[" + url + "](" + url + ")", false);
		embedBuilder.addField("Possible Fixes", "`" + fix + "`", false);
		embedBuilder.setFooter(SOLUTION_FOOTER + "\nCrash-Log Service for " + channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}
	
	/**
	 * Make crashlogOnlyEmbed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @param fix      the fix
	 * @return the embed builder
	 */
	public static EmbedBuilder crashlogOnlyEmbed(TextChannel channel, Message message, String url) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.cyan);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setDescription("`Links do not expire`");
		embedBuilder.addField(user + "'s Crash Log", "[" + url + "](" + url + ")", false);
		embedBuilder.setFooter("Crash-Log Service for " + channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}
	
	/**
	 * Make crashlogNotSupportedEmbed.
	 *
	 * @param channel  the channel
	 * @param message  the message
	 * @param filename the filename
	 * @param url      the URL
	 * @param version  the version
	 * @return the embed builder
	 */
	public static EmbedBuilder crashlogNotSupportedEmbed(TextChannel channel, Message message, String url, String version) {
		String user = message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.RED);
		embedBuilder.setTitle("Crash Report Utility");
		embedBuilder.setDescription("`Links do not expire`");
		embedBuilder.addField(user + "'s Crash Log", "[" + url + "](" + url + ")", false);
		embedBuilder.addField("Version Not Supported","`" + version + "` is no longer supported."
				+ "\nYour URL is still provided in the case any community members may know a fix", false);
		embedBuilder.setFooter(channel.getGuild().getName());
		embedBuilder.setTimestamp(Instant.now());
		return embedBuilder;
	}
	
}
