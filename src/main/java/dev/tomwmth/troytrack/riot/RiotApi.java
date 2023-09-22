package dev.tomwmth.troytrack.riot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hawolt.Javan;
import com.hawolt.api.MatchAPI;
import com.hawolt.data.api.Queue;
import com.hawolt.data.api.RankedQueue;
import com.hawolt.data.routing.Platform;
import com.hawolt.data.routing.Region;
import com.hawolt.dto.league.v4.LeagueEntryDTO;
import com.hawolt.dto.match.v5.match.MatchDto;
import com.hawolt.dto.summoner.v4.SummonerDto;
import com.hawolt.exceptions.DataNotFoundException;
import dev.tomwmth.troytrack.Config;
import dev.tomwmth.troytrack.Reference;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
    public static final Region REGION = Region.SEA;
    public static final Platform PLATFORM = Platform.OC1;
    public static final MatchAPI.Query LAST_MATCH_QUERY = MatchAPI.Query.builder()
            .setCount(1)
            .setQueue(Queue.SR_5x5_RANKED_SOLO)
            .build();

    private final LoadingCache<String, SummonerDto> summonerCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(Duration.of(5L, ChronoUnit.MINUTES))
            .concurrencyLevel(1)
            .build(new CacheLoader<>() {
                @Override @NotNull
                public SummonerDto load(@NotNull String key) throws IOException, DataNotFoundException {
                    return Javan.getSummonerByName(key);
                }
            });

    private final Set<SummonerTracker> summonerTrackers = new HashSet<>();

    private final ScheduledExecutorService trackerExecutor = Executors.newSingleThreadScheduledExecutor();

    public RiotApi() {
        Javan.setRiotAPIKey(System.getProperty("riot.token"));
        Javan.setDefaultRegion(REGION);
        Javan.setDefaultPlatform(PLATFORM);

        List<String> trackedSummoners = Config.getSettings().trackedSummoners;
        for (int i = 0; i < trackedSummoners.size(); i++) {
            String summonerName = trackedSummoners.get(i);
            try {
                SummonerTracker tracker = new SummonerTracker(this, summonerName);
                this.summonerTrackers.add(tracker);
                this.trackerExecutor.scheduleWithFixedDelay(() -> {
                    try {
                        tracker.heartbeat();
                    }
                    catch (Exception ex) {
                        Reference.LOGGER.error("Error executing tracker heartbeat for summoner \"" + tracker.getSummonerName() + "\"", ex);
                    }
                }, 500L * i, 20_000L, TimeUnit.MILLISECONDS);
            }
            catch (Exception ex) {
                Reference.LOGGER.error("Error loading tracker for summoner \"" + summonerName + "\"", ex);
            }
        }
    }

    @NotNull
    public SummonerDto getSummonerByName(@NotNull String name) throws ExecutionException {
        return this.summonerCache.get(name);
    }

    @NotNull
    public LeagueEntryDTO getLeagueEntry(@NotNull SummonerDto summonerDto, @NotNull RankedQueue queue) throws DataNotFoundException, IOException, IllegalStateException {
        List<LeagueEntryDTO> entryList = Javan.getLeagueEntriesForSummoner(summonerDto.getId());
        if (!entryList.isEmpty()) {
            Optional<LeagueEntryDTO> filtered = entryList.stream().filter(e -> e.getQueue() == queue).findFirst();
            if (filtered.isPresent()) {
                return filtered.get();
            }
            throw new IllegalStateException("Returned league entry list contained no entry for " + queue.name());
        }
        throw new IllegalStateException("Returned league entry list was empty");
    }

    @NotNull
    public MatchDto getLatestMatch(@NotNull SummonerDto summoner) throws DataNotFoundException, IOException, IllegalStateException {
        List<String> matchList = MatchAPI.getMatchListByPUUID(RiotApi.PLATFORM, summoner.getPUUID(), RiotApi.LAST_MATCH_QUERY);
        if (!matchList.isEmpty()) {
            return MatchAPI.getMatch(matchList.get(0));
        }
        throw new IllegalStateException("Returned match list was empty");
    }
}
