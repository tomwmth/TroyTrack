package dev.tomwmth.troytrack.riot;

import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.obj.CachedGuildChannel;
import dev.tomwmth.troytrack.obj.TrackedAccount;
import dev.tomwmth.troytrack.riot.score.PiggyScoreV2;
import dev.tomwmth.troytrack.riot.score.ScoreProvider;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.LeagueUtils;
import dev.tomwmth.viego.lol.constants.RankedQueue;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.match.v5.obj.Participant;
import dev.tomwmth.viego.lol.summoner.v4.obj.Summoner;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class AccountTracker {
    private static final String GAME_INFO_PATH = "https://www.leagueofgraphs.com/match/oce/%s#participant%d";
    private static final String CHAMP_ICON_PATH = "https://cdn.communitydragon.org/latest/champion/%d/square";
    private static final String TITLE_TEMPLATE = "%s %s a match%s";
    private static final String DESCRIPTION_TEMPLATE = """
            ● **%d**/**%d**/**%d** (%.2f KDA, %d%% KP)
            ● **%d CS** (%.1f per min)

            You can view the full game recap [here](%s).
            """;
    private static final String CHANGE_DESCRIPTION_TEMPLATE = "%s ➜ %s";
    private static final String SCORE_TITLE_TEMPLATE = "%d PS";
    private static final String SCORE_DESCRIPTION_TEMPLATE = "Team averaged %d PS ➜ (Δ%s)";
    private static final String FOOTER_TEMPLATE = "Match ID: %s";

    @NotNull
    private final RiotApi api;

    @Getter @NotNull
    private final TrackedAccount trackedAccount;

    @Nullable
    private LeagueEntry latestEntry;

    @Nullable
    private Match latestMatch;

    public AccountTracker(@NotNull RiotApi api, @NotNull TrackedAccount trackedAccount) throws Exception {
        this.api = api;
        this.trackedAccount = trackedAccount;

        RiotAccount account = this.api.getRiotAccountById(this.trackedAccount.getRiotId());
        Summoner summoner = this.api.getSummonerByRiotAccount(account);
        this.latestEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
        this.latestMatch = this.api.getLatestMatch(account, this.trackedAccount.getPlatform());
    }

    public void heartbeat() throws Exception {
        String message = null;
        boolean broken = false;

        RiotAccount account = this.api.getRiotAccountById(this.trackedAccount.getRiotId());
        Summoner summoner = this.api.getSummonerByRiotAccount(account);
        Match otherMatch = this.api.getLatestMatch(account, this.trackedAccount.getPlatform());

        if (this.latestEntry == null) {
            broken = true;
            this.latestEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
            message = "latest entry was null, attempted fix";
        }

        if (this.latestMatch == null) {
            broken = true;
            this.latestMatch = this.api.getLatestMatch(account, this.trackedAccount.getPlatform());
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

        Reference.LOGGER.info("Heartbeat for \"{}\" completed: {}", this.trackedAccount.getRiotId(), message);
    }

    private void processNewMatch(Summoner summoner, Match newMatch) {
        LeagueEntry newEntry = this.api.getLeagueEntry(summoner, RankedQueue.RANKED_SOLO_5x5);
        boolean remake = (newMatch.getInfo().getDuration() / 60.0F) < 5;
        if (!remake) {
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
                }
                else if (tracked.getTeamId() == participant.getTeamId()) {
                    teamTotal += scoreProvider.calculateScore(participant);
                }
            }
            int teamAverage = teamTotal / 4;

            String scoreTitle = SCORE_TITLE_TEMPLATE.formatted(trackedScore);
            String scoreDescription = SCORE_DESCRIPTION_TEMPLATE.formatted(teamAverage, this.calculateDelta(trackedScore, teamAverage));

            String link = GAME_INFO_PATH.formatted(matchId, tracked.getId());
            String description = DESCRIPTION_TEMPLATE.formatted(
                    tracked.getKills(), tracked.getDeaths(), tracked.getAssists(), scoreProvider.calculateKDA(tracked),
                    Math.round(scoreProvider.calculateKP(tracked) * 100.0F), scoreProvider.calculateCS(tracked),
                    scoreProvider.calculateCSPM(tracked), link
            );

            MessageEmbed eb = new EmbedBuilder()
                    .setTitle(title)
                    .addField(lpChange, changeDescription, false)
                    .addField(scoreTitle, scoreDescription, false)
                    .setDescription(description)
                    .setThumbnail(thumbnail)
                    .setFooter(footer)
                    .setColor(won ? EmbedUtils.SUCCESS_COLOR : EmbedUtils.ERROR_COLOR)
                    .build();
            this.sendMessage(eb);
        }
        else {
            Reference.LOGGER.info("Detected match was a remake, ignoring...");
        }

        this.latestEntry = newEntry;
        this.latestMatch = newMatch;
    }

    private void sendMessage(@NotNull MessageEmbed embed) {
        JDA jda = TroyTrack.getInstance().getJda();
        for (CachedGuildChannel ids : this.trackedAccount.getChannels()) {
            if (ids.getGuildId() > 0L && ids.getChannelId() > 0L) {
                Guild trackingGuild = jda.getGuildById(ids.getGuildId());
                if (trackingGuild != null) {
                    GuildChannel trackingChannel = trackingGuild.getGuildChannelById(ids.getChannelId());
                    if (trackingChannel instanceof GuildMessageChannel channel) {
                        channel.sendMessageEmbeds(embed).queue();
                    }
                }
            }
        }
    }

    private @NotNull String calculateDelta(int individualScore, int teamAverageScore) {
        int delta = individualScore - teamAverageScore;
        boolean positive = delta >= 0;
        delta = Math.abs(delta);
        return (positive ? "+" : "-") + delta;
    }
}
