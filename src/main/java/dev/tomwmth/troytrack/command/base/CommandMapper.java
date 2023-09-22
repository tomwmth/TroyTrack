package dev.tomwmth.troytrack.command.base;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/19/23
 */
public interface CommandMapper<S, T> {

    @NotNull
    T transform(@NotNull S value) throws Exception;

    @NotNull
    default Class<?> getInput() {
        return Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals("transform"))
                .findFirst().orElseThrow().getParameterTypes()[0];
    }

    @NotNull
    default Class<?> getOutput() {
        return Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.getName().equals("transform"))
                .findFirst().orElseThrow().getReturnType();
    }

    @NotNull
    default OptionType getType() {
        if (String.class.isAssignableFrom(this.getInput()))
            return OptionType.STRING;
        else if (Long.class.isAssignableFrom(this.getInput()))
            return OptionType.INTEGER;
        else if (Boolean.class.isAssignableFrom(this.getInput()))
            return OptionType.BOOLEAN;
        else if (User.class.isAssignableFrom(this.getInput()))
            return OptionType.USER;
        else if (Channel.class.isAssignableFrom(this.getInput()))
            return OptionType.CHANNEL;
        else if (Role.class.isAssignableFrom(this.getInput()))
            return OptionType.ROLE;
        else if (IMentionable.class.isAssignableFrom(this.getInput()))
            return OptionType.MENTIONABLE;
        else if (Double.class.isAssignableFrom(this.getInput()))
            return OptionType.NUMBER;
        else {
            throw new IllegalArgumentException("Invalid input type: " + this.getInput().getCanonicalName());
        }
    }
}
