package net.romvoid.crashbot.core.modules.commands;

import net.romvoid.crashbot.core.modules.commands.base.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static net.romvoid.crashbot.utilities.StringUtils.splitArgs;

public abstract class TreeCommand extends AbstractCommand implements ITreeCommand {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    //By default let all commands pass.
    private Predicate<Context> predicate = event -> true;

    public TreeCommand(CommandCategory category) {
        super(category);
    }

    public TreeCommand(CommandCategory category, CommandPermission permission) {
        super(category, permission);
    }

    @Override
    public void run(Context context, String commandName, String content) {
        String[] args = splitArgs(content, 2);

        if (subCommands.isEmpty()) {
            throw new IllegalArgumentException("No subcommands registered!");
        }

        Command command = subCommands.get(args[0]);
        boolean isDefault = false;
        if (command == null) {
            command = defaultTrigger(context, commandName, content);
            isDefault = true;
        }
        if (command == null)
            return; //Use SimpleTreeCommand then?

        var ct = isDefault ? content : args[1];

        if (!predicate.test(context)) return;

        command.run(new Context(context.getEvent(),  ct, context.isMentionPrefix()),
                commandName + (isDefault ? "" : " " + args[0]), ct
        );
    }

    public TreeCommand addSubCommand(String name, BiConsumer<Context, String> command) {
        subCommands.put(name, new SubCommand() {
            @Override
            protected void call(Context context, String content) {
                command.accept(context, content);
            }
        });
        return this;
    }

    public void setPredicate(Predicate<Context> predicate) {
        this.predicate = predicate;
    }

    @Override
    public TreeCommand createSubCommandAlias(String name, String alias) {
        SubCommand cmd = subCommands.get(name);
        if (cmd == null) {
            throw new IllegalArgumentException("Cannot create an alias of a non-existent sub command!");
        }

        //Creates a fully new instance. Without this, it'd be dependant on the original instance, and changing the child status would change it's parent's status too.
        SubCommand clone = SubCommand.copy(cmd);
        clone.setChild(true);
        subCommands.put(alias, clone);

        return this;
    }

    @Override
    public ITreeCommand addSubCommand(String name, SubCommand command) {
        subCommands.put(name, command);
        return this;
    }

    @Override
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }
}
