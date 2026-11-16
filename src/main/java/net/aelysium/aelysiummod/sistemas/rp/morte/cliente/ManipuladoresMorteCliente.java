package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.aelysium.aelysiummod.sistemas.rp.morte.rede.PacoteImpactoMorte;
import net.minecraft.client.Minecraft;
import net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.TelaAberturaMorteCanonica;
import net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.TelaBaseRitualMorte;
import net.aelysium.aelysiummod.sistemas.rp.morte.cliente.abertura.TelaEncerramentoMorteCanonica;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ManipuladoresMorteCliente {

    public static void handleMorteImpacto(PacoteImpactoMorte payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.habilitando()) {
                abrirAberturaMorteCanonica();
            } else {
                abrirEncerramentoMorteCanonica();
            }
        });
    }

    public static void abrirAberturaMorteCanonica() {
        if (!podeAbrirRitual()) return;
        Minecraft.getInstance().setScreen(new TelaAberturaMorteCanonica());
    }

    public static void abrirEncerramentoMorteCanonica() {
        if (!podeAbrirRitual()) return;
        Minecraft.getInstance().setScreen(new TelaEncerramentoMorteCanonica());
    }

    private static boolean podeAbrirRitual() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.player != null && !(mc.screen instanceof TelaBaseRitualMorte);
    }
}
