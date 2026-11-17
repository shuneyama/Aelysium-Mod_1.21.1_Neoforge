package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownPotion.class)
public abstract class MixinPocaoArremessada {

    @Redirect(method = "applySplash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    private double aelysium$distancia(ThrownPotion pocao, Entity alvo) {
        AABB a = pocao.getBoundingBox();
        AABB b = alvo.getBoundingBox();
        double dx = Math.max(0.0D, Math.max(a.minX - b.maxX, b.minX - a.maxX));
        double dy = Math.max(0.0D, Math.max(a.minY - b.maxY, b.minY - a.maxY));
        double dz = Math.max(0.0D, Math.max(a.minZ - b.maxZ, b.minZ - a.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }
}
