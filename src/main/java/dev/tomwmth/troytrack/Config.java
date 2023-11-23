package dev.tomwmth.troytrack;

import com.squareup.moshi.JsonAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class Config {
    private static final Config INSTANCE = new Config(new File("config.json"));
    private static final JsonAdapter<Settings> SETTINGS_ADAPTER = Reference.MOSHI.adapter(Settings.class).nonNull();

    private transient final File configFile;

    private Settings settings;

    public Config(@NotNull File configFile) {
        this.configFile = configFile;
        this.load();
    }

    public void load() {
        if (!this.configFile.exists()) {
            this.settings = new Settings();
            this.save();
            return;
        }
        try {
            String json = Files.readString(this.configFile.toPath());
            this.settings = SETTINGS_ADAPTER.fromJson(json);
        }
        catch (Exception ex) {
            Reference.LOGGER.error("Unable to read config file", ex);
            this.settings = new Settings();
            this.save();
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile))) {
            String json = SETTINGS_ADAPTER.toJson(this.settings);
            writer.write(json);
        }
        catch (Exception ex) {
            Reference.LOGGER.error("Unable to write config file", ex);
        }
    }

    @NotNull
    public static Config getInstance() {
        return INSTANCE;
    }

    @NotNull
    public static Settings getSettings() {
        return INSTANCE.settings;
    }

    public static final class Settings {
        public long adminUserId = 137143359050350592L;

        public long trackingGuildId = 0L;

        public long trackingChannelId = 0L;

        public List<RiotId> trackedSummoners = List.of(new RiotId("Hason", "OCE"));

        public static final class RiotId {
            public String gameName;
            public String tagLine;

            public RiotId(String gameName, String tagLine) {
                this.gameName = gameName;
                this.tagLine = tagLine;
            }
        }
    }
}
