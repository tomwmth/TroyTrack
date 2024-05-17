package dev.tomwmth.troytrack.command.base.annotation;

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
@Target({ElementType.METHOD})
public @interface SubCommand {
    @NotNull
    String name() default "";

    @NotNull
    String description() default "";

    @NotNull
    String group() default "";

    @NotNull
    String groupDescription() default "";
}
