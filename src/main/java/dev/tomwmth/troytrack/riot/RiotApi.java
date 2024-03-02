package dev.tomwmth.troytrack.riot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.tomwmth.troytrack.Config;
import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.viego.Viego;
import dev.tomwmth.viego.lol.constants.GameQueue;
import dev.tomwmth.viego.lol.constants.RankedQueue;
import dev.tomwmth.viego.lol.league.v4.LeagueV4;
import dev.tomwmth.viego.lol.league.v4.obj.LeagueEntry;
import dev.tomwmth.viego.lol.match.v5.MatchV5;
import dev.tomwmth.viego.lol.match.v5.obj.Match;
import dev.tomwmth.viego.lol.summoner.v4.SummonerV4;
import dev.tomwmth.viego.lol.summoner.v4.obj.Summoner;
import dev.tomwmth.viego.riot.account.v1.AccountV1;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.routing.Platform;
import dev.tomwmth.viego.routing.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
public class RiotApi {
    private static final MatchV5.Query LAST_MATCH_QUERY = new MatchV5.Query()
            .withMaxCount(1)
            .withQueue(GameQueue.SR_5x5_RANKED_SOLO);

    private final LoadingCache<RiotId, RiotAccount> riotAccountCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(Duration.of(5L, ChronoUnit.MINUTES))
            .concurrencyLevel(1)
            .build(new CacheLoader<>() {
                @Override @NotNull
                public RiotAccount load(@NotNull RiotId key) {
                    return AccountV1.getAccountByRiotId(key.getGameName(), key.getTagLine(), Region.EUROPE).getValue();
                }
            });

    private final LoadingCache<RiotAccount, Summoner> summonerCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(Duration.of(5L, ChronoUnit.MINUTES))
            .concurrencyLevel(1)
            .build(new CacheLoader<>() {
                @Override @NotNull
                public Summoner load(@NotNull RiotAccount key) {
                    return SummonerV4.getSummonerByPuuid(key.getPuuid(), Platform.OC1).getValue();
                }
            });

    private final Set<AccountTracker> accountTrackers = new HashSet<>();

    private final ScheduledExecutorService trackerExecutor = Executors.newSingleThreadScheduledExecutor();

    public RiotApi() {
        Viego.setApiKey(System.getProperty("riot.token"));

        var trackedAccounts = Config.getSettings().getTrackedAccounts();
        for (int i = 0; i < trackedAccounts.size(); i++) {
            var trackedAccount = trackedAccounts.get(i);
            try {
                AccountTracker tracker = new AccountTracker(this, trackedAccount.getRiotId(), trackedAccount.getPlatform());
                this.accountTrackers.add(tracker);
                this.trackerExecutor.scheduleWithFixedDelay(() -> {
                    try {
                        tracker.heartbeat();
                    }
                    catch (Exception ex) {
                        Reference.LOGGER.error("Error executing tracker heartbeat for summoner \"{}\"", tracker.getRiotId(), ex);
                    }
                }, 500L * i, 20_000L, TimeUnit.MILLISECONDS);
            }
            catch (Exception ex) {
                Reference.LOGGER.error("Error loading tracker for account \"{}\"", trackedAccount.getRiotId(), ex);
            }
        }
    }

    @NotNull
    public RiotAccount getRiotAccountById(@NotNull RiotId id) throws ExecutionException {
        return this.riotAccountCache.get(id);
    }

    @NotNull
    public Summoner getSummonerByRiotAccount(@NotNull RiotAccount account) throws ExecutionException {
        return this.summonerCache.get(account);
    }

    @Nullable
    public LeagueEntry getLeagueEntry(@NotNull Summoner summoner, @NotNull RankedQueue queue) throws IllegalStateException {
        var res = LeagueV4.getEntriesBySummonerId(summoner.getSummonerId(), Platform.OC1);
        if (res.isPresent()) {
            List<LeagueEntry> entries = res.getValue();
            Optional<LeagueEntry> filtered = entries.stream().filter(e -> e.getQueueType() == queue).findFirst();
            return filtered.orElse(null);
        }
        return null;
    }

    @Nullable
    public Match getLatestMatch(@NotNull RiotAccount account, @NotNull Platform platform) {
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
