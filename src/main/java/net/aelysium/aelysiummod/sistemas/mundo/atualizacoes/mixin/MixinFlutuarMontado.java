package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinFlutuarMontado {

    @Inject(method = "travel", at = @At("TAIL"))
    private void aelysium$flutuar(Vec3 entrada, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isVehicle()) return;
        EntityType<?> tipo = self.getType();
        if (tipo != EntityType.HORSE && tipo != EntityType.ZOMBIE_HORSE && tipo != EntityType.MULE && tipo != EntityType.DONKEY && tipo != EntityType.CAMEL) return;
        double limite = self.getFluidJumpThreshold();
        if (self.getFluidHeight(FluidTags.WATER) > limite || self.getFluidHeight(FluidTags.LAVA) > limite) {
            self.setDeltaMovement(self.getDeltaMovement().add(0.0, 0.04, 0.0));
        }
    }
}
