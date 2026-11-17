package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SimpleSoundInstance.class)
public abstract class MixinSomInterface {

    @ModifyVariable(method = "forUI(Lnet/minecraft/sounds/SoundEvent;FF)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static float aelysium$volumeUi(float volume) {
        return volume * OpcoesAtualizadas.VOLUME_UI.get().floatValue();
    }
}
