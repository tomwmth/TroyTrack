package dev.tomwmth.troytrack.command.base.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/20/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {
    @NotNull
    String value() default "";
}
