package dev.tomwmth.troytrack.util;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
@UtilityClass
public final class EmbedUtils {
    public static final Color NEUTRAL_COLOR = new Color(0x5326E6);
    public static final Color SUCCESS_COLOR = new Color(0x26E65C);
    public static final Color SPECIAL_COLOR = new Color(0xFFFFC400);
    public static final Color ERROR_COLOR = new Color(0xf42c2c);
    private static final String FOOTER = "";

    @NotNull
    public static EmbedBuilder of(@NotNull String title, @Nullable String description) {
        return new EmbedBuilder().setTitle(title).setDescription(description)
                .setColor(NEUTRAL_COLOR);
    }

    @NotNull
    public static EmbedBuilder of(@NotNull String description) {
        return new EmbedBuilder().setDescription(description)
                .setColor(NEUTRAL_COLOR);
    }

    @NotNull
    public static EmbedBuilder success(@NotNull String... description) {
        return prefixed("\u2705", description).setColor(SUCCESS_COLOR);
    }

    @NotNull
    public static EmbedBuilder failure(@NotNull String... description) {
        return prefixed("\u274C", description).setColor(ERROR_COLOR);
    }

    @NotNull
    public static EmbedBuilder prefixed(@NotNull String prefix, @NotNull String... description) {
        return of("`" + prefix + "` " + String.join("\n", description)).setFooter(FOOTER);
    }
}
