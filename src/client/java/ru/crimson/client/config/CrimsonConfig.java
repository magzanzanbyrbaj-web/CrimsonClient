
package ru.crimson.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class ConfigManager {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path DIRECTORY =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("crimsonclient")
                    .resolve("configs");

    private ConfigManager() {}

    public static List<String> list() {
        try {
            Files.createDirectories(DIRECTORY);

            try (Stream<Path> files = Files.list(DIRECTORY)) {
                return files
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .map(path -> path.getFileName()
                                .toString()
                                .replaceFirst("\\.json$", ""))
                        .sorted()
                        .toList();
            }
        } catch (IOException exception) {
            return List.of();
        }
    }

    public static boolean save(String name, CrimsonConfig config) {
        Path path = resolve(name);

        if (path == null || config == null) {
            return false;
        }

        try {
            Files.createDirectories(DIRECTORY);
            Files.writeString(path, GSON.toJson(config));
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public static boolean load(String name) {
        Path path = resolve(name);

        if (path == null || !Files.isRegularFile(path)) {
            return false;
        }

        try {
            CrimsonConfig loaded = GSON.fromJson(
                    Files.readString(path),
                    CrimsonConfig.class
            );

            if (loaded == null) {
                return false;
            }

            ru.crimson.client.CrimsonClientClient.config = loaded;
            loaded.save();

            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static Path getDirectory() {
        try {
            Files.createDirectories(DIRECTORY);
        } catch (IOException ignored) {
        }

        return DIRECTORY;
    }

    private static Path resolve(String name) {
        if (name == null) {
            return null;
        }

        String safeName = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]", "_");

        if (safeName.isBlank() || safeName.length() > 32) {
            return null;
        }

        return DIRECTORY.resolve(safeName + ".json").normalize();
    }
}
