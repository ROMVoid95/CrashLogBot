package net.romvoid.crashbot.commands;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.romvoid.crashbot.Bot;
import net.romvoid.crashbot.utilities.EmbedUtil;

public class InviteCommand extends Command {

	public InviteCommand() {
		this.name = "invite";
		this.help = "Generates an invite link for the bot";
		this.guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent event) {
		Bot.LOG.info(event.getAuthor().getName() + " invoked command " + this.name);
		MessageChannel channel = event.getChannel();
		String inviteURL = Bot.getJDA().getInviteUrl() + "&permissions=388177";
		if (event.getArgs().isEmpty()) {
			EmbedBuilder embed = EmbedUtil.embed("Invite Me To Your Server",
					"Want to use this bot in your server also? No problem! heres your invite link");
			embed.addField("Invite Link", "[Invite Me](" + inviteURL + ")", false);
			Message msg = EmbedUtil.message(embed);
			EmbedUtil.sendAndDeleteOnGuilds(channel, msg, 2, TimeUnit.MINUTES);
		} else {
			String[] args = event.getArgs().split("\\s+");
			if (args.length >= 2) {
				event.replyWarning("You can only define 1 serverId per command!");
			} else {
				String specialInvite = inviteURL + "&guild_id=" + args[0];
				EmbedBuilder embed = EmbedUtil.embed("Invite Me To Your Server",
						"Want to use this bot in your server also? No problem! heres your invite link");
				embed.addField("Here is your direct link to invite the bot", "[Invite Me](" + specialInvite + ")",
						false);
				Message msg = EmbedUtil.message(embed);
				EmbedUtil.sendAndDeleteOnGuilds(channel, msg, 2, TimeUnit.MINUTES);
			}
		}
		event.getMessage().delete().queue();
	}

}
