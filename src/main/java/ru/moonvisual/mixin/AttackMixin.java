package ru.moonvisual.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.moonvisual.effects.EffectManager;

@Mixin(ClientPlayerInteractionManager.class)
public class AttackMixin {
    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void moonvisual$onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        EffectManager.onAttack(player, target);
    }
}
