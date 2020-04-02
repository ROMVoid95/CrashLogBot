package net.romvoid.crashbot.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.commands.inerf.Command;
import net.romvoid.crashbot.utilities.EmbedUtil;

public class GithubCommand extends Command {

	public GithubCommand() {
		super("github");
		addAlias("git");
		addAlias("source");
	}

	@Override
	public void executeAndHandle(GuildMessageReceivedEvent event, List<String> params, User author, Message inputMessage) {
		MessageChannel channel = event.getChannel();
		EmbedBuilder embed = EmbedUtil.embed("CrashBot Github",
				"Have an issue? Or want to contribute? Head on over to my Github Repo!");
		embed.addField("Github Link", "[Github Repository](https://github.com/ROMVoid95/CrashBot)", false);
		Message msg = EmbedUtil.message(embed);
		EmbedUtil.sendAndDeleteOnGuilds(channel, msg, 2, TimeUnit.MINUTES);

	}

	@Override
	public String getDescription() {
		return null;
	}

}
