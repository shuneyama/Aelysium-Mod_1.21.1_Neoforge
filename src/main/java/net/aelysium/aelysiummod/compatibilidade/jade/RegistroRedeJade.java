package net.aelysium.aelysiummod.compatibilidade.jade;

import net.aelysium.aelysiummod.compatibilidade.jade.PacoteSincJogadoresOp;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistroRedeJade {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteSincJogadoresOp.TYPE, PacoteSincJogadoresOp.STREAM_CODEC, PacoteSincJogadoresOp::handle);
    }
}
