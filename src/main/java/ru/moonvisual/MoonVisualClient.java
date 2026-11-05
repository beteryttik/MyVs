package ru.moonvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.moonvisual.config.ConfigManager;
import ru.moonvisual.core.FeatureManager;
import ru.moonvisual.effects.EffectManager;
import ru.moonvisual.gui.ClickGuiScreen;
import ru.moonvisual.hud.HudManager;
import ru.moonvisual.lang.Lang;
import ru.moonvisual.perf.PerformanceManager;

public class MoonVisualClient implements ClientModInitializer {
    public static KeyBinding openGui;
    public static KeyBinding zoomKey;

    @Override
    public void onInitializeClient() {
        FeatureManager.registerAll();
        Lang.loadAll();
        ConfigManager.load();
        Lang.reload();

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.moonvisual.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.category"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.moonvisual.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.category"));

        HudRenderCallback.EVENT.register(HudManager::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) client.setScreen(new ClickGuiScreen());
            EffectManager.tick(client);
            PerformanceManager.tick(client);
            HudManager.tickCompute(client);
            ConfigManager.tick();
        });
    }
}
