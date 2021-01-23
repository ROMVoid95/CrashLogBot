package net.romvoid.crashbot.core.modules.commands;

import static net.romvoid.crashbot.utilities.StringUtils.splitArgs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.romvoid.crashbot.core.modules.commands.base.AbstractCommand;
import net.romvoid.crashbot.core.modules.commands.base.Command;
import net.romvoid.crashbot.core.modules.commands.base.CommandCategory;
import net.romvoid.crashbot.core.modules.commands.base.CommandPermission;
import net.romvoid.crashbot.core.modules.commands.base.Context;
import net.romvoid.crashbot.core.modules.commands.base.ITreeCommand;
import net.romvoid.crashbot.utilities.commands.EmoteReference;

public abstract class SimpleTreeCommand extends AbstractCommand implements ITreeCommand {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private Predicate<Context> predicate = event -> true;

    public SimpleTreeCommand(CommandCategory category) {
        super(category);
    }

    public SimpleTreeCommand(CommandCategory category, CommandPermission permission) {
        super(category, permission);
    }

    /**
     * Invokes the command to be executed.
     *
     * @param context     the context of the event that triggered the command
     * @param commandName the command name that was used
     * @param content     the arguments of the command
     */
    @Override
    public void run(Context context, String commandName, String content) {
        var args = splitArgs(content, 2);

        if (subCommands.isEmpty()) {
            throw new IllegalArgumentException("No subcommands registered!");
        }

        var command= subCommands.get(args[0]);

        if (command == null) {
            defaultTrigger(context, commandName, args[0]);
            return;
        }

        if (!predicate.test(context)) {
            return;
        }

        command.run(new Context(context.getEvent(), args[1], context.isMentionPrefix()), commandName + " " + args[0], args[1]);
    }

    public void setPredicate(Predicate<Context> predicate) {
        this.predicate = predicate;
    }

    public SimpleTreeCommand addSubCommand(String name, String description, BiConsumer<Context, String> command) {
        subCommands.put(name, new SubCommand() {
            @Override
            public String description() {
                return description;
            }

            @Override
            protected void call(Context context, String content) {
                command.accept(context, content);
            }
        });

        return this;
    }

    public SimpleTreeCommand addSubCommand(String name, BiConsumer<Context, String> command) {
        return addSubCommand(name, null, command);
    }

    public SimpleTreeCommand addSubCommand(String name, SubCommand command) {
        subCommands.put(name, command);
        return this;
    }

    @Override
    public SimpleTreeCommand createSubCommandAlias(String name, String alias) {
        var cmd = subCommands.get(name);
        if (cmd == null) {
            throw new IllegalArgumentException("Cannot create an alias of a non-existent sub command!");
        }

        //Creates a fully new instance. Without this, it'd be dependant on the original instance, and changing the child status would change it's parent's status too.
        var clone = SubCommand.copy(cmd);
        clone.setChild(true);
        subCommands.put(alias, clone);

        return this;
    }

    @Override
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }

    /**
     * Handling for when the Sub-Command isn't found.
     *
     * @param ctx         the context of the event that triggered the command
     * @param commandName the Name of the not-found command.
     */
    public Command defaultTrigger(Context ctx, String mainCommand, String commandName) {
        //why?
        if (commandName.isEmpty()) {
            commandName = "none";
        }

        ctx.sendStripped(String.format(
                "%1$sNo subcommand `%2$s` found in the `%3$s` command!. Check `~>help %3$s` for available subcommands",
                EmoteReference.ERROR, commandName, mainCommand)
        );

        return null;
    }
}
