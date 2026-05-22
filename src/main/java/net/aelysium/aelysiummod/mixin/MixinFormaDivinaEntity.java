package net.aelysium.aelysiummod.mixin;

import net.aelysium.aelysiummod.client.FormaDivinaCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinFormaDivinaEntity {

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void aelysium_cancelStepSound(CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            if (FormaDivinaCache.isActive(player.getUUID())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "spawnSprintParticle", at = @At("HEAD"), cancellable = true)
    private void aelysium_cancelSprintParticle(CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            if (FormaDivinaCache.isActive(player.getUUID())) {
                ci.cancel();
            }
        }
    }
}