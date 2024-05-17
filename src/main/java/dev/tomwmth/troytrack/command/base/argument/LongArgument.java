package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class LongArgument extends Argument<Long> {

    public LongArgument(@NotNull String name, @NotNull Object defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public @NotNull Object assign(@NotNull OptionMapping argument) {
        return (long) argument.getAsInt();
    }

    @Override
    public @Nullable Long deserialize(@NotNull Object input) {
        try {
            return Long.parseLong(input.toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
