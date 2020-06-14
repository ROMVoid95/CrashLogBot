package net.romvoid.crashbot.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.Bot;
import net.romvoid.crashbot.commands.inerf.Command;
import net.romvoid.crashbot.utilities.EmbedUtil;

public class InviteCommand extends Command {

	public InviteCommand() {
		super("invite");
		addAlias("inv");
	}

	@Override
	public void executeAndHandle(GuildMessageReceivedEvent event, List<String> params, User author,
			Message inputMessage) {
		MessageChannel channel = event.getChannel();
		String inviteURL = Bot.getJDA().getInviteUrl() + "&permissions=388177";

		EmbedBuilder embed = EmbedUtil.embed("Invite Me To Your Server",
				"Want to use this bot in your server also? No problem! heres your invite link");
		embed.addField("Invite Link", "[Invite Me](" + inviteURL + ")", false);
		Message msg = EmbedUtil.message(embed);
		EmbedUtil.sendAndDeleteOnGuilds(channel, msg, 2, TimeUnit.MINUTES);

	}

	@Override
	public String getDescription() {
		return "Generates an invite link for the bot";
	}

}
