package dev.tomwmth.troytrack.listener;

import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.command.base.CommandRegistry;
import dev.tomwmth.troytrack.listener.base.EventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
public class ReadyListener extends EventListener {
    public ReadyListener(@NotNull TroyTrack bot) {
        super(bot);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Thread.currentThread().setName("Discord");

        Reference.LOGGER.info("Connected to Discord API! (user: {}, guilds: {})", this.jda.getSelfUser().getName(), event.getGuildTotalCount());

        CommandRegistry registry = this.bot.getCommandRegistry();
        registry.updateGlobalCommands(this.jda);
        for (Guild guild : this.jda.getGuilds())
            registry.updateGuildCommands(guild);
    }
}
