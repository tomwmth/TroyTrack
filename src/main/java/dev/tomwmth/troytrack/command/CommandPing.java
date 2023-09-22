package dev.tomwmth.troytrack.command;

import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.command.base.Command;
import dev.tomwmth.troytrack.command.base.annotation.SlashCommand;
import dev.tomwmth.troytrack.util.EmbedUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
@SlashCommand(
        value = "ping",
        description = "View the bots latency to Discord"
)
public class CommandPing extends Command {
    public CommandPing(@NotNull TroyTrack bot) {
        super(bot);
    }

    @SlashCommand
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        long start = System.currentTimeMillis();
        event.replyEmbeds(EmbedUtils.prefixed("\uD83D\uDD70️", "Timing...").build()).queue(success -> {
            long time = System.currentTimeMillis() - start;
            long gateway = this.jda.getGatewayPing();
            success.editOriginalEmbeds(EmbedUtils.of("Pong!", "`%s` **Message Latency:** %dms\n`%s` **API Latency:** %dms"
                    .formatted("\uD83D\uDCE1", time, "\uD83D\uDEF0️", gateway)).build()).queue();
        });
    }

    @Override
    public boolean isGuildOnly() {
        return false;
    }
}
