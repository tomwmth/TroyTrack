package dev.tomwmth.troytrack.listener;

import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.troytrack.listener.base.EventListener;
import dev.tomwmth.troytrack.util.EmbedUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/19/23
 */
public class CommandListener extends EventListener {
    public CommandListener(@NotNull TroyTrack bot) {
        super(bot);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var id = event.getSubcommandGroup() != null ? event.getName() + "." + event.getSubcommandGroup() + "." + event.getSubcommandName()
                : event.getSubcommandName() != null ? event.getName() + "." + event.getSubcommandName()
                : event.getName();

        var command = this.bot.getCommandRegistry().getCommand(event.getName());
        if (command == null) {
            Reference.LOGGER.warn("Unknown command received (Name: {}, User: {}/{})",
                    event.getName(), event.getUser().getName(), event.getUser().getId());
            return;
        }

        var func = this.bot.getCommandRegistry().getCommandFunctions().get(id);
        if (func == null) {
            Reference.LOGGER.warn("Unknown command function (Name: {}, User: {}/{})",
                    event.getName(), event.getUser().getName(), event.getUser().getId());
            return;
        }

        var startTime = System.currentTimeMillis();
        var guild = event.getGuild() == null ? "N/A" : event.getGuild().getId();
        var method = func.method();
        var options = func.arguments();

        var arguments = new Object[options.size() + 1];
        arguments[0] = event;

        try {
            // Parse the arguments
            for (var i = 0; i < options.size(); i++) {
                var arg = options.get(i);
                var option = event.getOption(arg.getName());
                arguments[i + 1] = option == null ? arg.getDefaultValue() : arg.assign(option);
            }

            method.invoke(command, arguments);
        }
        catch (Exception ex) {
            Throwable t = ex;
            if (ex instanceof InvocationTargetException) {
                t = ex.getCause().getCause();
            }

            Reference.LOGGER.error("Unable to execute command {} (Guild: {}, User: {}/{}, Channel: {})",
                    command.getName(), guild, event.getUser().getName(), event.getUser().getId(),
                    event.getChannel().getId(), t);

            event.getHook().editOriginalEmbeds(EmbedUtils.failure("An internal error occurred while processing this command").build()).queue();
        }

        Reference.LOGGER.info("Slash command executed in {}ms (Command: {}, Guild: {}, User: {}/{}, Arguments: {})",
                System.currentTimeMillis() - startTime, command.getName(), guild, event.getUser().getName(), event.getUser().getId(),
                Stream.of(arguments).map(v -> v == null ? "< null >" : v.toString()).collect(Collectors.joining(", ")));
    }

    @Override
    public void onGenericContextInteraction(@NotNull GenericContextInteractionEvent<?> event) {
        var name = event.getName();
        var func = this.bot.getCommandRegistry().getContextFunctions().get(event.getName());
        if (func == null)
            return;

        var startTime = System.currentTimeMillis();
        var guild = event.getGuild() == null ? "N/A" : event.getGuild().getId();
        var command = func.command();
        var method = func.method();
        var parameters = method.getParameters();
        var type = func.type();

        try {
            var arguments = new Object[parameters.length == 1 ? 1 : 2];
            arguments[0] = event;
            if (arguments.length > 1) {
                if (type == Command.Type.MESSAGE) {
                    var ev = (MessageContextInteractionEvent) event;
                    arguments[1] = ev.getTarget();
                }
                else {
                    var ev = (UserContextInteractionEvent) event;
                    arguments[1] = parameters[1].getType() == Member.class ? ev.getTargetMember() : ev.getTarget();
                }
            }

            method.invoke(command, arguments);
        }
        catch (Exception ex) {
            var channel = event.getChannel();
            if (channel == null)
                return;
            Reference.LOGGER.error("Unable to execute context command {} (Command: {}, Guild: {}, User: {}/{}, Channel: {})",
                    name, command.getName(), guild, event.getUser().getName(),
                    event.getUser().getId(), channel.getId(), ex);

            if (!event.isAcknowledged()) {
                event.replyEmbeds(EmbedUtils.failure("An internal error occurred while processing this command").build()).queue();
            }
        }

        Reference.LOGGER.info("Context command executed in {}ms (Action: {}, Command: {}, Guild: {}, User: {}/{})",
                System.currentTimeMillis() - startTime, name, command.getName(), guild, event.getUser().getName(), event.getUser().getId());
    }
}
