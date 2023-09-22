package dev.tomwmth.troytrack.command;

import com.hawolt.data.api.RankedQueue;
import com.hawolt.dto.league.v4.LeagueEntryDTO;
import com.hawolt.dto.summoner.v4.SummonerDto;
import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.command.base.Command;
import dev.tomwmth.troytrack.command.base.annotation.Option;
import dev.tomwmth.troytrack.command.base.annotation.SlashCommand;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.LeagueUtils;
import dev.tomwmth.troytrack.util.enums.RankIcon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
@SlashCommand(
        value = "rank",
        description = "View any summoner's current rank"
)
public class CommandRank extends Command {
    private static final String YES = "\u2705";
    private static final String NO = "\u274C";
    private static final String TITLE_TEMPLATE = "%s is currently %s";
    private static final String STATS_TITLE_TEMPLATE = "`\uD83C\uDFAE` Stats";
    private static final String STATS_DESCRIPTION_TEMPLATE = "%d Games\n" +
            "%dW %dL\n" +
            "%d%% Win Rate";
    private static final String FLAGS_TITLE_TEMPLATE = "`\uD83C\uDFF3\uFE0F` Flags";
    private static final String FLAGS_DESCRIPTION_TEMPLATE = "Hot Streak: `%s`\n" +
            "Veteran: `%s`\n" +
            "Fresh Blood: `%s`\n" +
            "Inactive: `%s`";

    public CommandRank(@NotNull TroyTrack bot) {
        super(bot);
    }

    @SlashCommand
    public void execute(@NotNull SlashCommandInteractionEvent event,
                        @Option(name = "summoner", desc = "The name of the summoner to search") String summonerName) {
        try {
            SummonerDto summoner = this.bot.getRiotApi().getSummonerByName(summonerName);
            LeagueEntryDTO leagueEntry = this.bot.getRiotApi().getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
            int gamesWon = leagueEntry.getWins(), gamesLost = leagueEntry.getLosses();
            int gamesTotal = gamesWon + gamesLost;
            int winRate = LeagueUtils.calculateWinRate(gamesWon, gamesTotal);

            String title = TITLE_TEMPLATE.formatted(summoner.getName(), LeagueUtils.getRankString(leagueEntry));
            String statsDescription = STATS_DESCRIPTION_TEMPLATE.formatted(gamesTotal, gamesWon, gamesLost, winRate);
            String flagsDescription = FLAGS_DESCRIPTION_TEMPLATE.formatted(
                    leagueEntry.isHotStreak()  ? YES : NO,
                    leagueEntry.isVeteran()    ? YES : NO,
                    leagueEntry.isFreshBlood() ? YES : NO,
                    leagueEntry.isInactive()   ? YES : NO
            );

            event.replyEmbeds(
                    EmbedUtils.of(title, null)
                            .addField(STATS_TITLE_TEMPLATE, statsDescription, true)
                            .addField(FLAGS_TITLE_TEMPLATE, flagsDescription, true)
                            .setThumbnail(RankIcon.valueOf(leagueEntry.getTier()).getIcon())
                            .build()
            ).queue();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isGuildOnly() {
        return false;
    }
}
