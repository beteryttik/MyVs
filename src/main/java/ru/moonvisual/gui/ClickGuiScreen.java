package ru.moonvisual.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import ru.moonvisual.config.ConfigManager;
import ru.moonvisual.core.Feature;
import ru.moonvisual.core.FeatureManager;
import ru.moonvisual.lang.Lang;
import ru.moonvisual.theme.ThemeManager;
import ru.moonvisual.util.AnimValue;
import ru.moonvisual.util.Render2D;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    public static final String[] CATS = {"HUD", "Particles & Effects", "Animations", "Camera",
            "Performance", "Interface", "Sky & World", "Cosmetic", "General"};
    private static final int PANEL_W = 470, PANEL_H = 300, CAT_W = 150, ROW_H = 26, PAD = 8;
    private static final int[] PALETTE = {0xFFFFFF, 0x7FD9FF, 0xFF4455, 0xFFB7D5, 0x39FFB5,
            0xFFD84A, 0x9D7BFF, 0x202028, 0xFF7A00, 0x4455FF};

    private String cat = "HUD";
    private final AnimValue anim = new AnimValue();
    private float scroll = 0;
    private Feature dragSlider = null;
    private TextFieldWidget search;

    public ClickGuiScreen() {
        super(Text.literal("Moon Visual"));
    }

    private int panelX() {
        return (width - PANEL_W) / 2;
    }

    private int panelY() {
        return (height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        search = new TextFieldWidget(textRenderer, panelX() + CAT_W + PAD, panelY() + PAD,
                PANEL_W - CAT_W - PAD * 2, 16, Text.literal("Search"));
        search.setMaxLength(48);
    }

    private List<Feature> visibleFeatures() {
        List<Feature> list = new ArrayList<>();
        String q = (FeatureManager.on("search_in_gui") && search != null) ? search.getText().toLowerCase() : "";
        for (Feature f : FeatureManager.MAP.values()) {
            if (!f.category.equals(cat)) continue;
            if (!q.isEmpty()
                    && !f.nameEn.toLowerCase().contains(q)
                    && !f.nameRu.toLowerCase().contains(q)) continue;
            list.add(f);
        }
        return list;
    }

    private int rowY(int i) {
        return panelY() + PAD + 20 + i * ROW_H;
    }

    private int rowsVisible() {
        return (PANEL_H - PAD * 2 - 20) / ROW_H;
    }

    private void clickSound() {
        if (FeatureManager.on("gui_sounds")) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, FeatureManager.num("click_sound_volume")));
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float dt) {
        anim.approach(1.0f, 0.04f * FeatureManager.num("menu_speed"));
        ctx.fill(0, 0, width, height, 0x88000000);
        ctx.getMatrices().push();
        if (FeatureManager.on("menu_animation")) {
            float s = 0.94f + 0.06f * anim.value;
            ctx.getMatrices().translate(width / 2.0f, height / 2.0f, 0);
            ctx.getMatrices().scale(s, s, 1);
            ctx.getMatrices().translate(-width / 2.0f, -height / 2.0f, 0);
        }
        var th = ThemeManager.current();
        int accent = ThemeManager.accent();
        int a = (int) (FeatureManager.num("gui_opacity") * 255);
        int panel = (a << 24) | (th.panel & 0xFFFFFF);
        int bg = (a << 24) | (th.bg & 0xFFFFFF);

        // панель
        ctx.fill(panelX(), panelY(), panelX() + PANEL_W, panelY() + PANEL_H, panel);
        ctx.fill(panelX(), panelY(), panelX() + PANEL_W, panelY() + 2, accent);
        Render2D.text(ctx, Lang.t("gui.title"), panelX() + PAD, panelY() + PAD + 3, accent, true);

        // категории
        for (int i = 0; i < CATS.length; i++) {
            int y = panelY() + PAD + 20 + i * 24;
            boolean hov = mx >= panelX() + PAD && mx < panelX() + CAT_W - PAD && my >= y && my < y + 22;
            boolean sel = CATS[i].equals(cat);
            int col = sel ? (accent & 0xFFFFFF) | 0x55000000
                    : (hov && FeatureManager.on("hover_animation") ? 0x33FFFFFF : 0);
            if (col != 0) ctx.fill(panelX() + PAD, y, panelX() + CAT_W - PAD, y + 22, col);
            Render2D.text(ctx, Lang.t("cat." + CATS[i]), panelX() + PAD + 6, y + 7,
                    sel ? accent : th.text, shadow());
        }

        // поиск
        if (FeatureManager.on("search_in_gui") && search != null) {
            search.render(ctx, mx, my, dt);
        }

        // строки функций
        List<Feature> vis = visibleFeatures();
        int maxOff = Math.max(0, vis.size() - rowsVisible());
        scroll = Math.max(0, Math.min(scroll, maxOff));
        int start = (int) scroll;
        for (int i = 0; i + start < vis.size() && i < rowsVisible(); i++) {
            Feature f = vis.get(i + start);
            int y = rowY(i);
            int x = panelX() + CAT_W + PAD;
            int rw = PANEL_W - CAT_W - PAD * 2;
            boolean hov = mx >= x && mx < x + rw && my >= y && my < y + ROW_H - 2;
            if (hov && FeatureManager.on("hover_animation")) {
                ctx.fill(x, y, x + rw, y + ROW_H - 2, 0x22FFFFFF);
            }
            Render2D.text(ctx, f.displayName(), x + 6, y + 8, th.text, shadow());
            if (!f.implemented) {
                String soon = Lang.t("gui.soon");
                Render2D.text(ctx, soon, x + rw - Render2D.w(soon) - 6, y + 8, 0xFF666666, false);
            }
            drawControl(ctx, f, x, y, rw, accent, mx, my);
        }
        ctx.getMatrices().pop();
        if (FeatureManager.on("tooltips")) drawTooltip(ctx, vis, start, mx, my, accent);
    }

    private boolean shadow() {
        return FeatureManager.on("hud_text_shadow");
    }

    private void drawControl(DrawContext ctx, Feature f, int x, int y, int rw, int accent, int mx, int my) {
        switch (f.type) {
            case TOGGLE -> {
                int bx = x + rw - 34, by = y + 7;
                ctx.fill(bx, by, bx + 26, by + 11, 0x33000000);
                int fill = f.enabled ? accent : 0x66FFFFFF;
                int knob = f.enabled ? bx + 15 : bx + 2;
                ctx.fill(knob, by, knob + 11, by + 11, fill);
            }
            case SLIDER -> {
                int sx = x + rw - 110, sy = y + 10, sw = 100;
                ctx.fill(sx, sy, sx + sw, sy + 5, 0x55000000);
                int w = (int) (sw * (f.max <= 0 ? 0 : f.value / f.max));
                int hovBoost = (dragSlider == f) ? accent : accent;
                ctx.fill(sx, sy, sx + Math.max(0, Math.min(sw, w)), sy + 5, hovBoost);
                String val = String.format("%.1f", f.value);
                Render2D.text(ctx, val, sx - Render2D.w(val) - 6, y + 8, accent, false);
            }
            case COLOR -> {
                int bx = x + rw - 40, by = y + 6;
                ctx.fill(bx, by, bx + 32, by + 12, (0xFF << 24) | FeatureManager.color(f.id));
                ctx.drawBorder(bx, by, 32, 12, 0x88FFFFFF);
            }
            case SELECT -> {
                String s = "< " + f.optionName(FeatureManager.opt(f.id)) + " >";
                Render2D.text(ctx, s, x + rw - Render2D.w(s) - 6, y + 8, accent, false);
            }
        }
    }

    private void drawTooltip(DrawContext ctx, List<Feature> vis, int start, int mx, int my, int accent) {
        for (int i = 0; i + start < vis.size() && i < rowsVisible(); i++) {
            Feature f = vis.get(i + start);
            int y = rowY(i);
            if (mx >= panelX() + CAT_W + PAD && mx < panelX() + PANEL_W - PAD && my >= y && my < y + ROW_H - 2) {
                String tip = f.implemented ? f.displayName() + " - " + f.category
                        : f.displayName() + " (" + Lang.t("gui.soon") + ")";
                int w = Render2D.w(tip) + 8;
                int tx = Math.min(mx + 8, width - w);
                ctx.fill(tx, my + 12, tx + w, my + 26, 0xF0101014);
                Render2D.text(ctx, tip, tx + 4, my + 15, 0xFFE6E6EE, false);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int x = (int) mx, y = (int) my;
        // поиск
        if (FeatureManager.on("search_in_gui") && search != null) {
            search.mouseClicked(mx, my, button);
        }
        // категории
        for (int i = 0; i < CATS.length; i++) {
            int cy = panelY() + PAD + 20 + i * 24;
            if (x >= panelX() + PAD && x < panelX() + CAT_W - PAD && y >= cy && y < cy + 22) {
                cat = CATS[i];
                scroll = 0;
                clickSound();
                return true;
            }
        }
        // строки
        List<Feature> vis = visibleFeatures();
        int start = (int) scroll;
        for (int i = 0; i + start < vis.size() && i < rowsVisible(); i++) {
            Feature f = vis.get(i + start);
            int ry = rowY(i);
            if (y < ry || y >= ry + ROW_H - 2) continue;
            int rx = panelX() + CAT_W + PAD;
            int rw = PANEL_W - CAT_W - PAD * 2;
            clickSound();
            switch (f.type) {
                case TOGGLE -> f.enabled = !f.enabled;
                case SELECT -> f.value = (f.value + 1) % f.options.length;
                case COLOR -> {
                    int cur = FeatureManager.color(f.id);
                    int idx = -1;
                    for (int k = 0; k < PALETTE.length; k++) if (PALETTE[k] == cur) idx = k;
                    f.value = PALETTE[(idx + 1) % PALETTE.length];
                }
                case SLIDER -> {
                    int sx = rx + rw - 110;
                    if (x >= sx - 30) {
                        dragSlider = f;
                        applySlider(f, x, rx, rw);
                    }
                }
            }
            if (f.id.equals("language")) ru.moonvisual.lang.Lang.reload();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private void applySlider(Feature f, int mx, int rx, int rw) {
        int sx = rx + rw - 110, sw = 100;
        float p = (mx - sx) / (float) sw;
        p = Math.max(0f, Math.min(1f, p));
        f.value = p * f.max;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSlider != null) {
            applySlider(dragSlider, (int) mx, panelX() + CAT_W + PAD, PANEL_W - CAT_W - PAD * 2);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll -= (float) v * 1.5f;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            ConfigManager.save();
            client.setScreen(null);
            return true;
        }
        if (search != null && search.isFocused()) {
            return search.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (search != null && search.isFocused()) return search.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ConfigManager.save();
        super.removed();
    }
}
