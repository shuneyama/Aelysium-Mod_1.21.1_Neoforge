package net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.aelysium.aelysiummod.nucleo.cliente.VolumeSomCliente;

@OnlyIn(Dist.CLIENT)
public class SomAmbienteRitualMorte extends AbstractTickableSoundInstance {

    private float volumeAlvo;

    public SomAmbienteRitualMorte(SoundEvent som, float pitch) {
        super(som, SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0001f;
        this.pitch = pitch;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    public void definirVolume(float alvo) {
        volumeAlvo = alvo;
    }

    public void parar() {
        stop();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        volume = Math.max(0.0001f, volumeAlvo * (float) VolumeSomCliente.normalizado());
    }
}
