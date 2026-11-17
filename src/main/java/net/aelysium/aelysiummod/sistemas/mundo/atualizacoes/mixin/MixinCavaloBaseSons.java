package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAnimais;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractHorse.class)
public abstract class MixinCavaloBaseSons {

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void aelysium$passo(BlockPos pos, BlockState estado, CallbackInfo ci) {
        AbstractHorse cavalo = (AbstractHorse) (Object) this;
        if (!(cavalo instanceof Horse) || !cavalo.isBaby() || estado.liquid()) return;
        SoundEvent som = SonsAnimais.trocar(cavalo, "horse", List.of(), "step", SoundEvents.HORSE_STEP);
        if (som != SoundEvents.HORSE_STEP) {
            cavalo.playSound(som, 0.15F, 1.0F);
            ci.cancel();
        }
    }

    @ModifyArg(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), index = 0)
    private SoundEvent aelysium$pousar(SoundEvent original) {
        AbstractHorse cavalo = (AbstractHorse) (Object) this;
        return cavalo instanceof Horse ? SonsAnimais.trocar(cavalo, "horse", List.of(), "land", original) : original;
    }
}
