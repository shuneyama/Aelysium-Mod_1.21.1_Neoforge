package net.aelysium.aelysiummod.conteudo.filhotes.clima;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RegistroRedeClima {

    private RegistroRedeClima() {
    }

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteClimaAnimal.TYPE, PacoteClimaAnimal.STREAM_CODEC,
                (pacote, contexto) -> contexto.enqueueWork(() -> CacheClimaCliente.definir(pacote.entidade(), pacote.clima())));
    }
}
