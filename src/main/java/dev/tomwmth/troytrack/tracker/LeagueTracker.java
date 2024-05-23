package dev.tomwmth.troytrack.tracker;

import dev.tomwmth.troytrack.obj.TrackedAccount;
import dev.tomwmth.troytrack.tracker.score.PiggyScoreV2;
import dev.tomwmth.troytrack.tracker.score.ScoreProvider;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.LeagueUtils;
import dev.tomwmth.viego.lol.constants.RankedQueue;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.match.v5.obj.Participant;
import dev.tomwmth.viego.lol.summoner.v4.obj.Summoner;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public final class LeagueTracker extends AccountTracker {
    private static final String GAME_INFO_PATH = "https://www.leagueofgraphs.com/match/oce/%s#participant%d";
    private static final String CHAMP_ICON_PATH = "https://cdn.communitydragon.org/latest/champion/%d/square";
    private static final String TITLE_TEMPLATE = "%s %s a match%s";
    private static final String DESCRIPTION_TEMPLATE = """
            ● **%d**/**%d**/**%d** (%.2f KDA)
            ● **%d CS** (%.1f per min)
            ● **%d%% KP**

            You can view the full game recap [here](%s).
            """;
    private static final String CHANGE_DESCRIPTION_TEMPLATE = "%s ➜ %s";
    private static final String SCORE_TITLE_TEMPLATE = "%d PS";
    private static final String SCORE_DESCRIPTION_TEMPLATE = "Team averaged %d PS ➜ (Δ%s)";
    private static final String FOOTER_TEMPLATE = "Match ID: %s";

    @Nullable
    private LeagueEntry latestEntry;

    @Nullable
    private Match latestMatch;

    public LeagueTracker(@NotNull TrackedAccount trackedAccount) throws Exception {
        super(trackedAccount);

        RiotAccount account = this.api.getRiotAccountById(this.trackedAccount.getRiotId());
        Summoner summoner = this.api.getLolApi().getSummonerByRiotAccount(account);
        this.latestEntry = this.api.getLolApi().getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_DUO, this.trackedAccount.getPlatform());
        this.latestMatch = this.api.getLolApi().getLatestMatch(account, this.trackedAccount.getPlatform());
    }

    @Override
    public @NotNull String heartbeat() throws Exception {
        String message = null;

        RiotAccount account = this.api.getRiotAccountById(this.trackedAccount.getRiotId());
        Match otherMatch = this.api.getLolApi().getLatestMatch(account, this.trackedAccount.getPlatform());

        if (otherMatch != null) {
            Summoner summoner = this.api.getLolApi().getSummonerByRiotAccount(account);
            LeagueEntry otherEntry = this.api.getLolApi().getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_DUO, this.trackedAccount.getPlatform());
            if (this.latestMatch == null || !otherMatch.getMetadata().getId().equals(this.latestMatch.getMetadata().getId())) {
                boolean remake = (otherMatch.getInfo().getDuration() / 60.0F) <= 3.5F;
                if (!remake) {
                    this.processNewMatch(summoner, otherMatch, otherEntry);
                    message = "processed new match";
                }
                else {
                    message = "ignored likely remake";
                }
            }

            this.latestMatch = otherMatch;
            this.latestEntry = otherEntry;
        }

        if (message == null) {
            message = "no new match";
        }

        return message;
    }

    private void processNewMatch(@NotNull Summoner summoner, @NotNull Match newMatch, @Nullable LeagueEntry newEntry) {
        Participant tracked = newMatch.getParticipant(summoner.getPuuid());

        boolean won = tracked.isWin();
        String matchId = newMatch.getMetadata().getId().split("_")[1];
        String thumbnail = CHAMP_ICON_PATH.formatted(tracked.getChampionId());
        String title = TITLE_TEMPLATE.formatted(this.trackedAccount.getRiotId().toString(), (won ? "won" : "lost"), (won ? "!" : "."));
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
            } else if (tracked.getTeamId() == participant.getTeamId()) {
                teamTotal += scoreProvider.calculateScore(participant);
            }
        }
        int teamAverage = teamTotal / 4;

        String scoreTitle = SCORE_TITLE_TEMPLATE.formatted(trackedScore);
        String scoreDescription = SCORE_DESCRIPTION_TEMPLATE.formatted(teamAverage, this.calculateDelta(trackedScore, teamAverage));

        String link = GAME_INFO_PATH.formatted(matchId, tracked.getId());
        String description = DESCRIPTION_TEMPLATE.formatted(
                tracked.getKills(), tracked.getDeaths(), tracked.getAssists(), scoreProvider.calculateKDA(tracked),
                scoreProvider.calculateCS(tracked), scoreProvider.calculateCSPM(tracked),
                Math.round(scoreProvider.calculateKP(tracked) * 100.0F), link
        );

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setThumbnail(thumbnail)
                .setFooter(footer)
                .setColor(won ? EmbedUtils.SUCCESS_COLOR : EmbedUtils.ERROR_COLOR);

        if (newEntry != null) {
            eb.addField(lpChange, changeDescription, false);
        }
        eb.addField(scoreTitle, scoreDescription, false);

        this.sendMessage(eb.build());
    }

    private @NotNull String calculateDelta(int individualScore, int teamAverageScore) {
        int delta = individualScore - teamAverageScore;
        boolean positive = delta >= 0;
        delta = Math.abs(delta);
        return (positive ? "+" : "-") + delta;
    }
}
