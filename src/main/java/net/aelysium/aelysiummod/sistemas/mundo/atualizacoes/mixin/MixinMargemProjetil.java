package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public abstract class MixinMargemProjetil {

    @ModifyVariable(method = "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static float aelysium$margem(float margem, Level mundo, Entity projetil, Vec3 inicio, Vec3 fim, AABB caixa, Predicate<Entity> filtro) {
        if (margem != 0.3F) return margem;
        int idade = projetil.tickCount;
        if (idade <= 2) return 0.0F;
        return Math.min(0.3F, (idade - 2) * 0.05F);
    }
}
