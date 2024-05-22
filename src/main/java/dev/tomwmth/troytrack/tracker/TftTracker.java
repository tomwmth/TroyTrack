package dev.tomwmth.troytrack.tracker;

import dev.tomwmth.troytrack.obj.TrackedAccount;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 22/05/2024
 */
public final class TftTracker extends AccountTracker {
    public TftTracker(@NotNull TrackedAccount trackedAccount) {
        super(trackedAccount);
    }

    @Override
    public @NotNull String heartbeat() throws Exception {
        // TODO: TFT impl
        return "";
    }
}
