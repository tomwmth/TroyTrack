package dev.tomwmth.troytrack.util.object;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * @author BestBearr <crumbygames12@gmail.com>
 * Created on 2/19/23
 */
@Getter @Setter
@EqualsAndHashCode @ToString
public class Pair<A, B> {
    private A first;

    private B second;

    private Pair(@NotNull A a, @NotNull B b) {
        this.first = a;
        this.second = b;
    }

    @NotNull
    public static <A, B> Pair<A, B> of(@NotNull A first, @NotNull B second) {
        return new Pair<>(first, second);
    }
}
