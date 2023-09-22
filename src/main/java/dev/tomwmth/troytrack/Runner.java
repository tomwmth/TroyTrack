package dev.tomwmth.troytrack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

/**
 * @author Thomas Wearmouth <tomwmth@pm.me>
 * Created on 10/09/2023
 */
public class Runner {
    public static void main(String[] args) {
        File dotEnvFile = new File(".env");
        if (dotEnvFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dotEnvFile))) {
                var lines = reader.lines().toList();
                for (var line : lines) {
                    if (!line.contains("="))
                        continue;

                    String key = line.substring(0, line.indexOf('='));
                    String value = line.substring(line.indexOf('=') + 1);
                    System.setProperty(key, value);
                }
            }
            catch (Exception ex) {
                Reference.LOGGER.error("Unable to read .env file", ex);
            }
        }

        Reference.LOGGER.info("Starting bot...");
        Thread thread = new Thread(TroyTrack::new);
        thread.setDaemon(true);
        thread.start();

        new Thread(() -> {
            var scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNext()) {
                    switch (scanner.nextLine().trim().toLowerCase()) {
                        case "exit", "stop" -> {
                            Reference.LOGGER.info("Stopping all processes...");
                            System.exit(0);
                        }
                        default -> {
                            Reference.LOGGER.info("Try using 'exit' or 'stop' to halt this program");
                        }
                    }
                }
            }
        }).start();
    }
}
