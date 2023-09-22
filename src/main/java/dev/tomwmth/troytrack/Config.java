package dev.tomwmth.troytrack;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 15/09/2023
 */
public class Config {
    private static final Config INSTANCE = new Config(new File("config.json"));

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
        try (BufferedReader reader = new BufferedReader(new FileReader(this.configFile))) {
            this.settings = Reference.GSON.fromJson(reader, Settings.class);
        }
        catch (Exception ex) {
            Reference.LOGGER.error("Unable to read config file", ex);
            this.settings = new Settings();
            this.save();
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile))) {
            Reference.GSON_PRETTY.toJson(this.settings, writer);
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
        @SerializedName("admin_user_id")
        public long adminUserId = 137143359050350592L;

        @SerializedName("tracking_guild_id")
        public long trackingGuildId = 0L;

        @SerializedName("tracking_channel_id")
        public long trackingChannelId = 0L;

        @SerializedName("tracked_summoners")
        public List<String> trackedSummoners = List.of("Hason");
    }
}
