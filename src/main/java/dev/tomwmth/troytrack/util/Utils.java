package dev.tomwmth.troytrack.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 23/05/2024
 */
@UtilityClass
public final class Utils {
    public static @NotNull String ordinal(int num) {
        int mod100 = num % 100;
        int mod10 = num % 10;
        if (mod10 == 1 && mod100 != 11)
            return num + "st";
        else if (mod10 == 2 && mod100 != 12)
            return num + "nd";
        else if (mod10 == 3 && mod100 != 13)
            return num + "rd";
        else
            return num + "th";
    }

    public static @NotNull String convertToTitleCase(@NotNull String text) {
        StringBuilder sb = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            sb.append(ch);
        }

        return sb.toString();
    }
}
