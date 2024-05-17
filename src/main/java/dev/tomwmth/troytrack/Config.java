package dev.tomwmth.troytrack;

import dev.tomwmth.troytrack.obj.CachedGuildChannel;
import dev.tomwmth.troytrack.obj.TrackedAccount;
import dev.tomwmth.troytrack.riot.RiotId;
import dev.tomwmth.viego.internal.RiotGame;
import dev.tomwmth.viego.routing.Platform;
import lombok.Getter;
import lombok.Setter;
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

    @NotNull
    public static Config getInstance() {
        return INSTANCE;
    }

    @NotNull
    public static Settings getSettings() {
        return INSTANCE.settings;
    }

    public void load() {
        if (!this.configFile.exists()) {
            this.settings = new Settings();
            this.save();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(this.configFile))) {
            this.settings = Reference.GSON.fromJson(reader, Settings.class);
        } catch (Exception ex) {
            Reference.LOGGER.error("Unable to read config file", ex);
            this.settings = new Settings();
            this.save();
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.configFile))) {
            Reference.GSON_PRETTY.toJson(this.settings, writer);
        } catch (Exception ex) {
            Reference.LOGGER.error("Unable to write config file", ex);
        }
    }

    @Getter
    @Setter
    public static final class Settings {
        private long adminUserId = 137143359050350592L;

        private List<TrackedAccount> trackedAccounts = List.of(
                new TrackedAccount(
                        RiotId.parse("Hason#OCE"),
                        Platform.OC1,
                        List.of(CachedGuildChannel.of(-1L, -1L)),
                        List.of(RiotGame.LEAGUE_OF_LEGENDS)
                )
        );
    }
}
