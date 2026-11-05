package ru.moonvisual.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import ru.moonvisual.core.Feature;
import ru.moonvisual.core.FeatureManager;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static int autosaveTimer = 0;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("moonvisual.json");
    }

    public static void load() {
        try {
            if (Files.notExists(path())) return;
            JsonObject o = JsonParser.parseString(Files.readString(path())).getAsJsonObject();
            for (var en : o.entrySet()) {
                Feature f = FeatureManager.MAP.get(en.getKey());
                if (f == null) continue;
                JsonObject e = en.getValue().getAsJsonObject();
                f.enabled = e.get("e").getAsBoolean();
                f.value = e.get("v").getAsFloat();
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            for (Feature f : FeatureManager.MAP.values()) {
                JsonObject e = new JsonObject();
                e.addProperty("e", f.enabled);
                e.addProperty("v", f.value);
                o.add(f.id, e);
            }
            Files.writeString(path(), new GsonBuilder().setPrettyPrinting().create().toJson(o));
        } catch (Exception ignored) {
        }
    }

    public static void tick() {
        autosaveTimer++;
        if (autosaveTimer >= 1200) {
            autosaveTimer = 0;
            if (FeatureManager.on("auto_save")) save();
        }
    }
}
