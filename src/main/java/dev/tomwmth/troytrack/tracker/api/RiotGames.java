package dev.tomwmth.troytrack.tracker.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.tomwmth.troytrack.tracker.RiotId;
import dev.tomwmth.viego.ApiCredentials;
import dev.tomwmth.viego.RestResponse;
import dev.tomwmth.viego.Viego;
import dev.tomwmth.viego.riot.account.v1.AccountV1;
import dev.tomwmth.viego.riot.account.v1.obj.RiotAccount;
import dev.tomwmth.viego.routing.Region;
import lombok.Getter;
import net.dv8tion.jda.api.exceptions.HttpException;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public final class RiotGames {
    private static final RiotGames INSTANCE = new RiotGames();

    private final LoadingCache<RiotId, RiotAccount> riotAccountCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(Duration.of(5L, ChronoUnit.MINUTES))
            .concurrencyLevel(1)
            .build(new CacheLoader<>() {
                @Override
                @NotNull
                public RiotAccount load(@NotNull RiotId key) {
                    RestResponse<RiotAccount> response = AccountV1.getAccountByRiotId(key.getGameName(), key.getTagLine(), Region.EUROPE);
                    if (response.isPresent()) {
                        return response.getValue();
                    }
                    else {
                        throw new HttpException("Server returned " + response.getStatus());
                    }
                }
            });

    @Getter
    private final LeagueOfLegends lolApi = new LeagueOfLegends();

    @Getter
    private final TeamfightTactics tftApi = new TeamfightTactics();

    private RiotGames() {
        Viego.setApiCredentials(
                ApiCredentials.builder()
                        .leagueOfLegends(System.getProperty("riot.lol.token"))
//                        .teamfightTactics(System.getProperty("riot.tft.token"))
                        .build()
        );
    }

    public @NotNull RiotAccount getRiotAccountById(@NotNull RiotId id) throws ExecutionException {
        return this.riotAccountCache.get(id);
    }

    public static @NotNull RiotGames getInstance() {
        return INSTANCE;
    }
}
