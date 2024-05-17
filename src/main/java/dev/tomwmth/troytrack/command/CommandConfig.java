package dev.tomwmth.troytrack.command;

import dev.tomwmth.troytrack.Config;
import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.command.base.Command;
import dev.tomwmth.troytrack.command.base.annotation.SlashCommand;
import dev.tomwmth.troytrack.util.EmbedUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
@SlashCommand(
        value = "config",
        description = "Reload the bots configuration"
)
public class CommandConfig extends Command {
    public CommandConfig(@NotNull TroyTrack bot) {
        super(bot);
    }

    @SlashCommand
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() == Config.getSettings().getAdminUserId()) {
            long start = System.currentTimeMillis();
            Config.getInstance().load();
            long elapsed = System.currentTimeMillis() - start;
            event.replyEmbeds(
                    EmbedUtils.success("The configuration was reloaded in %dms".formatted(elapsed))
                            .build()
            ).queue();
        } else {
            event.replyEmbeds(
                    EmbedUtils.failure("You do not have permission to execute this command")
                            .build()
            ).queue();
        }
    }

    @Override
    public boolean isGuildOnly() {
        return false;
    }
}
