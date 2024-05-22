package dev.tomwmth.troytrack.util;

import dev.tomwmth.troytrack.util.enums.RankIcon;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.shared.constants.RankedDivision;
import dev.tomwmth.viego.shared.constants.RankedTier;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
@UtilityClass
public final class LeagueUtils {
    public static int calculateWinRate(int gamesWon, int totalGames) {
        float percentage = (float) gamesWon / (float) totalGames;
        return Math.round(percentage * 100.0F);
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
