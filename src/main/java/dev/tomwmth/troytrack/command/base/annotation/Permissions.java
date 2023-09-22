package dev.tomwmth.troytrack.command.base.annotation;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/19/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Permissions {
    @NotNull
    Permission[] value();
}
