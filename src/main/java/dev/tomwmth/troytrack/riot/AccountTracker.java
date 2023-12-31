package dev.tomwmth.troytrack.riot;

import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.riot.score.PiggyScoreV2;
import dev.tomwmth.troytrack.riot.score.ScoreProvider;
import dev.tomwmth.troytrack.util.CollectionUtils;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.LeagueUtils;
import dev.tomwmth.viego.lol.constants.RankedQueue;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.match.v5.obj.Participant;
import dev.tomwmth.viego.lol.summoner.v4.obj.Summoner;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.routing.Platform;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class AccountTracker {
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
    private final RiotId riotId;

    @NotNull
    private final Platform platform;

    @Nullable
    private LeagueEntry latestEntry;

    @Nullable
    private Match latestMatch;

    public AccountTracker(@NotNull RiotApi api, @NotNull RiotId riotId, @NotNull Platform platform) throws Exception {
        this.api = api;
        this.riotId = riotId;
        this.platform = platform;


        RiotAccount account = this.api.getRiotAccountById(this.riotId);
        Summoner summoner = this.api.getSummonerByRiotAccount(account);
        this.latestEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
        this.latestMatch = this.api.getLatestMatch(account, platform);
    }

    public void heartbeat() throws Exception {
        String message = null;
        boolean broken = false;

        RiotAccount account = this.api.getRiotAccountById(this.riotId);
        Summoner summoner = this.api.getSummonerByRiotAccount(account);
        Match otherMatch = this.api.getLatestMatch(account, this.platform);

        if (this.latestEntry == null) {
            broken = true;
            this.latestEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
            message = "latest entry was null, attempted fix";
        }

        if (this.latestMatch == null) {
            broken = true;
            this.latestMatch = this.api.getLatestMatch(account, platform);
            message = "latest match was null, attempted fix";
        }

        if (!broken && otherMatch != null
                && !otherMatch.getMetadata().getId().equals(this.latestMatch.getMetadata().getId())) {
            this.processNewMatch(summoner, otherMatch);
            message = "found new match";
        }
        else if (message == null) {
            message = "no new match";
        }

        Reference.LOGGER.info("Heartbeat for \"{}\" completed: {}", this.riotId, message);
    }

    private void processNewMatch(Summoner summoner, Match newMatch) {
        LeagueEntry newEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
        boolean remake = (newMatch.getInfo().getDuration() / 60.0F) < 5;
        if (!remake) {
            Participant tracked = newMatch.getParticipant(summoner.getPuuid());

            boolean won = tracked.isWin();
            String matchId = newMatch.getMetadata().getId();
            String thumbnail = won ? CollectionUtils.pickRandom(THUMBNAIL_WON) : CollectionUtils.pickRandom(THUMBNAIL_LOST);
            String title = TITLE_TEMPLATE.formatted(riotId.toString(), (won ? "won" : "lost"), (won ? "!" : "."));
            String previousRankString = LeagueUtils.getAbbreviatedRankString(this.latestEntry);
            String newRankString = LeagueUtils.getAbbreviatedRankString(newEntry);
            String lpChange = LeagueUtils.calculateLeaguePointChange(this.latestEntry, newEntry);
            String changeDescription = CHANGE_DESCRIPTION_TEMPLATE.formatted(previousRankString, newRankString);
            String footer = FOOTER_TEMPLATE.formatted(matchId);

            ScoreProvider scoreProvider = new PiggyScoreV2(newMatch);
            int trackedScore = 0;
            int teamTotal = 0;
            for (Participant participant : newMatch.getInfo().getParticipants()) {
                if (tracked.getPuuid().equals(participant.getPuuid())) {
                    trackedScore = scoreProvider.calculateScore(participant);
                }
                else if (tracked.getTeamId() == participant.getTeamId()) {
                    teamTotal += scoreProvider.calculateScore(participant);
                }
            }
            int teamAverage = teamTotal / 4;

            String link = GAME_INFO_PATH.formatted(matchId.replace("OC1_", ""), tracked.getId());
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
