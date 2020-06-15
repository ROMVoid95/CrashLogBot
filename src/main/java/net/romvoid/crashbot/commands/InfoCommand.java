package net.romvoid.crashbot.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.commands.inerf.Command;

public class InfoCommand extends Command {

	public InfoCommand() {
		super("info");
		addAlias("about");
	}

	@Override
	public void executeAndHandle(GuildMessageReceivedEvent event, List<String> params, User author, Message inputMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
