package dev.tomwmth.troytrack.command;

import dev.tomwmth.troytrack.TroyTrack;
import dev.tomwmth.troytrack.command.base.Command;
import dev.tomwmth.troytrack.command.base.annotation.Option;
import dev.tomwmth.troytrack.command.base.annotation.SlashCommand;
import dev.tomwmth.troytrack.riot.RiotId;
import dev.tomwmth.troytrack.util.EmbedUtils;
import dev.tomwmth.troytrack.util.LeagueUtils;
import dev.tomwmth.troytrack.util.enums.RankIcon;
import dev.tomwmth.viego.HttpStatus;
import dev.tomwmth.viego.lol.constants.RankedQueue;
import dev.tomwmth.viego.lol.league.v4.LeagueV4;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.lol.summoner.v4.SummonerV4;
import dev.tomwmth.viego.riot.account.v1.AccountV1;
import dev.tomwmth.viego.routing.Platform;
import dev.tomwmth.viego.routing.Region;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
                        @Option(name = "riotId", desc = "The Riot ID of the player to search") String id,
                        @Option(name = "region", desc = "The region to search for the player") Platform platform,
                        @Option(name = "queue", desc = "The ranked queue to search for the player", required = false) RankedQueue queue) {
        event.deferReply().queue();

        try {
            RiotId riotId = RiotId.parse(id);
            if (riotId == null) {
                event.getHook().editOriginalEmbeds(
                        EmbedUtils.failure("Not a valid Riot ID")
                                .build()
                ).queue();
                return;
            }

            AccountV1.getAccountByRiotId(riotId.getGameName(), riotId.getTagLine(), Region.EUROPE).ifPresentOrElse(account -> {
                SummonerV4.getSummonerByPuuid(account.getPuuid(), platform).ifPresentOrElse(summoner -> {
                    LeagueV4.getEntriesBySummonerId(summoner.getSummonerId(), platform).ifPresentOrElse(entries -> {
                        Optional<LeagueEntry> opt = entries.stream().filter(e -> e.getQueueType() == (queue == null ? RankedQueue.RANKED_SOLO_5x5 : queue)).findFirst();
                        if (opt.isPresent()) {
                            LeagueEntry leagueEntry = opt.get();
                            int gamesWon = leagueEntry.getWins(), gamesLost = leagueEntry.getLosses();
                            int gamesTotal = gamesWon + gamesLost;
                            int winRate = LeagueUtils.calculateWinRate(gamesWon, gamesTotal);

                            String title = TITLE_TEMPLATE.formatted(account.getDisplayId(), LeagueUtils.getRankString(leagueEntry));
                            String statsDescription = STATS_DESCRIPTION_TEMPLATE.formatted(gamesTotal, gamesWon, gamesLost, winRate);
                            String flagsDescription = FLAGS_DESCRIPTION_TEMPLATE.formatted(
                                    leagueEntry.isHotStreak()  ? YES : NO,
                                    leagueEntry.isVeteran()    ? YES : NO,
                                    leagueEntry.isFreshBlood() ? YES : NO,
                                    leagueEntry.isInactive()   ? YES : NO
                            );

                            event.getHook().editOriginalEmbeds(
                                    EmbedUtils.of(title, null)
                                            .addField(STATS_TITLE_TEMPLATE, statsDescription, true)
                                            .addField(FLAGS_TITLE_TEMPLATE, flagsDescription, true)
                                            .setThumbnail(RankIcon.valueOf(leagueEntry.getTier()).getIcon())
                                            .build()
                            ).queue();
                        }
                        else {
                            String title = TITLE_TEMPLATE.formatted(account.getDisplayId(), "UNRANKED");
                            event.getHook().editOriginalEmbeds(
                                    EmbedUtils.of(title, null)
                                            .setThumbnail(RankIcon.UNRANKED.getIcon())
                                            .build()
                            ).queue();
                        }
                    }, status -> this.onFailure(event, status));
                }, status -> this.onFailure(event, status));
            }, status -> this.onFailure(event, status));
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isGuildOnly() {
        return false;
    }

    private void onFailure(SlashCommandInteractionEvent event, HttpStatus status) {
        event.getHook().editOriginalEmbeds(
                EmbedUtils.failure(String.format("API responded with `%s`", status))
                        .build()
        ).queue();
    }
}
