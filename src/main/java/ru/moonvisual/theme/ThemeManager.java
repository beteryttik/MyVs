package ru.moonvisual.theme;

import ru.moonvisual.core.FeatureManager;

import java.awt.Color;

public class ThemeManager {
    private static final Theme[] THEMES = {
            new Theme("Moonlight", 0xF00B0D12, 0xF0141620, 0x7FD9FF, 0xE6EDF7),
            new Theme("Dark",      0xF00C0C0E, 0xF0151518, 0x9BA3AE, 0xE8E8EC),
            new Theme("Light",     0xF0F2F5FA, 0xF0FFFFFF, 0x4A90D9, 0xFF20242C),
            new Theme("Bloodmoon", 0xF017080C, 0xF0251016, 0xFF4455, 0xF5DDE1),
            new Theme("Sakura",    0xF01A121C, 0xF02A1A26, 0xFFB7D5, 0xF6E6EF),
            new Theme("Neon",      0xF0050B0E, 0xF00E181E, 0x39FFB5, 0xDFFFEF),
            new Theme("Midnight",  0xF0060714, 0xF0101222, 0x9D7BFF, 0xE4E1F7)
    };

    public static Theme current() {
        return THEMES[FeatureManager.opt("theme") % THEMES.length];
    }

    /** Акцент GUI: радуга, если включена, иначе цвет темы или пользовательский. */
    public static int accent() {
        if (FeatureManager.on("rainbow_accent")) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0f;
            return (0xFF << 24) | (Color.HSBtoRGB(hue, 0.7f, 0.95f) & 0xFFFFFF);
        }
        int custom = FeatureManager.color("accent_color");
        if (custom != 0x7FD9FF) return (0xFF << 24) | custom;
        return current().accent;
    }
}
