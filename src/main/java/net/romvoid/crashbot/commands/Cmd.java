package net.romvoid.crashbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.romvoid.crashbot.Bot;

public abstract class Cmd extends Command {

	protected abstract void execute(CommandEvent event);
	
	void log(CommandEvent event) {
		Bot.LOG.info(event.getAuthor().getName() + " invoked command " + this.name);
	}

}
