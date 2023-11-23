package dev.tomwmth.troytrack;

import com.squareup.moshi.Moshi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class Reference {
    public static final Logger LOGGER = LoggerFactory.getLogger(TroyTrack.class.getSimpleName());

    public static final Moshi MOSHI = new Moshi.Builder()
            .build();
}
