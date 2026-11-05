package ru.moonvisual.lang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.moonvisual.core.FeatureManager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Lang {
    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> RU = new HashMap<>();
    private static Map<String, String> cur = EN;

    public static void loadAll() {
        load(EN, "/assets/moonvisual/lang/en_us.json");
        load(RU, "/assets/moonvisual/lang/ru_ru.json");
        reload();
    }

    private static void load(Map<String, String> map, String path) {
        try (InputStreamReader r = new InputStreamReader(
                Lang.class.getResourceAsStream(path), StandardCharsets.UTF_8)) {
            JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
            for (var e : o.entrySet()) map.put(e.getKey(), e.getValue().getAsString());
        } catch (Exception ignored) {
        }
    }

    public static void reload() {
        cur = FeatureManager.opt("language") == 0 ? EN : RU;
    }

    public static String t(String key) {
        String v = cur.get(key);
        return v == null ? key : v;
    }
}
