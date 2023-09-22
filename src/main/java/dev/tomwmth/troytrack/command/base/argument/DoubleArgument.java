package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class DoubleArgument extends Argument<Double> {

    public DoubleArgument(@NotNull String name, @NotNull Object defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public @NotNull Object assign(@NotNull OptionMapping argument) {
        return argument.getAsDouble();
    }

    @Override
    public @Nullable Double deserialize(@NotNull Object input) {
        try {
            return Double.parseDouble(input.toString());
        }
        catch (Exception ex) {
            return null;
        }
    }
}
