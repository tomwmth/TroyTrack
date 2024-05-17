package dev.tomwmth.troytrack.obj;

import dev.tomwmth.troytrack.riot.RiotId;
import dev.tomwmth.viego.internal.RiotGame;
import dev.tomwmth.viego.routing.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 2/03/2024
 */
@Getter
@AllArgsConstructor
public final class TrackedAccount {
    private final RiotId riotId;
    private final Platform platform;
    private final List<CachedGuildChannel> channels;
    private final List<RiotGame> games;
}
