package dev.tomwmth.troytrack.command.base;

import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.command.base.annotation.SlashCommand;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/19/23
 */
public abstract class Command {
    protected final TroyTrack bot;

    protected final JDA jda;

    @Getter @Setter
    protected CommandData commandData;

    @Getter
    protected final List<CommandData> contextCommands = new ArrayList<>();

    @Getter
    private final boolean guildOnly;

    public Command(@NotNull TroyTrack bot) {
        this.bot = bot;
        this.jda = this.bot.getJda();

        var annotation = this.getClass().getAnnotation(SlashCommand.class);
        this.guildOnly = annotation.guildOnly();
    }

    @NotNull
    public String getName() {
        return this.commandData.getName();
    }
}
