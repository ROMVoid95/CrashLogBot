package net.romvoid.crashbot.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManager extends InterfacedEventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManager.class);
    private long lastJdaEvent;

    public static Logger getLog() {
        return log;
    }
    
    @Override
    public void handle(@NotNull GenericEvent event) {
        lastJdaEvent = System.currentTimeMillis();
        super.handle(event);
    }

    public long lastJDAEvent() {
        return lastJdaEvent;
    }

    public long lastJDAEventDiff() {
        return System.currentTimeMillis() - lastJDAEvent();
    }
}
