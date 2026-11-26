package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAtualizados;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
public abstract class MixinSomFlechaBloco {

    @ModifyArg(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), index = 0)
    private SoundEvent aelysium$somFlechaBloco(SoundEvent som) {
        if (som == SoundEvents.ARROW_HIT || som == SoundEvents.CROSSBOW_HIT) return SonsAtualizados.FLECHA_BLOCO;
        return som;
    }
}
