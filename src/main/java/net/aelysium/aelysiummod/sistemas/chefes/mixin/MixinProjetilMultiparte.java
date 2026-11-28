package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MultipartAwareEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class MixinProjetilMultiparte extends Entity {

    public MixinProjetilMultiparte(EntityType<?> tipo, Level nivel) {
        super(tipo, nivel);
    }

    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
    private void aelysium$aoAcertar(HitResult resultado, CallbackInfo ci) {
        if (resultado.getType() == HitResult.Type.ENTITY
                && resultado instanceof EntityHitResult acerto
                && acerto.getEntity() instanceof MultipartAwareEntity multiparte) {
            multiparte.setNextDamagedPart(multiparte.getBounds().raycast(position(), position().add(getDeltaMovement())));
        }
    }
}
