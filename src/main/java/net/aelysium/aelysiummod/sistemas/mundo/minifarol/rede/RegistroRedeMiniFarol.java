package net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede;

import net.aelysium.aelysiummod.sistemas.mundo.minifarol.cliente.ManipuladorMiniFarolCliente;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeMiniFarol {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                PacoteAbrirMiniFarol.TYPE,
                PacoteAbrirMiniFarol.CODEC,
                (pacote, ctx) -> ctx.enqueueWork(() -> {
                    if (FMLEnvironment.dist.isClient()) {
                        ManipuladorMiniFarolCliente.abrir(pacote);
                    }
                }));

        registrar.playToServer(
                PacoteConfigurarMiniFarol.TYPE,
                PacoteConfigurarMiniFarol.CODEC,
                PacoteConfigurarMiniFarol::handle);
    }
}
