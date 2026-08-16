package top.csituka.sparkle_craft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import top.csituka.sparkle_craft.sparkle_craft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("sparkle-craft.json");
    private static final ConfigData data = load();

    private ModConfig() {
    }

    public static boolean showFlyBeaconBarrier() {
        return data.showFlyBeaconBarrier;
    }

    public static void setShowFlyBeaconBarrier(boolean value) {
        data.showFlyBeaconBarrier = value;
        save();
    }

    private static ConfigData load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                ConfigData loaded = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigData.class);
                if (loaded != null) {
                    return loaded;
                }
            }
        } catch (IOException | JsonParseException e) {
            sparkle_craft.LOGGER.warn("Failed to load config from {}", CONFIG_PATH, e);
        }
        return new ConfigData();
    }

    private static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            sparkle_craft.LOGGER.warn("Failed to save config to {}", CONFIG_PATH, e);
        }
    }

    private static class ConfigData {
        public boolean showFlyBeaconBarrier = true;
    }
}
