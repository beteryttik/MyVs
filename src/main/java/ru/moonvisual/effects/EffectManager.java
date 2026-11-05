package ru.moonvisual.effects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import ru.moonvisual.MoonVisualClient;
import ru.moonvisual.core.Feature;
import ru.moonvisual.core.FeatureManager;
import ru.moonvisual.hud.HudManager;

import java.util.Random;

public class EffectManager {
    private static final Random RND = new Random();
    private static boolean wasOnGround = true;
    private static int cps = 0;
    private static long cpsWindow = 0;
    private static float zoomCur = 1.0f;
    public static boolean zoomHeld = false;

    public static int cpsValue() {
        return cps;
    }

    /** Вызывается из миксина атаки. */
    public static void onAttack(PlayerEntity player, Entity target) {
        cps++;
        HudManager.onHit(target);
        Feature styleF = FeatureManager.f("hit_style");
        if (FeatureManager.on("hit_particles") && styleF != null) {
            spawnStyle(styleF.optionName(FeatureManager.opt("hit_style")),
                    target.getX(), target.getEyeY(), target.getZ());
        }
        if (FeatureManager.on("crit_particles") && player.getAttackCooldownProgress(0.0f) > 0.9f
                && player.fallDistance > 0.0f) {
            Feature cs = FeatureManager.f("crit_style");
            if (cs != null) spawnStyle(cs.optionName(FeatureManager.opt("crit_style")),
                    target.getX(), target.getEyeY(), target.getZ());
        }
        if (FeatureManager.on("hit_glow")) {
            int c = FeatureManager.color("glow_color");
            Vector3f col = dustColor(c);
            for (int i = 0; i < 10; i++) {
                player.getWorld().addParticle(new DustParticleEffect(col, 1.0f),
                        target.getX() + r(0.5), target.getEyeY() + r(0.5), target.getZ() + r(0.5), 0, 0.05, 0);
            }
        }
        if (target instanceof LivingEntity le && le.isDead() && FeatureManager.on("kill_effect")) {
            String ks = FeatureManager.option("kill_style");
            for (int i = 0; i < 40; i++) {
                if (ks.equals("Burst")) {
                    target.getWorld().addParticle(ParticleTypes.POOF,
                            target.getX() + r(0.8), target.getY() + r(1.2), target.getZ() + r(0.8), r(0.2), 0.1, r(0.2));
                } else if (ks.equals("Implosion")) {
                    target.getWorld().addParticle(ParticleTypes.CLOUD,
                            target.getX() + r(1.0), target.getY() + r(1.5), target.getZ() + r(1.0), 0, 0.02, 0);
                } else {
                    target.getWorld().addParticle(ParticleTypes.SOUL,
                            target.getX() + r(0.6), target.getY() + r(1.2), target.getZ() + r(0.6), r(0.1), 0.1, r(0.1));
                }
            }
        }
        if (FeatureManager.on("hitmarker_sound")) {
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(),
                    1.6f, FeatureManager.num("hitmarker_volume")));
        }
    }

    private static void spawnStyle(String style, double x, double y, double z) {
        var world = MinecraftClient.getInstance().world;
        if (world == null) return;
        int count = (int) (8 * FeatureManager.num("particle_density"));
        for (int i = 0; i < count; i++) {
            double ox = r(0.5), oy = r(0.8), oz = r(0.5);
            switch (style) {
                case "Spark" -> world.addParticle(ParticleTypes.ENCHANTED_HIT, x + ox, y + oy, z + oz, r(0.1), 0.05, r(0.1));
                case "Moonlight" -> world.addParticle(ParticleTypes.END_ROD, x + ox, y + oy, z + oz, r(0.1), 0.1, r(0.1));
                case "Blood" -> world.addParticle(new DustParticleEffect(new Vector3f(0.8f, 0.1f, 0.2f), 1.0f),
                        x + ox, y + oy, z + oz, 0, 0.05, 0);
                case "Bloom" -> world.addParticle(ParticleTypes.CHERRY_LEAVES, x + ox, y + oy, z + oz, r(0.05), 0.02, r(0.05));
                case "Star" -> world.addParticle(ParticleTypes.END_ROD, x + ox, y + oy, z + oz, r(0.2), 0.15, r(0.2));
                case "Void" -> world.addParticle(ParticleTypes.PORTAL, x + ox, y + oy, z + oz, r(0.2), 0.05, r(0.2));
                case "Frost" -> world.addParticle(ParticleTypes.SNOWFLAKE, x + ox, y + oy, z + oz, 0, 0.03, 0);
                case "Ember" -> world.addParticle(ParticleTypes.FLAME, x + ox, y + oy, z + oz, 0, 0.05, 0);
                case "Astral" -> world.addParticle(ParticleTypes.GLOW, x + ox, y + oy, z + oz, r(0.1), 0.08, r(0.1));
                case "Sakura" -> world.addParticle(ParticleTypes.CHERRY_LEAVES, x + ox, y + oy, z + oz, r(0.1), 0.03, r(0.1));
                default -> world.addParticle(ParticleTypes.CRIT, x + ox, y + oy, z + oz, r(0.1), 0.05, r(0.1));
            }
        }
    }

    private static Vector3f dustColor(int rgb) {
        return new Vector3f(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
    }

    private static double r(double range) {
        return (RND.nextDouble() - 0.5) * 2 * range;
    }

    /** Тик обработки движения/аур. */
    public static void tick(MinecraftClient mc) {
        zoomHeld = FeatureManager.on("zoom") && MoonVisualClient.zoomKey.isPressed();
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - cpsWindow >= 1000) {
            cpsWindow = now;
            cps = 0;
        }
        boolean onGround = mc.player.isOnGround();
        if (FeatureManager.on("jump_circle") && wasOnGround && !onGround && mc.player.getVelocity().y > 0) {
            spawnCircle(mc, FeatureManager.option("jump_style"));
        }
        if (FeatureManager.on("landing_particles") && !wasOnGround && onGround && mc.player.fallDistance > 1.0f) {
            int c = FeatureManager.color("footstep_color");
            Vector3f col = dustColor(c);
            for (int i = 0; i < 12; i++) {
                mc.world.addParticle(new DustParticleEffect(col, 0.8f),
                        mc.player.getX() + r(0.6), mc.player.getY(), mc.player.getZ() + r(0.6), r(0.2), 0.02, r(0.2));
            }
        }
        if (FeatureManager.on("sprint_trail") && mc.player.isSprinting() && mc.player.age % 4 == 0) {
            spawnTrail(mc, FeatureManager.option("trail_style"), 3);
        }
        if (FeatureManager.on("step_particles") && onGround
                && (mc.player.getX() != 0 && mc.player.age % 10 == 0)) {
            int c = FeatureManager.color("footstep_color");
            mc.world.addParticle(new DustParticleEffect(dustColor(c), 0.5f),
                    mc.player.getX() + r(0.3), mc.player.getY() + 0.05, mc.player.getZ() + r(0.3), 0, 0.01, 0);
        }
        if (FeatureManager.on("moon_aura") && mc.player.age % 5 == 0) {
            String st = FeatureManager.option("aura_style");
            for (int i = 0; i < 2; i++) {
                double ax = mc.player.getX() + r(0.6), az = mc.player.getZ() + r(0.6);
                if (st.equals("Stars")) {
                    mc.world.addParticle(ParticleTypes.END_ROD, ax, mc.player.getY() + 1.0, az, 0, 0.02, 0);
                } else if (st.equals("Mist")) {
                    mc.world.addParticle(ParticleTypes.CLOUD, ax, mc.player.getY() + 0.3, az, 0, 0.01, 0);
                } else {
                    mc.world.addParticle(ParticleTypes.GLOW, ax, mc.player.getY() + r(1.5) + 0.5, az, 0, 0.02, 0);
                }
            }
        }
        if (FeatureManager.on("potion_swirl") && mc.player.age % 8 == 0
                && !mc.player.getStatusEffects().isEmpty()) {
            for (int i = 0; i < 4; i++) {
                double ang = (mc.player.age % 8) * Math.PI / 4 + i;
                mc.world.addParticle(ParticleTypes.WITCH,
                        mc.player.getX() + Math.cos(ang) * 0.5, mc.player.getY() + 0.5, mc.player.getZ() + Math.sin(ang) * 0.5,
                        0, 0.05, 0);
            }
        }
        wasOnGround = onGround;
    }

    private static void spawnCircle(MinecraftClient mc, String style) {
        for (int i = 0; i < 20; i++) {
            double ang = i * Math.PI / 10;
            double x = mc.player.getX() + Math.cos(ang) * 0.7;
            double z = mc.player.getZ() + Math.sin(ang) * 0.7;
            if (style.equals("Moon")) {
                mc.world.addParticle(ParticleTypes.END_ROD, x, mc.player.getY(), z, 0, 0.02, 0);
            } else if (style.equals("Glow")) {
                mc.world.addParticle(ParticleTypes.GLOW, x, mc.player.getY() + 0.05, z, 0, 0, 0);
            } else {
                mc.world.addParticle(ParticleTypes.HAPPY_VILLAGER, x, mc.player.getY(), z, 0, 0, 0);
            }
        }
    }

    private static void spawnTrail(MinecraftClient mc, String style, int count) {
        for (int i = 0; i < count; i++) {
            switch (style) {
                case "Sparkle" -> mc.world.addParticle(ParticleTypes.END_ROD,
                        mc.player.getX() + r(0.2), mc.player.getY() + 0.2, mc.player.getZ() + r(0.2), 0, 0.02, 0);
                case "Flame" -> mc.world.addParticle(ParticleTypes.FLAME,
                        mc.player.getX() + r(0.2), mc.player.getY() + 0.2, mc.player.getZ() + r(0.2), 0, 0.02, 0);
                case "Mist" -> mc.world.addParticle(ParticleTypes.CLOUD,
                        mc.player.getX() + r(0.2), mc.player.getY() + 0.1, mc.player.getZ() + r(0.2), 0, 0.01, 0);
                default -> mc.world.addParticle(ParticleTypes.GLOW,
                        mc.player.getX() + r(0.2), mc.player.getY() + 0.3, mc.player.getZ() + r(0.2), 0, 0.02, 0);
            }
        }
    }

    /** Модификатор FOV (зум). Вызывается из миксина. */
    public static float fovModifier(float base) {
        float factor = FeatureManager.num("zoom_factor");
        float target = zoomHeld ? base / factor : base;
        float speed = 0.2f + 0.6f * FeatureManager.num("zoom_smooth");
        zoomCur += (target - zoomCur) * speed;
        return zoomCur;
    }
}
