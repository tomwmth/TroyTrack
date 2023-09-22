package dev.tomwmth.troytrack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class Reference {
    public static final Logger LOGGER = LoggerFactory.getLogger(TroyTrack.class.getSimpleName());

    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final Gson GSON_PRETTY = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
}
