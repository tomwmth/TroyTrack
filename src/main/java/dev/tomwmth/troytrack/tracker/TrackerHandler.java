package dev.tomwmth.troytrack.tracker;

import dev.tomwmth.troytrack.Config;
import dev.tomwmth.troytrack.Reference;
import dev.tomwmth.viego.internal.RiotGame;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public final class TrackerHandler {
    @Getter
    private final Set<AccountTracker> trackers = new HashSet<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TrackerHandler() {
        var trackedAccounts = Config.getSettings().getTrackedAccounts();
        for (int i = 0; i < trackedAccounts.size(); i++) {
            var trackedAccount = trackedAccounts.get(i);
            for (RiotGame game : trackedAccount.getGames()) {
                try {
                    AccountTracker tracker = switch (game) {
                        case LEAGUE_OF_LEGENDS -> new LeagueTracker(trackedAccount);
                        case TEAMFIGHT_TACTICS -> new TftTracker(trackedAccount);
                    };

                    this.trackers.add(tracker);
                    this.executor.scheduleWithFixedDelay(() -> {
                        try {
                            String status = tracker.heartbeat();
                            Reference.LOGGER.info("Heartbeat completed [{} : {}] - {}", trackedAccount.getRiotId(), game.toDisplayableString(), status);
                        } catch (Exception ex) {
                            Reference.LOGGER.error("Error executing tracker heartbeat [{} : {}]", trackedAccount.getRiotId(), game.toDisplayableString(), ex);
                        }
                    }, 500L * i, 20_000L, TimeUnit.MILLISECONDS);
                }
                catch (Exception ex) {
                    Reference.LOGGER.error("Error loading tracker [{} : {}]", trackedAccount.getRiotId(), game.toDisplayableString(), ex);
                }
            }
        }
    }

    public void shutdown() {
        this.executor.shutdown();
    }
}
