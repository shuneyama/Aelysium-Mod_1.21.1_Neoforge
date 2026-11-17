package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Leashable.class)
public interface MixinDistanciaAmarra {

    @Redirect(method = "tickLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;distanceTo(Lnet/minecraft/world/entity/Entity;)F"))
    private static float aelysium$centros(Entity a, Entity b) {
        return (float) a.getBoundingBox().getCenter().distanceTo(b.getBoundingBox().getCenter());
    }

    @ModifyConstant(method = "tickLeash", constant = @Constant(doubleValue = 10.0D))
    private static double aelysium$distancia(double original) {
        return 12.0D;
    }
}
