package dev.tomwmth.troytrack.tracker.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.routing.Platform;
import dev.tomwmth.viego.tft.summoner.v1.SummonerV1;
import dev.tomwmth.viego.tft.summoner.v1.obj.Summoner;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public final class TeamfightTactics {
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

}
