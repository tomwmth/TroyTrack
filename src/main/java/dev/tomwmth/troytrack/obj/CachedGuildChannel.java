package dev.tomwmth.troytrack.obj;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 2/03/2024
 */
@Getter
public final class CachedGuildChannel {
    private static final Map<Integer, CachedGuildChannel> CACHE = new HashMap<>();

    private final long guildId;
    private final long channelId;

    private CachedGuildChannel(long guildId, long channelId) {
        this.guildId = guildId;
        this.channelId = channelId;
    }

    public static CachedGuildChannel of(long guildId, long channelId) {
        int hash = generateHash(guildId, channelId);
        if (CACHE.containsKey(hash)) {
            return CACHE.get(hash);
        }
        CachedGuildChannel guildChannel = new CachedGuildChannel(guildId, channelId);
        CACHE.put(hash, guildChannel);
        return guildChannel;
    }

    private static int generateHash(long guildId, long channelId) {
        long combined = (guildId << 32) | (channelId & 0xFFFFFFFFL);
        return (int) (combined ^ (combined >>> 32));
    }
}
