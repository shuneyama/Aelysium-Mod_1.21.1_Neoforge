package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.AvisoMusica;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class MixinMusicaCliente {

    @Shadow
    private int nextSongDelay;

    @Shadow
    private SoundInstance currentMusic;

    @Inject(method = "tick", at = @At("HEAD"))
    private void aelysium$frequencia(CallbackInfo ci) {
        if (currentMusic != null) return;
        OpcoesAtualizadas.FrequenciaMusica frequencia = OpcoesAtualizadas.FREQUENCIA.get();
        if (frequencia == OpcoesAtualizadas.FrequenciaMusica.CONSTANTE) {
            nextSongDelay = Math.min(nextSongDelay, 0);
        } else if (frequencia == OpcoesAtualizadas.FrequenciaMusica.FREQUENTE) {
            nextSongDelay = Math.min(nextSongDelay, 1200);
        }
    }

    @Inject(method = "startPlaying", at = @At("RETURN"))
    private void aelysium$aviso(Music seletor, CallbackInfo ci) {
        if (currentMusic != null && OpcoesAtualizadas.AVISO_MUSICA.get()) {
            AvisoMusica.mostrar(currentMusic);
        }
    }
}
