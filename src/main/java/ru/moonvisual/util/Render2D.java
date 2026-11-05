package ru.moonvisual.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Render2D {
    public static void fill(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        c.fill(x1, y1, x2, y2, col);
    }

    public static void border(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        c.drawBorder(x1, y1, x2 - x1, y2 - y1, col);
    }

    public static TextRenderer tr() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public static void text(DrawContext c, String s, int x, int y, int col, boolean shadow) {
        c.drawText(tr(), s, x, y, col, shadow);
    }

    public static void textC(DrawContext c, String s, int cx, int y, int col, boolean shadow) {
        c.drawText(tr(), s, cx - tr().getWidth(s) / 2, y, col, shadow);
    }

    public static int w(String s) {
        return tr().getWidth(s);
    }
}
