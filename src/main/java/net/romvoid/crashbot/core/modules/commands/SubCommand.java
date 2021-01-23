package net.romvoid.crashbot.core.modules.commands;

import net.romvoid.crashbot.core.modules.commands.base.AssistedCommand;
import net.romvoid.crashbot.core.modules.commands.base.CommandPermission;
import net.romvoid.crashbot.core.modules.commands.base.Context;
import net.romvoid.crashbot.core.modules.commands.base.InnerCommand;

public abstract class SubCommand implements InnerCommand, AssistedCommand {
    public boolean child;

    private CommandPermission permission = null;

    public SubCommand() {
    }

    public SubCommand(CommandPermission permission) {
        this.permission = permission;
    }

    /**
     * Creates a copy of a SubCommand, usually to assign child status to it.
     *
     * @param original The original SubCommand to copy.
     * @return The copy of the original SubCommand, without the description.
     */
    public static SubCommand copy(SubCommand original) {
        return new SubCommand(original.permission) {
            @Override
            protected void call(Context ctx, String content) {
                original.call(ctx, content);
            }

            @Override
            public String description() {
                return null;
            }
        };
    }

    protected abstract void call(Context ctx, String content);

    @Override
    public CommandPermission permission() {
        return permission;
    }

    @Override
    public void run(Context ctx, String commandName, String content) {
        call(ctx, content);
    }

    public boolean isChild() {
        return this.child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }
}
