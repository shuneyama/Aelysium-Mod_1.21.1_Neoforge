package net.aelysium.aelysiummod.conteudo.item.glock.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteRecuoGlock;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteSomGlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.aelysium.aelysiummod.nucleo.cliente.VolumeSomCliente;

public class ManipuladoresGlockCliente {

    public static void handleGlockSom(PacoteSomGlock payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ResourceLocation somId = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID,
                    payload.tiro() ? "glock.tiro" : "glock.pegando");
            float volume = (float) VolumeSomCliente.normalizado();

            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                    SoundEvent.createVariableRangeEvent(somId), SoundSource.PLAYERS,
                    volume, 1.0f, SoundInstance.createUnseededRandom(),
                    payload.x(), payload.y(), payload.z()));
        });
    }

    public static void handleGlockRecuo(PacoteRecuoGlock payload, IPayloadContext context) {
        context.enqueueWork(ManipuladorRecuoGlock::aplicarRecuo);
    }
}
