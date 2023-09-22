package dev.tomwmth.troytrack.listener.base;

import dev.tomwmth.troytrack.TroyTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
public abstract class EventListener extends ListenerAdapter {
    protected final TroyTrack bot;

    protected final JDA jda;

    public EventListener(@NotNull TroyTrack bot) {
        this.bot = bot;
        this.jda = this.bot.getJda();
    }
}
