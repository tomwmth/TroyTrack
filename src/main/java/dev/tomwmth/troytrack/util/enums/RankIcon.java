package dev.tomwmth.troytrack.util.enums;

import com.hawolt.data.api.RankedTier;
import lombok.Getter;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public enum RankIcon {
    IRON(RankedTier.IRON, 1152005327370322020L),
    BRONZE(RankedTier.BRONZE, 1152005297372680213L),
    SILVER(RankedTier.SILVER, 1152005341899391136L),
    GOLD(RankedTier.GOLD, 1152005319698956368L),
    PLATINUM(RankedTier.PLATINUM, 1152005336681680928L),
    EMERALD(RankedTier.EMERALD, 1152005313260683265L),
    DIAMOND(RankedTier.DIAMOND, 1152005308533710858L),
    MASTER(RankedTier.MASTER, 1152005332302827580L),
    GRANDMASTER(RankedTier.GRANDMASTER, 1152005324585308280L),
    CHALLENGER(RankedTier.CHALLENGER, 1152005301080428616L);

    private static final String CDRAGON_PATH = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-shared-components/global/default/images/%s.png";
    private static final String EMOJI_FORMAT = "<:%s:%d>";

    @Getter
    private final RankedTier tier;
    private final long emojiId;

    RankIcon(RankedTier tier, long emojiId) {
        this.tier = tier;
        this.emojiId = emojiId;
    }

    public String getIcon() {
        return CDRAGON_PATH.formatted(this.name().toLowerCase());
    }

    public String getEmoji() {
        return EMOJI_FORMAT.formatted(this.name().toLowerCase(), this.emojiId);
    }

    public static RankIcon valueOf(RankedTier rankedTier) {
        if (rankedTier == null) {
            throw new NullPointerException("RankedTier is null");
        }

        for (RankIcon icon : values()) {
            if (icon.getTier() == rankedTier) {
                return icon;
            }
        }

        throw new IllegalArgumentException("No enum constant matching " + rankedTier.getClass().getName());
    }
}
