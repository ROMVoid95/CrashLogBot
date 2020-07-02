/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 ROMVoid
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.romvoid.crashbot;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * The listener interface for receiving message events.
 * The class that is interested in processing a message
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addMessageListener<code> method. When
 * the message event occurs, that object's appropriate
 * method is invoked.
 *
 * @see MessageEvent
 */
public class MessageListener extends ListenerAdapter {

    /**
     * On guild message received.
     *
     * @param event the event
     */
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot())
            return;

        if(messageContainsPrefix(event))
            Bot.getInstance().handleCommandEvent(event);

    }

    /**
     * Returns true if the message contains the bots prefix.
     * TODO: Create a per-guild prefix option
     *
     * @param event the message received event
     * @return true, if successful
     */
    private boolean messageContainsPrefix(GuildMessageReceivedEvent event) {
        return event.getMessage().getContentRaw().startsWith(Bot.getPrefix());
    }
}
