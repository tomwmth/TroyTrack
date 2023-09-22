package dev.tomwmth.troytrack.command.base.argument;

import dev.tomwmth.troytrack.command.base.Argument;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public class UserArgument extends Argument<User> {

    public UserArgument(@NotNull String name) {
        super(name, null);
    }

    @Override
    public @Nullable Object assign(@NotNull OptionMapping argument) {
        return argument.getAsUser();
    }

    @Override
    public @Nullable User getDefaultValue() {
        return null;
    }

    @Override
    public @Nullable User deserialize(@NotNull Object input) {
        return null;
    }
}
