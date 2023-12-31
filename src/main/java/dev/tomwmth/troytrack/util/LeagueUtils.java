package dev.tomwmth.troytrack.util;

import dev.tomwmth.troytrack.util.enums.RankIcon;
import dev.tomwmth.viego.lol.constants.RankedDivision;
import dev.tomwmth.viego.lol.constants.RankedTier;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.routing.Platform;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
@UtilityClass
public final class LeagueUtils {
    private static final String[] PLATFORM_STRINGS = { "Oceania", "North America", "Europe West", "Europe Nordic & East", "Korea", "Japan", "Brazil", "Latin America North", "Latin America South", "Russia", "Turkey", "Singapore", "Philippines", "Taiwan", "Vietnam", "Thailand" };

    public static int calculateWinRate(int gamesWon, int totalGames) {
        float percentage = (float) gamesWon / (float) totalGames;
        return Math.round(percentage * 100.0F);
    }

    @NotNull
    public static String calculateLeaguePointChange(@NotNull LeagueEntry before, @NotNull LeagueEntry after) {
        if (before.getTier() != after.getTier()) {
            if (before.getTier().ordinal() < after.getTier().ordinal())
                return "PROMOTED!";
            else
                return "DEMOTED!";
        }
        else if (before.getDivision() != after.getDivision()) {
            if (before.getDivision().ordinal() > after.getDivision().ordinal())
                return "PROMOTED!";
            else
                return "DEMOTED!";
        }
        else {
            String str = "";
            int lpChange = after.getPoints() - before.getPoints();
            if (lpChange >= 0)
                str += "+";
            else
                str += "-";
            str += Math.abs(lpChange);
            str += " LP";
            return str;
        }
    }

    @NotNull
    public static String getRankString(@NotNull LeagueEntry leagueEntry) {
        RankedTier tier = leagueEntry.getTier();
        RankedDivision division = leagueEntry.getDivision();
        int lp = leagueEntry.getPoints();

        StringBuilder sb = new StringBuilder();
        sb.append(convertToTitleCase(tier.name())).append(" ");
        sb.append(division).append(" ");
        sb.append("(").append(lp).append(" LP)");

        return sb.toString();
    }

    @NotNull
    public static String getAbbreviatedRankString(@NotNull LeagueEntry leagueEntry) {
        RankedTier tier = leagueEntry.getTier();
        RankedDivision division = leagueEntry.getDivision();
        int lp = leagueEntry.getPoints();

        StringBuilder sb = new StringBuilder();
        sb.append(RankIcon.valueOf(tier).getEmoji()).append(" ");
        sb.append(tier.name().charAt(0));
        sb.append(RomanNumerals.convertFrom(division.name())).append(" ");
        sb.append("(").append(lp).append(" LP)");

        return sb.toString();
    }

    @NotNull
    public static Platform platformFromString(@NotNull String platformString) {
        return switch (platformString) {
            case "Oceania" -> Platform.OC1;
            case "North America" -> Platform.NA1;
            case "Europe West" -> Platform.EUW1;
            case "Europe Nordic & East" -> Platform.EUN1;
            case "Korea" -> Platform.KR;
            case "Japan" -> Platform.JP1;
            case "Brazil" -> Platform.BR1;
            case "Latin America North" -> Platform.LA1;
            case "Latin America South" -> Platform.LA2;
            case "Russia" -> Platform.RU;
            case "Turkey" -> Platform.TR1;
            case "Singapore" -> Platform.SG2;
            case "Philippines" -> Platform.PH2;
            case "Taiwan" -> Platform.TW2;
            case "Vietnam" -> Platform.VN2;
            case "Thailand" -> Platform.TH2;
            default -> throw new IllegalArgumentException("Invalid platform: " + platformString);
        };
    }

    @NotNull
    private static String convertToTitleCase(@NotNull String text) {
        StringBuilder sb = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            }
            else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            }
            else {
                ch = Character.toLowerCase(ch);
            }
            sb.append(ch);
        }

        return sb.toString();
    }
}
