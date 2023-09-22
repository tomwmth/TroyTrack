package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class EnumArgument extends Argument<Enum<?>> {

    private final Enum<?>[] enumConstants;

    public EnumArgument(@NotNull String name, @NotNull Class<Enum<?>> clazz, @Nullable Object defaultValue) {
        super(name, null);
        if (!clazz.isEnum())
            throw new IllegalArgumentException("class must be an enum class");
        this.enumConstants = clazz.getEnumConstants();
        this.defaultValue = defaultValue == null ? null : this.deserialize(defaultValue);
    }

    @Override
    public @Nullable Object assign(@NotNull OptionMapping argument) {
        return this.deserialize(argument.getAsString());
    }

    @Override
    public @Nullable Enum<?> deserialize(@NotNull Object input) {
        for (var value : this.enumConstants)
            if (value.name().equalsIgnoreCase(input.toString()))
                return value;
        return null;
    }
}
