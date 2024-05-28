package dev.tomwmth.troytrack.tracker;

import dev.tomwmth.troytrack.obj.TrackedAccount;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.TftUtils;
import dev.tomwmth.troytrack.util.Utils;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.tft.constants.RankedQueue;
import dev.tomwmth.viego.tft.league.v1.obj.LeagueEntry;
import dev.tomwmth.viego.tft.match.v1.obj.Match;
import dev.tomwmth.viego.tft.match.v1.obj.Participant;
import dev.tomwmth.viego.tft.summoner.v1.obj.Summoner;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public final class TftTracker extends AccountTracker {
    private static final String GAME_INFO_PATH = "https://tactics.tools/player/%s/%s/%s";
    private static final String SUMM_ICON_PATH = "https://cdn.communitydragon.org/latest/profile-icon/%d";
    private static final String TITLE_TEMPLATE = "%s just went %s%s";
    private static final String DESCRIPTION_TEMPLATE = """
            %s
            
            You can view the full game recap [here](%s).
            """;
    private static final String DESCRIPTION_LINE_TEMPLATE = "● **%d** %s";
    private static final String FOOTER_TEMPLATE = "Match ID: %s";
    private static final String CHANGE_DESCRIPTION_TEMPLATE = "%s ➜ %s";

    @Nullable
    private LeagueEntry latestEntry;

    @Nullable
    private Match latestMatch;

    public TftTracker(@NotNull TrackedAccount trackedAccount) throws Exception {
        super(trackedAccount);

        RiotAccount account = this.api.getRiotAccountById(this.trackedAccount.getRiotId());
        Summoner summoner = this.api.getTftApi().getSummonerByRiotAccount(account, this.trackedAccount.getPlatform());
        this.latestEntry = this.api.getTftApi().getLeagueEntry(summoner, RankedQueue.RANKED_STANDARD, this.trackedAccount.getPlatform());
        this.latestMatch = this.api.getTftApi().getLatestMatch(account, this.trackedAccount.getPlatform());
    }

    @Override
    public @NotNull String heartbeat() throws Exception {
        String message = null;

        RiotAccount account = this.api.getRiotAccountById(this.trackedAccount.getRiotId());
        Match otherMatch = this.api.getTftApi().getLatestMatch(account, this.trackedAccount.getPlatform());

        if (otherMatch != null) {
            Summoner summoner = this.api.getTftApi().getSummonerByRiotAccount(account, this.trackedAccount.getPlatform());
            LeagueEntry otherEntry = this.api.getTftApi().getLeagueEntry(summoner, RankedQueue.RANKED_STANDARD, this.trackedAccount.getPlatform());
            if (this.latestMatch == null || !otherMatch.getMetadata().getId().equals(this.latestMatch.getMetadata().getId())) {
                this.processNewMatch(summoner, otherMatch, otherEntry);
                message = "processed new match";
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

        int placement = tracked.getPlacement();
        String matchId = newMatch.getMetadata().getId().split("_")[1];
        String thumbnail = SUMM_ICON_PATH.formatted(summoner.getIconId());
        String title = TITLE_TEMPLATE.formatted(this.trackedAccount.getRiotId().toString(), Utils.ordinal(placement), placement <= 4 ? "!" : ".");
        String previousRankString = TftUtils.getAbbreviatedRankString(this.latestEntry);
        String newRankString = TftUtils.getAbbreviatedRankString(newEntry);
        String lpChange = TftUtils.calculateLeaguePointChange(this.latestEntry, newEntry);
        String changeDescription = CHANGE_DESCRIPTION_TEMPLATE.formatted(previousRankString, newRankString);
        String footer = FOOTER_TEMPLATE.formatted(matchId);

        String link = GAME_INFO_PATH.formatted(
                this.trackedAccount.getRiotId().getGameName(),
                this.trackedAccount.getRiotId().getTagLine(),
                newMatch.getMetadata().getId()
        );
        String description = DESCRIPTION_TEMPLATE.formatted(TftUtils.generateTraitList(tracked.getTraits(), DESCRIPTION_LINE_TEMPLATE), link);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setThumbnail(thumbnail)
                .setFooter(footer)
                .setColor(placement <= 4 ? (placement == 1 ? EmbedUtils.SPECIAL_COLOR : EmbedUtils.SUCCESS_COLOR) : EmbedUtils.ERROR_COLOR);

        if (newEntry != null) {
            eb.addField(lpChange, changeDescription, false);
        }

        this.sendMessage(eb.build());
    }
}
