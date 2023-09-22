package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class ChannelArgument extends Argument<Channel> {

    public ChannelArgument(@NotNull String name) {
        super(name, null);
    }

    @Override
    public @Nullable Object assign(@NotNull OptionMapping argument) {
        return argument.getAsRole();
    }

    @Override
    public @Nullable Channel getDefaultValue() {
        return null;
    }

    @Override
    public @Nullable Channel deserialize(@NotNull Object input) {
        return null;
    }
}
