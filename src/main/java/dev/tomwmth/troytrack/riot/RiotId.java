package dev.tomwmth.troytrack.riot;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 23/11/2023
 */
@Getter
public final class RiotId {
    private final String gameName;
    private final String tagLine;

    public RiotId(String gameName, String tagLine) {
        this.gameName = gameName;
        this.tagLine = tagLine;
    }

    @Override
    public String toString() {
        return String.format("%s#%s", this.gameName, this.tagLine);
    }

    @Nullable
    public static RiotId parse(@NotNull String id) throws IllegalArgumentException {
        String[] split = id.split("#");
        if (split.length == 2)
            return new RiotId(split[0], split[1]);
        else return null;
    }

    public static final class Adapter extends TypeAdapter<RiotId> {
        @Override
        public void write(JsonWriter out, RiotId value) throws IOException {
            if (value != null)
                out.value(value.toString());
            else out.nullValue();
        }

        @Override
        public RiotId read(JsonReader in) throws IOException {
            String value = in.nextString();
            return RiotId.parse(value);
        }
    }
}
