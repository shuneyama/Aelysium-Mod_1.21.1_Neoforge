package net.aelysium.aelysiummod.sistemas.admin.cabeca.rede;

import net.aelysium.aelysiummod.sistemas.admin.cabeca.cliente.ManipuladorCabecaCliente;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeCabeca {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(
                PacoteAbrirCabeca.TYPE,
                PacoteAbrirCabeca.CODEC,
                (pacote, ctx) -> ctx.enqueueWork(() -> {
                    if (FMLEnvironment.dist.isClient()) {
                        ManipuladorCabecaCliente.abrir(pacote);
                    }
                }));

        registrar.playToServer(
                PacoteDefinirCabeca.TYPE,
                PacoteDefinirCabeca.CODEC,
                PacoteDefinirCabeca::handle);
    }
}
