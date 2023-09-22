package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class BooleanArgument extends Argument<Boolean> {

    public BooleanArgument(@NotNull String name, @Nullable Object defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public @Nullable Object assign(@NotNull OptionMapping argument) {
        return argument.getAsBoolean();
    }

    @Override
    public @Nullable Boolean deserialize(@NotNull Object input) {
        return input.toString().equalsIgnoreCase("true");
    }
}
