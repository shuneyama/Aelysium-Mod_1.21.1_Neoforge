package net.aelysium.aelysiummod.nucleo.guarda;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeGuarda {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                net.aelysium.aelysiummod.nucleo.guarda.GuardaRede.PacoteSincGuarda.TYPE,
                net.aelysium.aelysiummod.nucleo.guarda.GuardaRede.PacoteSincGuarda.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    net.aelysium.aelysiummod.nucleo.guarda.EstadoGuardaCliente.set(payload.autorizado());
                    if (net.neoforged.fml.ModList.get().isLoaded("emi")) {
                        net.aelysium.aelysiummod.compatibilidade.emi.PonteAelysiumEmi.atualizarSeNecessario();
                    }
                    if (net.neoforged.fml.ModList.get().isLoaded("jei")) {
                        net.aelysium.aelysiummod.compatibilidade.jei.PonteAelysiumJei.atualizarSeNecessario();
                    }
                }));
    }
}
