package net.romvoid.crashbot.core.opts.core;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

@FunctionalInterface
public interface InteractiveOpt extends Operation {
	int run(GuildMessageReceivedEvent event);
}
