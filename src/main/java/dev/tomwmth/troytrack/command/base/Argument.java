package dev.tomwmth.troytrack.command.base;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
@Getter
public abstract class Argument<T> {
    private final String name;

    protected T defaultValue;

    public Argument(@NotNull String name, @Nullable Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue == null ? null : this.deserialize(defaultValue);
    }

    @Nullable
    public abstract Object assign(@NotNull OptionMapping argument);

    @Nullable
    public abstract T deserialize(@NotNull Object input);
}
