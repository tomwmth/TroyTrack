package dev.tomwmth.troytrack.tracker.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.tomwmth.troytrack.util.object.Pair;
import dev.tomwmth.viego.lol.constants.GameQueue;
import dev.tomwmth.viego.lol.constants.RankedQueue;
import dev.tomwmth.viego.lol.league.v4.LeagueV4;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.lol.match.v5.MatchV5;
import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.summoner.v4.SummonerV4;
import dev.tomwmth.viego.lol.summoner.v4.obj.Summoner;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.routing.Platform;
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
public final class LeagueOfLegends {
    private static final MatchV5.Query LAST_MATCH_QUERY = new MatchV5.Query()
            .withMaxCount(1)
            .withQueue(GameQueue.SR_5x5_RANKED_SOLO);

    private final LoadingCache<Pair<RiotAccount, Platform>, Summoner> summonerCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(Duration.of(5L, ChronoUnit.MINUTES))
            .concurrencyLevel(1)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Summoner load(@NotNull Pair<RiotAccount, Platform> pair) {
                    return SummonerV4.getSummonerByPuuid(pair.getFirst().getPuuid(), pair.getSecond()).getValue();
                }
            });

    public @NotNull Summoner getSummonerByRiotAccount(@NotNull RiotAccount account, @NotNull Platform platform) throws ExecutionException {
        return this.summonerCache.get(Pair.of(account, platform));
    }

    public @Nullable LeagueEntry getLeagueEntry(@NotNull Summoner summoner, @NotNull RankedQueue queue, @NotNull Platform platform) throws IllegalStateException {
        var res = LeagueV4.getEntriesBySummonerId(summoner.getSummonerId(), platform);
        if (res.isPresent()) {
            List<LeagueEntry> entries = res.getValue();
            Optional<LeagueEntry> filtered = entries.stream().filter(e -> e.getQueueType() == queue).findFirst();
            return filtered.orElse(null);
        }
        return null;
    }

    public @Nullable Match getLatestMatch(@NotNull RiotAccount account, @NotNull Platform platform) {
        var idResponse = MatchV5.getMatchIdsByPuuid(account.getPuuid(), LAST_MATCH_QUERY, platform);
        if (idResponse.isPresent()) {
            List<String> matchIds = idResponse.getValue();
            if (!matchIds.isEmpty()) {
                var matchResponse = MatchV5.getMatchById(matchIds.get(0), platform);
                if (matchResponse.isPresent()) {
                    return matchResponse.getValue();
                }
            }
        }
        return null;
    }
}
