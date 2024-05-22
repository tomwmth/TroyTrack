package dev.tomwmth.troytrack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.tomwmth.troytrack.tracker.RiotId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class Reference {
    public static final Logger LOGGER = LogManager.getLogger(TroyTrack.class.getSimpleName());

    public static final Gson GSON = gson()
            .create();

    public static final Gson GSON_PRETTY = gson()
            .setPrettyPrinting()
            .create();

    private static GsonBuilder gson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(RiotId.class, new RiotId.Adapter());
    }
}
