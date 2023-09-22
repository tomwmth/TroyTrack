package dev.tomwmth.troytrack.command.base;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
public record RegisteredSlashCommand(@NotNull Method method, @NotNull List<Argument> arguments) {
}
