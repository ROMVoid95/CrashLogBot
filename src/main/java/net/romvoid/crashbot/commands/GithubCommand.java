package net.romvoid.crashbot.commands;

import java.util.List;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.commands.inerf.Command;

public class GithubCommand extends Command {

	public GithubCommand() {
        super("github");
        addAlias("git");
        addAlias("source");	}

	@Override
	public void execute(GuildMessageReceivedEvent event, List<String> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
