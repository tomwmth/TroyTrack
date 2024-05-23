package dev.tomwmth.troytrack.tracker.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.routing.Platform;
import dev.tomwmth.viego.tft.constants.GameQueue;
import dev.tomwmth.viego.tft.constants.RankedQueue;
import dev.tomwmth.viego.tft.league.v1.LeagueV1;
import dev.tomwmth.viego.tft.league.v1.obj.LeagueEntry;
import dev.tomwmth.viego.tft.match.v1.MatchV1;
import dev.tomwmth.viego.tft.match.v1.obj.Match;
import dev.tomwmth.viego.tft.summoner.v1.SummonerV1;
import dev.tomwmth.viego.tft.summoner.v1.obj.Summoner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public final class TeamfightTactics {
    private static final MatchV1.Query LATEST_MATCHES_QUERY = new MatchV1.Query()
            .withMaxCount(10);

    private final LoadingCache<RiotAccount, Summoner> summonerCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(Duration.of(5L, ChronoUnit.MINUTES))
            .concurrencyLevel(1)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Summoner load(@NotNull RiotAccount key) {
                    return SummonerV1.getSummonerByPuuid(key.getPuuid(), Platform.OC1).getValue();
                }
            });

    public @NotNull Summoner getSummonerByRiotAccount(@NotNull RiotAccount account) throws ExecutionException {
        return this.summonerCache.get(account);
    }

    public @Nullable LeagueEntry getLeagueEntry(@NotNull Summoner summoner, @NotNull RankedQueue queue, @NotNull Platform platform) throws IllegalStateException {
        var res = LeagueV1.getEntriesBySummonerId(summoner.getSummonerId(), platform);
        if (res.isPresent()) {
            List<LeagueEntry> entries = res.getValue();
            Optional<LeagueEntry> filtered = entries.stream().filter(e -> e.getQueueType() == queue).findFirst();
            return filtered.orElse(null);
        }
        return null;
    }

    /**
     * @see <a href="https://github.com/RiotGames/developer-relations/issues/935">Issue #935: [FEATURE-REQUEST] Allow filtering TFT matches by queue ID and/or type</a>
     */
    public @Nullable Match getLatestMatch(@NotNull RiotAccount account, @NotNull Platform platform) {
        var idResponse = MatchV1.getMatchIdsByPuuid(account.getPuuid(), LATEST_MATCHES_QUERY, platform);
        if (idResponse.isPresent()) {
            List<String> matchIds = idResponse.getValue();
            if (!matchIds.isEmpty()) {
                for (String matchId : matchIds) {
                    var matchResponse = MatchV1.getMatchById(matchId, platform);
                    if (matchResponse.isPresent()) {
                        var match = matchResponse.getValue();
                        if (match.getInfo().getQueueId() == GameQueue.TFT_RANKED.getId()
                                && match.getInfo().getMode().equals("standard")) {
                            return match;
                        }
                    }
                }
            }
        }
        return null;
    }
}
