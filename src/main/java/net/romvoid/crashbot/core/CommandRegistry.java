package net.romvoid.crashbot.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.romvoid.crashbot.core.command.CommandManager;
import net.romvoid.crashbot.core.command.NewCommand;
import net.romvoid.crashbot.core.command.NewContext;
import net.romvoid.crashbot.core.command.argument.ArgumentParseError;
import net.romvoid.crashbot.core.modules.commands.AliasCommand;
import net.romvoid.crashbot.core.modules.commands.base.Command;
import net.romvoid.crashbot.core.modules.commands.base.CommandCategory;
import net.romvoid.crashbot.core.modules.commands.base.CommandPermission;
import net.romvoid.crashbot.core.modules.commands.base.Context;
import net.romvoid.crashbot.core.modules.commands.help.HelpContent;
import net.romvoid.crashbot.data.CrashBotData;
import net.romvoid.crashbot.db.entities.DBGuild;
import net.romvoid.crashbot.db.entities.helpers.GuildData;
import net.romvoid.crashbot.utilities.Commons;
import net.romvoid.crashbot.utilities.commands.EmoteReference;
import net.romvoid.crashbot.utilities.commands.RateLimiter;

public class CommandRegistry {
	private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);
    private final Map<String, Command> commands;
    private final CommandManager newCommands = new CommandManager();
    private final RateLimiter rl = new RateLimiter(TimeUnit.MINUTES, 1);

    public CommandRegistry(Map<String, Command> commands) {
        this.commands = Preconditions.checkNotNull(commands);
    }

    public CommandRegistry() {
        this(new HashMap<>());
    }

    public Map<String, Command> commands() {
        return commands;
        
        
    }

    public Map<String, Command> getCommandsForCategory(CommandCategory category) {
        return commands.entrySet().stream()
                .filter(cmd -> cmd.getValue().category() == category)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void process(GuildMessageReceivedEvent event, DBGuild dbGuild, String cmdName, String content, String prefix, boolean isMention) {
        final var managedDatabase = CrashBotData.db();
        var command = commands.get(cmdName.toLowerCase());
        var guildData = dbGuild.getData();

        if (command == null) {
            return;
        }

        final var author = event.getAuthor();
        final var channel = event.getChannel();
        // Variable used in lambda expression should be final or effectively final...
        final var cmd = command;

        if (managedDatabase.getCrashBotData().getBlackListedUsers().contains(author.getId())) {
            if (!rl.process(author)) {
                return;
            }

            channel.sendMessage("""
                    :x: You have been blacklisted from using all of Mantaro's functions, likely for botting or hitting the spam filter.
                    If you wish to get more details on why, or appeal, don't hesitate to join the support server and ask, but be sincere.
                    """
            ).queue();
            return;
        }

        final var member = event.getMember();
        final var guild = event.getGuild();
        final var channelDisabledCommands = guildData.getChannelSpecificDisabledCommands().get(channel.getId());
        if (channelDisabledCommands != null && channelDisabledCommands.contains(name(cmd, cmdName))) {
            sendDisabledNotice(event, guildData, CommandDisableLevel.COMMAND_SPECIFIC);
            return;
        }

        if (guildData.getDisabledUsers().contains(author.getId()) && isNotAdmin(member)) {
            sendDisabledNotice(event, guildData, CommandDisableLevel.USER);
            return;
        }

        var isOptions = cmdName.equalsIgnoreCase("opts");
        if (guildData.getDisabledChannels().contains(channel.getId()) && !isOptions) {
            sendDisabledNotice(event, guildData, CommandDisableLevel.CHANNEL);
            return;
        }

        if (!cmd.permission().test(member)) {
            channel.sendMessage(EmoteReference.STOP + "You have no permissions to trigger this command :(").queue();
            return;
        }
        // !! Permission check end

        // Used a command on the new system?
        // sort-of-fix: remove if statement when we port all commands
        boolean executedNew;
        try {
            executedNew = newCommands.execute(new NewContext(event.getMessage(),
                    event.getMessage().getContentRaw().substring(prefix.length()))
            );
        } catch (ArgumentParseError e) {
            if (e.getMessage() != null) {
                channel.sendMessage(EmoteReference.ERROR + e.getMessage()).queue();
            } else {
                e.printStackTrace();
                channel.sendMessage(
                        EmoteReference.ERROR + "There was an error parsing the arguments for this command. Please report this to the developers"
                ).queue();
            }

            return;
        }

        if (!executedNew) {
            cmd.run(new Context(event, content, isMention), cmdName, content);
        }

        log.debug("!! COMMAND INVOKE: command:{}, user:{}, guild:{}, channel:{}",
                cmdName, author.getAsTag(), guild.getId(), channel.getId()
        );
    }

    public void register(Class<? extends NewCommand> clazz) {
        var cmd = newCommands.register(clazz);
        var p = new ProxyCommand(cmd);
        commands.put(cmd.name(), p);
        cmd.aliases().forEach(a -> commands.put(a, new AliasProxyCommand(p)));
    }

    public <T extends Command> T register(String name, T command) {
        commands.putIfAbsent(name, command);
        log.debug("Registered command " + name);
        return command;
    }

    public void registerAlias(String command, String alias) {
        if (!commands.containsKey(command)) {
            log.error(command + " isn't in the command map...");
        }

        Command parent = commands.get(command);
        if (parent instanceof ProxyCommand) {
            throw new IllegalArgumentException("Use @Alias instead");
        }
        parent.getAliases().add(alias);

        register(alias, new AliasCommand(alias, command, parent));
    }

    private boolean isNotAdmin(Member member) {
        return !CommandPermission.ADMIN.test(member);
    }

    public void sendDisabledNotice(GuildMessageReceivedEvent event, GuildData data, CommandDisableLevel level) {
        if (level != CommandDisableLevel.NONE) {
            event.getChannel().sendMessageFormat("%sThis command is disabled on this server. Reason: %s",
                    EmoteReference.ERROR, Commons.capitalize(level.getName())
            ).queue();
        } // else don't
    }

    private static String name(Command c, String userInput) {
        if (c instanceof AliasCommand) {
            // Return the original command name here for all intents and purposes.
            // This is because in the check for command disable (which is what this is used for), the
            // command disabled will be the original command, and the check expects that.
            return ((AliasCommand) c).getOriginalName();
        }

        if (c instanceof ProxyCommand) {
            return ((ProxyCommand) c).c.name();
        }

        return userInput.toLowerCase();
    }

    private static class ProxyCommand implements Command {
        private final NewCommand c;

        private ProxyCommand(NewCommand c) {
            this.c = c;
        }

        @Override
        public CommandCategory category() {
            return c.category();
        }

        @Override
        public CommandPermission permission() {
            return c.permission();
        }

        @Override
        public void run(Context context, String commandName, String content) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HelpContent help() {
            return c.help();
        }
        
        @Override
        public List<String> getAliases() {
            return c.aliases();
        }
    }

    private static class AliasProxyCommand extends ProxyCommand {
        @SuppressWarnings("unused")
		private final ProxyCommand p;
        private AliasProxyCommand(ProxyCommand p) {
            super(p.c);
            this.p = p;
        }

        @Override
        public CommandCategory category() {
            return null;
        }
    }

    enum CommandDisableLevel {
        NONE("None"),
        CATEGORY("Disabled category on server"),
        SPECIFIC_CATEGORY("Disabled category on specific channel"),
        COMMAND("Disabled command"),
        COMMAND_SPECIFIC("Disabled command on specific channel"),
        GUILD("Disabled command on this server"),
        ROLE("Disabled role on this server"),
        ROLE_CATEGORY("Disabled role for this category in this server"),
        SPECIFIC_ROLE("Disabled role for this command in this server"),
        SPECIFIC_ROLE_CATEGORY("Disabled role for this category in this server"),
        CHANNEL("Disabled channel"),
        USER("Disabled user");

        final String name;

        CommandDisableLevel(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
