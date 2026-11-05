package ru.moonvisual.core;

import java.util.LinkedHashMap;

public class FeatureManager {
    public static final LinkedHashMap<String, Feature> MAP = new LinkedHashMap<>();

    public static void registerAll() {
        MAP.clear();
        Features.init();
        for (Feature f : Features.all()) {
            MAP.put(f.id, f);
        }
    }

    public static void add(Feature f) {
        MAP.put(f.id, f);
    }

    public static Feature f(String id) {
        return MAP.get(id);
    }

    public static boolean on(String id) {
        Feature f = MAP.get(id);
        return f != null && f.active();
    }

    public static float num(String id) {
        Feature f = MAP.get(id);
        return f == null ? 0f : f.value;
    }

    public static int color(String id) {
        Feature f = MAP.get(id);
        return f == null ? 0xFFFFFF : ((int) f.value) & 0xFFFFFF;
    }

    public static int opt(String id) {
        Feature f = MAP.get(id);
        if (f == null || f.options == null) return 0;
        int i = (int) f.value;
        return Math.max(0, Math.min(f.options.length - 1, i));
    }

    public static String option(String id) {
        Feature f = MAP.get(id);
        return f == null ? "?" : f.optionName(opt(id));
    }
}
