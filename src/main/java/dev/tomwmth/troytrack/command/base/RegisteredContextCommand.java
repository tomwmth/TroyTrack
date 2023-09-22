package dev.tomwmth.troytrack.command.base;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public record RegisteredContextCommand(@NotNull Command command, @NotNull Method method, @NotNull net.dv8tion.jda.api.interactions.commands.Command.Type type) {
}
