package dev.tomwmth.troytrack.command.base;

import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.troytrack.command.base.annotation.*;
import dev.tomwmth.troytrack.command.base.argument.*;
import dev.tomwmth.troytrack.util.CollectionUtils;
import dev.tomwmth.troytrack.util.object.Pair;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/19/23
 */
@Getter
public class CommandRegistry {
    private static final Map<Class<?>, OptionType> OPTION_TYPES = CollectionUtils.map(
            String.class, OptionType.STRING,
            Integer.class, OptionType.INTEGER,
            int.class, OptionType.INTEGER,
            Long.class, OptionType.INTEGER,
            long.class, OptionType.INTEGER,
            Short.class, OptionType.INTEGER,
            short.class, OptionType.INTEGER,
            Byte.class, OptionType.INTEGER,
            byte.class, OptionType.INTEGER,
            Double.class, OptionType.NUMBER,
            double.class, OptionType.NUMBER,
            Float.class, OptionType.NUMBER,
            float.class, OptionType.NUMBER,
            Boolean.class, OptionType.BOOLEAN,
            boolean.class, OptionType.BOOLEAN,
            User.class, OptionType.USER,
            Channel.class, OptionType.CHANNEL,
            Role.class, OptionType.ROLE,
            IMentionable.class, OptionType.MENTIONABLE
    );

    private final List<Command> commands;

    private final Map<String, RegisteredSlashCommand> commandFunctions = new HashMap<>();
    private final Map<String, RegisteredContextCommand> contextFunctions = new HashMap<>();

    public CommandRegistry(@NotNull List<Command> commands) {
        this.commands = commands;

        for (var command : this.commands) {
            if (!command.getClass().isAnnotationPresent(SlashCommand.class))
                continue;

            var clazz = command.getClass();
            var annotation = clazz.getAnnotation(SlashCommand.class);
            var name = annotation.value().isEmpty() ? clazz.getSimpleName().replace("Command", "") : annotation.value();
            var commandData = Commands.slash(name, annotation.description().isEmpty() ? "No description set" : annotation.description());
            var subcommandGroups = new ArrayList<SubcommandGroupData>();

            // Update the command
            command.setCommandData(commandData);

            // Register Subcommands
            for (var method : command.getClass().getMethods()) {
                if (method.isAnnotationPresent(SlashCommand.class)) {
                    var options = this.parse(method);
                    if (options != null) {
                        commandData.addOptions(options.stream().map(Pair::getFirst).toList());
                        this.commandFunctions.put(name, new RegisteredSlashCommand(method, options.stream().map(Pair::getSecond).toList()));
                    }
                } else if (method.isAnnotationPresent(SubCommand.class)) {
                    var subcommand = method.getAnnotation(SubCommand.class);
                    var options = this.parse(method);
                    if (options == null)
                        continue;

                    var scName = subcommand.name().isEmpty() ? method.getName().toLowerCase() : subcommand.name();
                    var scDescription = subcommand.description().isEmpty() ? "No description set" : subcommand.description();
                    var data = new SubcommandData(scName, scDescription).addOptions(options.stream().map(Pair::getFirst).toList());
                    var group = name + ".";
                    if (!subcommand.group().isEmpty()) {
                        var groupName = subcommand.group();
                        var groupDescription = subcommand.groupDescription().isEmpty() ? "No description set" : subcommand.groupDescription();
                        var subcommandGroup = subcommandGroups.stream().filter(subcommandGroupData -> subcommandGroupData.getName().equals(groupName)).findFirst();
                        if (subcommandGroup.isPresent())
                            subcommandGroup.get().addSubcommands(data);
                        else
                            subcommandGroups.add(new SubcommandGroupData(groupName, groupDescription).addSubcommands(data));

                        group += groupName + "." + scName;
                    } else
                        group += scName;
                    commandData.addSubcommands(data);
                    this.commandFunctions.put(group, new RegisteredSlashCommand(method, options.stream().map(Pair::getSecond).toList()));
                } else if (method.isAnnotationPresent(Context.class)) {
                    var parameters = method.getParameters();
                    if (parameters.length == 0)
                        continue;

                    var param = parameters[0].getType();
                    if (param != GenericContextInteractionEvent.class && param != UserContextInteractionEvent.class && param != MessageContextInteractionEvent.class) {
                        Reference.LOGGER.warn("Context handler {} in class {} must have parameter of GenericContextInteractionEvent",
                                method.getName(), clazz.getName());
                        continue;
                    }

                    var context = method.getAnnotation(Context.class);
                    var type = param == UserContextInteractionEvent.class || parameters.length > 1 && parameters[1].getType() == User.class ? net.dv8tion.jda.api.interactions.commands.Command.Type.USER : net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE;
                    var contextName = context.value().isEmpty() ? method.getName() : context.value();
                    this.contextFunctions.put(contextName, new RegisteredContextCommand(command, method, type));
                    command.contextCommands.add(type == net.dv8tion.jda.api.interactions.commands.Command.Type.USER ? Commands.user(contextName) : Commands.message(contextName));
                }
            }

            // Register the subcommands
            if (!subcommandGroups.isEmpty())
                commandData.addSubcommandGroups(subcommandGroups);

            // Handle permissions
            if (clazz.isAnnotationPresent(Permissions.class))
                commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(clazz.getAnnotation(Permissions.class).value()));
        }
    }

    @Nullable
    private List<Pair<OptionData, Argument>> parse(@NotNull Method method) {
        var parameters = method.getParameters();
        if (parameters.length == 0)
            return null;

        if (parameters[0].getType() != SlashCommandInteractionEvent.class) {
            Reference.LOGGER.warn("Slash command handler {} in class {} must have parameter of ButtonInteractionEvent",
                    method, method.getClass().getName());
            return null;
        }

        var options = new LinkedList<Pair<OptionData, Argument>>();

        if (parameters.length > 1)
            for (var i = 1; i < parameters.length; i++) {
                var parameter = parameters[i];
                if (!parameter.isAnnotationPresent(Option.class))
                    return null;

                var defaultValue = parameter.isAnnotationPresent(Default.class) ? parameter.getAnnotation(Default.class).value() : "";
                var option = parameter.getAnnotation(Option.class);
                var type = parameter.getType();
                var name = (option.name().isEmpty() ? parameter.getName() : option.name()).toLowerCase();
                var description = option.desc().isEmpty() ? "No description set" : option.desc();
                var required = option.required();

                if (OPTION_TYPES.containsKey(type)) {
                    var optionType = OPTION_TYPES.get(type);
                    var data = new OptionData(optionType, name, description, required);
                    if (optionType == OptionType.STRING && option.choices().length > 0) {
                        data.addChoices(Stream.of(option.choices()).map(e -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(e.toLowerCase(), e)).toList());
                    }
                    if (optionType == OptionType.INTEGER && option.min() != option.max()) {
                        data.setMinValue(option.min());
                        data.setMaxValue(option.max());
                    }

                    if (type == String.class)
                        options.add(Pair.of(data, new StringArgument(name, defaultValue)));
                    else if (type == Integer.class || type == int.class)
                        options.add(Pair.of(data, new IntegerArgument(name, defaultValue)));
                    else if (type == Long.class || type == long.class)
                        options.add(Pair.of(data, new LongArgument(name, defaultValue)));
                    else if (type == Short.class || type == short.class)
                        options.add(Pair.of(data, new ShortArgument(name, defaultValue)));
                    else if (type == Byte.class || type == byte.class)
                        options.add(Pair.of(data, new ByteArgument(name, defaultValue)));
                    else if (type == Float.class || type == float.class)
                        options.add(Pair.of(data, new FloatArgument(name, defaultValue)));
                    else if (type == Double.class || type == double.class)
                        options.add(Pair.of(data, new DoubleArgument(name, defaultValue)));
                    else if (type == Boolean.class || type == boolean.class)
                        options.add(Pair.of(data, new BooleanArgument(name, defaultValue)));
                    else if (type == Channel.class)
                        options.add(Pair.of(data, new ChannelArgument(name)));
                    else if (type == Role.class)
                        options.add(Pair.of(data, new RoleArgument(name)));
                    else if (type == User.class)
                        options.add(Pair.of(data, new UserArgument(name)));
                    else if (type == IMentionable.class)
                        options.add(Pair.of(data, new MentionableArgument(name)));
                } else if (type.isEnum()) {
                    var data = new OptionData(OptionType.STRING, name, description, required);
                    data.addChoices(Stream.of(type.getEnumConstants()).map(e -> {
                        var enumeration = (Enum<?>) e;
                        return new net.dv8tion.jda.api.interactions.commands.Command.Choice(enumeration.toString(), enumeration.name().toLowerCase());
                    }).toList());
                    options.add(Pair.of(data, new EnumArgument(name, (Class<Enum<?>>) type, defaultValue)));
                } else {
                    Reference.LOGGER.warn("Slash command option `{}` on command `{}` in class {} is an invalid type",
                            name, method.getName(), method.getDeclaringClass().getName());
                }
            }

        return options;
    }

    @Nullable
    public Command getCommand(@NotNull String name) {
        for (var i = 0; i < this.commands.size(); i++) {
            var command = this.commands.get(i);
            if (command.getName().equalsIgnoreCase(name))
                return command;
        }
        return null;
    }

    public void updateGlobalCommands(@NotNull JDA bot) {
        bot.updateCommands()
                .addCommands(this.commands.stream().filter(cmd -> !cmd.isGuildOnly()).map(Command::getCommandData).toList())
                .queue();
    }

    public void updateGuildCommands(@NotNull Guild guild) {

        var jda = guild.getJDA();

        guild.retrieveCommands().queue(commands -> {
            // Only update commands which were changed
            var changed = false;

            // Remove commands from other applications
            commands = new ArrayList<>(commands);
            commands.removeIf(cmd -> cmd.getApplicationIdLong() != jda.getSelfUser().getApplicationIdLong());

            // Check existing commands
            for (var discordCommand : commands) {

                var alpineCommand = this.getCommand(discordCommand.getName());

                // Command was deleted
                if (alpineCommand == null) {
                    // Command does not exist anymore, delete the command
                    guild.deleteCommandById(discordCommand.getIdLong()).queue();
                    changed = true;
                    break;
                }

                // If command is not a valid guild command, continue
                if (!alpineCommand.isGuildOnly())
                    continue;

                // Check slash command
                if (this.updateCommand(discordCommand, alpineCommand.getCommandData())) {
                    changed = true;
                    break;
                }

                // Check context commands
                if (alpineCommand.getContextCommands().stream().anyMatch(ctx -> this.updateCommand(discordCommand, ctx))) {
                    changed = true;
                    break;
                }
            }

            // Add non-existent commands
            for (var command : this.commands) {
                if (command.isGuildOnly()) {
                    var commandData = command.getCommandData();
                    if (commands.stream().noneMatch(cmd -> cmd.getName().equalsIgnoreCase(commandData.getName()) && cmd.getType() == commandData.getType())) {
                        changed = true;
                        break;
                    }

                    // Check context commands
                    for (var context : command.getContextCommands()) {
                        if (commands.stream().noneMatch(cmd -> cmd.getName().equals(context.getName()) && cmd.getType() == context.getType())) {
                            changed = true;
                            break;
                        }
                    }
                }
            }

            if (changed) {
                Reference.LOGGER.info("Executing update to slash commands");
                var update = guild.updateCommands();
                for (var command : this.commands) {
                    update = update.addCommands(command.getCommandData());
                    update = update.addCommands(command.getContextCommands());
                }

                update.queue();
            }
        }, ex -> {
            Reference.LOGGER.error("Unable to fetch guild commands, updating all commands", ex);

            // No commands or error, just update all commands
            var update = guild.updateCommands();

            for (var command : this.commands) {
                if (!command.isGuildOnly())
                    continue;
                update = update.addCommands(command.getCommandData());
                update = update.addCommands(command.getContextCommands());
            }

            update.queue();
        });
    }

    private boolean updateCommand(@NotNull net.dv8tion.jda.api.interactions.commands.Command command, @NotNull CommandData commandData) {
        if (command.getName().equals(commandData.getName()) && command.getType() == commandData.getType()) {
            if (commandData instanceof SlashCommandData data) {
                var opts = data.getOptions();
                for (var option : command.getOptions()) {
                    if (opts.stream().noneMatch(opt -> opt.getName().equals(option.getName()))) {
                        return true;
                    }

                    var opt = opts.stream().filter(o -> o.getName().equals(option.getName())).findFirst().get();
                    if (!Objects.equals(OptionData.fromOption(option).toData().toString(), opt.toData().toData().toString())) {
                        return true;
                    }
                }
            } else {
                return !commandData.getName().equals(command.getName()) || commandData.getType() != command.getType();
            }
        }

        return false;
    }
}
