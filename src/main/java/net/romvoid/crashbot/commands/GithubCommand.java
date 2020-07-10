package net.romvoid.crashbot.commands;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.romvoid.crashbot.utilities.EmbedUtil;

public class GithubCommand extends Command {

	public GithubCommand() {
		this.name = "github";
        this.help = "Returns the github repository for the bot";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent event) {
		MessageChannel channel = event.getChannel();
		EmbedBuilder embed = EmbedUtil.embed("CrashBot Github",
				"Have an issue? Or want to contribute? Head on over to my Github Repo!");
		embed.addField("Github Link", "[Github Repository](https://github.com/ROMVoid95/CrashBot)", false);
		Message msg = EmbedUtil.message(embed);
		EmbedUtil.sendAndDeleteOnGuilds(channel, msg, 2, TimeUnit.MINUTES);
	}

}
