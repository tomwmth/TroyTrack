package dev.tomwmth.troytrack.util;

import dev.tomwmth.troytrack.util.enums.RankIcon;
import dev.tomwmth.viego.shared.constants.RankedDivision;
import dev.tomwmth.viego.shared.constants.RankedTier;
import dev.tomwmth.viego.tft.league.v1.obj.LeagueEntry;
import dev.tomwmth.viego.tft.match.v1.obj.Trait;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 24/05/2024
 */
public final class TftUtils {
    private static final Pattern TRAIT_NAME_PATTERN = Pattern.compile("^TFT\\d*_(.+)$");

    public static @NotNull String generateTraitList(@NotNull List<Trait> traits, @NotNull String format) {
        traits = traits.stream()
                .filter(t -> t.getCurrentTier() > 0)
                .sorted((t1, t2) -> {
                    int styleComp = Integer.compare(t2.getStyle(), t1.getStyle());
                    if (styleComp != 0) {
                        return styleComp;
                    }
                    return Integer.compare(t2.getUnitAmount(), t1.getUnitAmount());
                })
                .toList();

        List<String> lines = new ArrayList<>();
        for (Trait trait : traits) {
            String id = sanitizeTraitId(trait.getName());
            lines.add(String.format(format, trait.getUnitAmount(), id));
        }
        return String.join("\n", lines);
    }

    public static @NotNull String sanitizeTraitId(@NotNull String id) {
        Matcher matcher = TRAIT_NAME_PATTERN.matcher(id);
        if (matcher.matches()) {
            String isolatedName = matcher.group(1);
            return Utils.convertToTitleCase(isolatedName);
        }
        return id;
    }

    public static @NotNull String calculateLeaguePointChange(@Nullable LeagueEntry before, @Nullable LeagueEntry after) {
        if (before == null || before.getTier() == null || before.getDivision() == null ||
                after == null || after.getTier() == null || after.getDivision() == null)  {
            return "PLACED!";
        }
        if (before.getTier() != after.getTier()) {
            if (before.getTier().ordinal() < after.getTier().ordinal())
                return "PROMOTED!";
            else
                return "DEMOTED!";
        } else if (before.getDivision() != after.getDivision()) {
            if (before.getDivision().ordinal() > after.getDivision().ordinal())
                return "PROMOTED!";
            else
                return "DEMOTED!";
        } else {
            int lpChange = after.getPoints() - before.getPoints();
            return String.format("%s%d LP", (lpChange >= 0) ? "+" : "-", Math.abs(lpChange));
        }
    }

    public static @NotNull String getRankString(@Nullable LeagueEntry leagueEntry) {
        if (leagueEntry != null) {
            RankedTier tier = leagueEntry.getTier();
            RankedDivision division = leagueEntry.getDivision();

            if (tier != null && division != null) {
                return String.format(
                        "%s %s (%d LP)",
                        tier.toDisplayableString(),
                        division.toDisplayableString(),
                        leagueEntry.getPoints()
                );
            }
        }
        return "Unranked";
    }

    public static @NotNull String getAbbreviatedRankString(@Nullable LeagueEntry leagueEntry) {
        if (leagueEntry != null) {
            RankedTier tier = leagueEntry.getTier();
            RankedDivision division = leagueEntry.getDivision();

            if (tier != null && division != null) {
                return String.format(
                        "%s %s%d (%d LP)",
                        RankIcon.valueOf(tier).getEmoji(),
                        tier.toDisplayableString().charAt(0),
                        RomanNumerals.convertFrom(division.toDisplayableString()),
                        leagueEntry.getPoints()
                );
            }
        }
        return String.format("%s N/A", RankIcon.UNRANKED.getEmoji());
    }
}
