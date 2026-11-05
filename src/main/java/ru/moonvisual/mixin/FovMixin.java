package ru.moonvisual.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.moonvisual.effects.EffectManager;

@Mixin(GameRenderer.class)
public class FovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void moonvisual$fov(Camera camera, float tickDelta, boolean changingFov,
                                CallbackInfoReturnable<Float> cir) {
        float f = EffectManager.fovModifier(cir.getReturnValueF());
        cir.setReturnValue(f);
    }
}
