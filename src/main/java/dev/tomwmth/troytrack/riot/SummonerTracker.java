package dev.tomwmth.troytrack.riot;

import com.hawolt.data.api.RankedQueue;
import com.hawolt.dto.league.v4.LeagueEntryDTO;
import com.hawolt.dto.match.v5.match.MatchDto;
import com.hawolt.dto.match.v5.match.ParticipantDto;
import com.hawolt.dto.summoner.v4.SummonerDto;
import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.riot.score.PiggyScoreV2;
import dev.tomwmth.troytrack.riot.score.ScoreProvider;
import dev.tomwmth.troytrack.util.CollectionUtils;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.LeagueUtils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class SummonerTracker {
    private static final String GAME_INFO_PATH = "https://www.leagueofgraphs.com/match/oce/%s#participant%d";
    private static final String[] THUMBNAIL_WON = {
            "https://media.tenor.com/flGNpobJuuoAAAAi/happy-clap.gif",
            "https://media.tenor.com/XggXN7Y2G7YAAAAi/poggies-poggieshands.gif",
            "https://media.tenor.com/9phgfRuGM8UAAAAi/marcelomunareto5.gif",
            "https://media.tenor.com/9HiFghlR4WAAAAAi/clapping-drake.gif"
    };
    private static final String[] THUMBNAIL_LOST = {
            "https://media.tenor.com/VB1JaUzZqIsAAAAi/sadge-cry-sadge.gif",
            "https://media.tenor.com/iCLR19N5OsMAAAAi/nooo.gif",
            "https://media.tenor.com/iaz00NplM0QAAAAi/icant.gif",
            "https://media.tenor.com/IaSQ2CvyEAoAAAAi/pepepoint-pepe.gif"
    };
    private static final String TITLE_TEMPLATE = "%s %s a match%s";
    private static final String DESCRIPTION_TEMPLATE = "You can view the full game recap [here](%s).\n" +
            "\n" +
            "%s";
    private static final String CHANGE_DESCRIPTION_TEMPLATE = "%s ➜ %s";
    private static final String FOOTER_TEMPLATE = "Match ID: %s";

    @NotNull
    private final RiotApi api;

    @Getter @NotNull
    private final String summonerName;

    @NotNull
    private LeagueEntryDTO latestEntry;

    @NotNull
    private MatchDto latestMatch;

    public SummonerTracker(@NotNull RiotApi api, @NotNull String summonerName) throws Exception {
        this.api = api;
        this.summonerName = summonerName;

        SummonerDto summoner = this.api.getSummonerByName(this.summonerName);
        this.latestEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
        this.latestMatch = this.api.getLatestMatch(summoner);
    }

    public void heartbeat() throws Exception {
        SummonerDto summoner = this.api.getSummonerByName(this.summonerName);
        MatchDto otherMatch = this.api.getLatestMatch(summoner);
        if (!otherMatch.getMetadataDto().getMatchId().equals(this.latestMatch.getMetadataDto().getMatchId())) {
            this.processNewMatch(summoner, otherMatch);
            Reference.LOGGER.info("Heartbeat for \"{}\" completed: found new match", this.summonerName);
        }
        else {
            Reference.LOGGER.info("Heartbeat for \"{}\" completed: no new match", this.summonerName);
        }
    }

    private void processNewMatch(SummonerDto summoner, MatchDto newMatch) throws Exception {
        LeagueEntryDTO newEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
        boolean remake = (newMatch.getInfoDto().getGameDuration() / 60.0F) < 5;
        if (!remake) {
            boolean won = newMatch.isVictorious(summoner);
            String matchId = newMatch.getMetadataDto().getMatchId();
            String thumbnail = won ? CollectionUtils.pickRandom(THUMBNAIL_WON) : CollectionUtils.pickRandom(THUMBNAIL_LOST);
            String title = TITLE_TEMPLATE.formatted(summoner.getName(), (won ? "won" : "lost"), (won ? "!" : "."));
            String previousRankString = LeagueUtils.getAbbreviatedRankString(this.latestEntry);
            String newRankString = LeagueUtils.getAbbreviatedRankString(newEntry);
            String lpChange = LeagueUtils.calculateLeaguePointChange(this.latestEntry, newEntry);
            String changeDescription = CHANGE_DESCRIPTION_TEMPLATE.formatted(previousRankString, newRankString);
            String footer = FOOTER_TEMPLATE.formatted(matchId);

            ParticipantDto tracked = newMatch.getParticipantData(summoner.getPUUID());

            ScoreProvider scoreProvider = new PiggyScoreV2(newMatch);
            int trackedScore = 0;
            int teamTotal = 0;
            for (ParticipantDto participant : newMatch.getInfoDto().getParticipants()) {
                if (tracked.getSummonerId().equals(participant.getSummonerId())) {
                    trackedScore = scoreProvider.calculateScore(participant);
                }
                else if (tracked.getTeamId() == participant.getTeamId()) {
                    teamTotal += scoreProvider.calculateScore(participant);
                }
            }
            int teamAverage = teamTotal / 4;

            String link = GAME_INFO_PATH.formatted(matchId.replace("OC1_", ""), tracked.getParticipantId());
            String description = DESCRIPTION_TEMPLATE.formatted(link, scoreProvider.generateVerdict(trackedScore, teamAverage));

            MessageEmbed eb = new EmbedBuilder()
                    .setTitle(title)
                    .addField(lpChange, changeDescription, false)
                    .addField("Individual Score", trackedScore + " PS", true)
                    .addField("Team Score", teamAverage + " PS", true)
                    .setDescription(description)
                    .setThumbnail(thumbnail)
                    .setFooter(footer)
                    .setColor(won ? EmbedUtils.SUCCESS_COLOR : EmbedUtils.ERROR_COLOR)
                    .build();
            TroyTrack.getInstance().sendTrackingMessage(eb);
        }
        else {
            Reference.LOGGER.info("Detected match was a remake, ignoring...");
        }

        this.latestEntry = newEntry;
        this.latestMatch = newMatch;
    }
}
