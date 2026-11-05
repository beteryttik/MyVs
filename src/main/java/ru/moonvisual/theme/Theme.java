package ru.moonvisual.theme;

public class Theme {
    public final String id;
    public final int bg, panel, accent, text;

    public Theme(String id, int bg, int panel, int accent, int text) {
        this.id = id;
        this.bg = bg;
        this.panel = panel;
        this.accent = accent;
        this.text = text;
    }
}
