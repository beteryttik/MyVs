package ru.moonvisual.perf;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import ru.moonvisual.core.FeatureManager;

public class PerformanceManager {
    private static boolean appliedOnce = false;

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (FeatureManager.on("memory_cleanup") && mc.player.age % 1200 == 0) {
            System.gc();
        }
        int bg = Math.max(5, (int) FeatureManager.num("background_fps"));
        if (!mc.isWindowFocused() && bg < 60) {
            try {
                Thread.sleep(Math.max(2, 1000 / bg));
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        applyOptions(mc);
        if (FeatureManager.on("adaptive_quality") && mc.getCurrentFps() < 30) {
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            mc.options.getParticles().setValue(ParticlesMode.MINIMAL);
        }
    }

    private static void applyOptions(MinecraftClient mc) {
        try {
            mc.options.getMaxFps().setValue((int) FeatureManager.num("max_fps"));
            mc.options.getFov().setValue((int) FeatureManager.num("fov"));
            mc.options.getViewDistance().setValue((int) FeatureManager.num("render_distance"));
            mc.options.getBiomeBlendRadius().setValue((int) FeatureManager.num("biome_blend"));
            mc.options.getEntityDistanceScaling().setValue((double) FeatureManager.num("entity_distance"));
            mc.options.getMipmapLevels().setValue((int) FeatureManager.num("mipmaps"));
            if (FeatureManager.on("clouds_off")) mc.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            if (FeatureManager.on("entity_shadows_off")) mc.options.getEntityShadows().setValue(false);
            if (FeatureManager.on("darkness_flicker_off")) mc.options.getDarknessEffectScale().setValue(0.0d);
            if (FeatureManager.on("dynamic_fov_off")) mc.options.getFovEffectScale().setValue(0.0d);
            String preset = FeatureManager.option("graphics_preset");
            for (GraphicsMode g : GraphicsMode.values()) {
                if (g.name().equalsIgnoreCase(preset)) mc.options.getGraphicsMode().setValue(g);
            }
            String pc = FeatureManager.option("particle_cap");
            for (ParticlesMode p : ParticlesMode.values()) {
                if (p.name().equalsIgnoreCase(pc)) mc.options.getParticles().setValue(p);
            }
            appliedOnce = true;
        } catch (Exception ignored) {
        }
    }
}
