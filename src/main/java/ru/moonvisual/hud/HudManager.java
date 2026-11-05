package ru.moonvisual.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ru.moonvisual.core.Feature;
import ru.moonvisual.core.FeatureManager;
import ru.moonvisual.effects.EffectManager;
import ru.moonvisual.lang.Lang;
import ru.moonvisual.theme.ThemeManager;
import ru.moonvisual.util.Render2D;

import java.util.ArrayList;
import java.util.List;

public class HudManager {
    private static long lastHit = 0;
    private static float lastHpPct = 1.0f;
    private static double lastX = 0, lastY = 0, lastZ = 0, speed = 0;

    public static void onHit(Entity target) {
        lastHit = System.currentTimeMillis();
        if (target instanceof LivingEntity le) {
            lastHpPct = le.getHealth() / Math.max(1.0f, le.getMaxHealth());
        }
    }

    public static void tickCompute(MinecraftClient mc) {
        if (mc.player == null) return;
        double dx = mc.player.getX() - lastX;
        double dz = mc.player.getZ() - lastZ;
        double d = Math.sqrt(dx * dx + dz * dz);
        speed = speed * 0.8 + d * 20.0 * 0.2;
        lastX = mc.player.getX();
        lastY = mc.player.getY();
        lastZ = mc.player.getZ();
    }

    public static void render(DrawContext ctx, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(FeatureManager.num("hud_offset_x"), FeatureManager.num("hud_offset_y"), 0);
        float s = FeatureManager.num("hud_scale");
        m.scale(s, s, 1);
        drawInfoStack(ctx, mc);
        drawBars(ctx, mc);
        drawPotions(ctx, mc);
        drawKeystrokes(ctx, mc);
        drawTargetHud(ctx, mc);
        m.pop();
        drawArraylist(ctx, mc);
        drawCrosshair(ctx, mc);
        drawHitCrosshair(ctx, mc);
        drawDamageNumbers(ctx, mc);
        drawLowHpVignette(ctx, mc);
        if (FeatureManager.on("debug_mode")) drawDebug(ctx, mc);
    }

    private static int accent() {
        return (0xFF << 24) | FeatureManager.color("hud_accent");
    }

    private static boolean shadow() {
        return FeatureManager.on("hud_text_shadow");
    }

    private static int card(DrawContext ctx, MinecraftClient mc, String id, String text, int y) {
        if (!FeatureManager.on(id)) return y;
        var th = ThemeManager.current();
        int w = Render2D.w(text) + 10;
        if (FeatureManager.on("hud_shadow")) ctx.fill(3, y + 3, 3 + w, y + 16, 0x5A000000);
        ctx.fill(0, y, w, y + 16, th.panel);
        ctx.fill(0, y, 2, y + 16, accent());
        ctx.drawText(mc.textRenderer, text, 7, y + 4, th.text, shadow());
        return y + 18;
    }

    private static void drawInfoStack(DrawContext ctx, MinecraftClient mc) {
        int y = 4;
        if (FeatureManager.on("watermark")) {
            String wm = "Moon Visual";
            String ver = Lang.t("gui.title");
            String style = FeatureManager.option("watermark_style");
            int w = Render2D.w(wm) + 14;
            ctx.fill(0, y, w, y + 16, ThemeManager.current().panel);
            if (FeatureManager.on("watermark_glow")) ctx.fill(0, y, w, y + 2, accent());
            ctx.fill(0, y, 2, y + 16, accent());
            if (style.equals("Text")) {
                ctx.drawText(mc.textRenderer, wm, 7, y + 4, accent(), shadow());
            } else {
                ctx.drawText(mc.textRenderer, wm, 7, y + 4, ThemeManager.current().text, shadow());
            }
            if (!style.equals("Minimal")) {
                String v = " 0.1";
                ctx.drawText(mc.textRenderer, v, 7 + Render2D.w(wm), y + 4, accent(), false);
            }
            y += 20;
        }
        y = card(ctx, mc, "fps", "FPS: " + mc.getCurrentFps(), y);
        y = card(ctx, mc, "cps", "CPS: " + EffectManager.cpsValue(), y);
        y = card(ctx, mc, "ping", "Ping: " + ping(mc) + " ms", y);
        y = card(ctx, mc, "coords", String.format("XYZ: %d %d %d",
                (int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ()), y);
        y = card(ctx, mc, "direction", "Dir: " + cardinal(mc.player.getYaw()), y);
        y = card(ctx, mc, "biome", "Biome: " + biome(mc), y);
        if (FeatureManager.on("time")) {
            long tod = mc.world.getTimeOfDay();
            int h = (int) ((tod / 1000 + 6) % 24);
            int mi = (int) ((tod % 1000) * 60 / 1000);
            if (FeatureManager.option("clock_style").equals("Moon")) {
                y = card(ctx, mc, "time", String.format("Moon: %02d:%02d", h, mi), y);
            } else {
                y = card(ctx, mc, "time", String.format("Time: %02d:%02d", h, mi), y);
            }
        }
        y = card(ctx, mc, "server_ip", "IP: " + ip(mc), y);
        if (FeatureManager.on("memory")) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / 1048576;
            long total = rt.totalMemory() / 1048576;
            y = card(ctx, mc, "memory", "Mem: " + used + "/" + total + " MB", y);
        }
        if (FeatureManager.on("speed_hud")) {
            y = card(ctx, mc, "speed_hud", String.format("Speed: %.1f m/s", speed), y);
        }
        if (FeatureManager.on("held_item_info")) {
            ItemStack st = mc.player.getMainHandStack();
            if (!st.isEmpty()) {
                y = card(ctx, mc, "held_item_info", st.getName().getString()
                        + (st.getCount() > 1 ? " x" + st.getCount() : ""), y);
            }
        }
        if (FeatureManager.on("durability")) {
            ItemStack st = mc.player.getMainHandStack();
            if (!st.isEmpty() && st.getMaxDamage() > 0) {
                int pct = 100 - st.getDamage() * 100 / st.getMaxDamage();
                y = card(ctx, mc, "durability", "Dur: " + pct + "%", y);
            }
        }
        if (FeatureManager.on("tps")) {
            y = card(ctx, mc, "tps", "TPS: --", y);
        }
    }

    private static int ping(MinecraftClient mc) {
        try {
            var h = mc.getNetworkHandler();
            if (h == null) return 0;
            var e = h.getPlayerListEntry(mc.player.getUuid());
            return e == null ? 0 : e.getLatency();
        } catch (Exception ex) {
            return 0;
        }
    }

    private static String ip(MinecraftClient mc) {
        var s = mc.getCurrentServerEntry();
        return s == null ? "singleplayer" : s.address;
    }

    private static String cardinal(float yaw) {
        String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int i = (int) Math.round((((yaw % 360) + 360) % 360) / 45.0) % 8;
        return dirs[i];
    }

    private static String biome(MinecraftClient mc) {
        try {
            var key = mc.world.getBiome(mc.player.getBlockPos()).getKey();
            return key.map(k -> k.getValue().getPath()).orElse("unknown");
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private static void drawBars(DrawContext ctx, MinecraftClient mc) {
        int y = 4;
        // info stack height estimation: count enabled cards
        String[] ids = {"watermark","fps","cps","ping","coords","direction","biome","time",
                "server_ip","memory","speed_hud","held_item_info","durability","tps"};
        for (String id : ids) if (FeatureManager.on(id)) y += 18;
        if (FeatureManager.on("watermark")) y += 2;
        y += 4;
        var th = ThemeManager.current();
        if (FeatureManager.on("health_hud")) {
            float hp = mc.player.getHealth(), max = Math.max(1, mc.player.getMaxHealth());
            int pct = (int) (hp / max * 100);
            String txt = "HP: " + (int) hp;
            int w = 110;
            ctx.fill(0, y, w, y + 16, th.panel);
            ctx.fill(2, y + 6, (int) (2 + (w - 4) * hp / max), y + 12, accent());
            ctx.drawText(mc.textRenderer, txt, 6, y + 4, th.text, shadow());
            y += 18;
        }
        if (FeatureManager.on("hunger_hud")) {
            int food = mc.player.getHungerManager().getFoodLevel();
            String txt = "Food: " + food;
            int w = 110;
            ctx.fill(0, y, w, y + 16, th.panel);
            ctx.fill(2, y + 6, (int) (2 + (w - 4) * food / 20.0), y + 12, 0xFFCC7744);
            ctx.drawText(mc.textRenderer, txt, 6, y + 4, th.text, shadow());
            y += 18;
        }
        if (FeatureManager.on("armor_hud")) {
            int x = 0;
            for (ItemStack st : mc.player.getInventory().armor) {
                if (!st.isEmpty()) {
                    ctx.drawItem(st, x, y);
                    x += 18;
                }
            }
            if (x > 0) y += 18;
        }
    }

    private static void drawPotions(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("potions_hud")) return;
        var th = ThemeManager.current();
        int y = mc.getWindow().getScaledHeight() / 3;
        for (var eff : mc.player.getStatusEffects()) {
            String name = Text.translatable(eff.getEffectType().value().getTranslationKey()).getString();
            int sec = Math.max(0, eff.getDuration() / 20);
            String txt = name + " " + (sec / 60) + ":" + String.format("%02d", sec % 60);
            int w = Render2D.w(txt) + 10;
            int x = mc.getWindow().getScaledWidth() - w - 4;
            ctx.fill(x, y, x + w, y + 15, th.panel);
            ctx.drawText(mc.textRenderer, txt, x + 5, y + 4, th.text, shadow());
            y += 17;
        }
    }

    private static void drawKeystrokes(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("keystrokes")) return;
        int handle = mc.getWindow().getHandle();
        var th = ThemeManager.current();
        boolean[] pressed = {
                org.lwjgl.glfw.GLFW.GLFW_KEY_W == key(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_W),
                org.lwjgl.glfw.GLFW.GLFW_KEY_A == key(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_A),
                org.lwjgl.glfw.GLFW.GLFW_KEY_S == key(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_S),
                org.lwjgl.glfw.GLFW.GLFW_KEY_D == key(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_D),
                mc.mouse.wasLeftButtonClicked(),
                mc.mouse.wasRightButtonClicked()
        };
        String[] labels = {"W", "A", "S", "D", "LMB", "RMB"};
        String style = FeatureManager.option("keystrokes_style");
        int size = style.equals("Minimal") ? 18 : 22;
        int bx = 4, by = mc.getWindow().getScaledHeight() - 100;
        for (int i = 0; i < labels.length; i++) {
            int x, yy;
            if (i == 0) { x = bx + size; yy = by; }
            else if (i < 4) { x = bx + (i - 1) * (size + 2); yy = by + size + 2; }
            else { x = bx + (i - 4) * (size * 2 + 2) + size / 2; yy = by + (size + 2) * 2; }
            int w = (i >= 4) ? size * 2 + 2 : size;
            int col = pressed[i] ? accent() : th.panel;
            ctx.fill(x, yy, x + w, yy + size, col);
            int tc = pressed[i] ? 0xFF101018 : th.text;
            Render2D.textC(ctx, labels[i], x + w / 2, yy + size / 2 - 4, tc, shadow());
        }
    }

    private static boolean key(int handle, int code) {
        return com.mojang.blaze3d.platform.InputUtil.isKeyPressed(handle, code);
    }

    private static void drawArraylist(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("arraylist")) return;
        List<Feature> list = new ArrayList<>();
        for (Feature f : FeatureManager.MAP.values()) {
            if (f.type == Feature.Type.TOGGLE && f.enabled) list.add(f);
        }
        list.sort((a, b) -> Render2D.w(b.displayName()) - Render2D.w(a.displayName()));
        var th = ThemeManager.current();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int pos = FeatureManager.opt("arraylist_pos");
        int y = pos < 2 ? 4 : sh - 4;
        for (Feature f : list) {
            int h = 14;
            int yy = pos < 2 ? y : y - h;
            int w = Render2D.w(f.displayName()) + 8;
            int x = (pos == 0 || pos == 2) ? 4 : sw - w - 4;
            ctx.fill(x, yy, x + w, yy + h, th.panel);
            ctx.fill((pos == 0 || pos == 2) ? x : x + w - 2, yy,
                    (pos == 0 || pos == 2) ? x + 2 : x + w, yy + h, accent());
            ctx.drawText(mc.textRenderer, f.displayName(), x + 6, yy + 3, th.text, shadow());
            y = pos < 2 ? y + h : y - h;
        }
    }

    private static void drawTargetHud(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("targethud")) return;
        if (!(mc.targetedEntity instanceof LivingEntity le)) return;
        var th = ThemeManager.current();
        String name = le.getName().getString();
        float hp = le.getHealth(), max = Math.max(1, le.getMaxHealth());
        int pct = (int) (hp / max * 100);
        String style = FeatureManager.option("targethud_style");
        int w = 140, x = mc.getWindow().getScaledWidth() / 2 - w / 2, y = 6;
        ctx.fill(x, y, x + w, y + 24, th.panel);
        if (style.equals("Panel") || style.equals("Moon")) {
            ctx.fill(x, y, x + w, y + 2, accent());
        }
        ctx.drawText(mc.textRenderer, name, x + 6, y + 5, th.text, shadow());
        int barY = y + 17;
        ctx.fill(x + 6, barY, x + w - 6, barY + 4, 0x66202028);
        int fill = (int) ((w - 12) * hp / max);
        ctx.fill(x + 6, barY, x + 6 + fill, barY + 4, accent());
        if (FeatureManager.on("target_info")) {
            String info = pct + "% | " + (int) mc.player.distanceTo(le) + "m";
            ctx.drawText(mc.textRenderer, info, x + w - Render2D.w(info) - 6, y + 5, accent(), shadow());
        }
    }

    private static void drawCrosshair(DrawContext ctx, MinecraftClient mc) {
        String style = FeatureManager.option("crosshair_style");
        if (style.equals("Vanilla")) return;
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        int cx = w / 2, cy = h / 2;
        int col = (0xFF << 24) | FeatureManager.color("crosshair_color");
        int out = FeatureManager.on("crosshair_outline") ? 0xFF101014 : col;
        switch (style) {
            case "Dot" -> {
                if (FeatureManager.on("crosshair_outline")) ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, out);
                ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, col);
            }
            case "Cross" -> {
                if (FeatureManager.on("crosshair_outline")) {
                    ctx.fill(cx - 7, cy - 1, cx + 7, cy + 1, out);
                    ctx.fill(cx - 1, cy - 7, cx + 1, cy + 7, out);
                }
                ctx.fill(cx - 6, cy, cx + 6, cy + 1, col);
                ctx.fill(cx, cy - 6, cx + 1, cy + 6, col);
            }
            case "Circle" -> {
                for (int i = 0; i < 12; i++) {
                    double a = i * Math.PI / 6;
                    ctx.fill(cx + (int) (Math.cos(a) * 4), cy + (int) (Math.sin(a) * 4),
                            cx + (int) (Math.cos(a) * 4) + 1, cy + (int) (Math.sin(a) * 4) + 1, col);
                }
            }
            case "Moon" -> {
                ctx.fill(cx - 3, cy - 3, cx + 1, cy + 1, col);
                ctx.fill(cx + 1, cy - 1, cx + 3, cy + 1, col);
                ctx.fill(cx - 1, cy + 1, cx + 1, cy + 3, col);
            }
        }
    }

    private static void drawHitCrosshair(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("hit_crosshair")) return;
        long dt = System.currentTimeMillis() - lastHit;
        if (dt > 300) return;
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        int cx = w / 2, cy = h / 2;
        int col = (0xFF << 24) | FeatureManager.color("hit_color");
        int grow = (int) (dt / 25);
        int a = 0xFF - (int) (0xFF * dt / 300);
        col = (a << 24) | (col & 0xFFFFFF);
        ctx.fill(cx - 8 - grow, cy, cx - 4 - grow, cy + 1, col);
        ctx.fill(cx + 4 + grow, cy, cx + 8 + grow, cy + 1, col);
        ctx.fill(cx, cy - 8 - grow, cx + 1, cy - 4 - grow, col);
        ctx.fill(cx, cy + 4 + grow, cx + 1, cy + 8 + grow, col);
    }

    private static void drawDamageNumbers(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("damage_numbers")) return;
        long dt = System.currentTimeMillis() - lastHit;
        if (dt > 600) return;
        String style = FeatureManager.option("damage_number_style");
        int rise = 0, alpha = 0xFF;
        if (style.equals("Rise")) rise = (int) (dt / 20);
        if (style.equals("Fade")) alpha = Math.max(0, 0xFF - (int) (0xFF * dt / 600));
        int col = ((alpha << 24) | (FeatureManager.color("hit_color") & 0xFFFFFF));
        String txt = "HIT " + (int) (lastHpPct * 100) + "%";
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        Render2D.textC(ctx, txt, w / 2, h / 2 - 24 - rise, col, shadow());
    }

    private static void drawLowHpVignette(DrawContext ctx, MinecraftClient mc) {
        if (!FeatureManager.on("low_hp_vignette")) return;
        float hp = mc.player.getHealth();
        float thr = FeatureManager.num("low_hp_threshold");
        if (hp > thr) return;
        float t = 1.0f - hp / Math.max(1.0f, thr);
        int pulse = (int) (Math.abs(Math.sin(System.currentTimeMillis() / 300.0)) * 40);
        int a = Math.min(0x88, (int) (0x40 * t) + pulse);
        int rgb = FeatureManager.color("damage_overlay_color");
        int c1 = (a << 24) | rgb;
        int c2 = (0x00000000) | rgb;
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        ctx.fillGradient(0, 0, w, h / 4, 0x00000000, c1);
        ctx.fillGradient(0, h - h / 4, w, h, c1, 0x00000000);
    }

    private static void drawDebug(DrawContext ctx, MinecraftClient mc) {
        String txt = "MoonVisual | features: " + FeatureManager.MAP.size()
                + " | fps: " + mc.getCurrentFps();
        Render2D.text(ctx, txt, 4, mc.getWindow().getScaledHeight() - 12, 0xFF66DDAA, true);
    }
}
