package net.romvoid.crashbot.utilities;

import java.util.Set;

import org.reflections.Reflections;

import net.romvoid.crashbot.commands.inerf.Command;

public class ReflectUtil {

	public static Set<Class<? extends Command>> getCommandClasses() {
		Reflections reflections = new Reflections("net.romvoid.crashbot.commands");
        Set<Class<? extends Command>> scannersSet = reflections.getSubTypesOf(Command.class);
		return scannersSet;
	}
}
