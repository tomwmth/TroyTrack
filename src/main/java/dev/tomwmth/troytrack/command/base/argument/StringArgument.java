package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class StringArgument extends Argument<String> {

    public StringArgument(@NotNull String name, @Nullable Object defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public @Nullable Object assign(@NotNull OptionMapping argument) {
        return argument.getAsString();
    }

    @Override
    public @Nullable String deserialize(@NotNull Object input) {
        return input.toString();
    }
}
