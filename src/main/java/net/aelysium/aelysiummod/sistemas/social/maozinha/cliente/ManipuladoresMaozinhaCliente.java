package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacoteDeslocamentoMaozinha;
import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacoteEstadoMaozinha;
import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacotePuloMaozinha;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ManipuladoresMaozinhaCliente {

    public static void handleEstado(PacoteEstadoMaozinha payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            switch (payload.fase()) {
                case OFERTA -> CacheMaozinha.ofertar(payload.lider(), payload.alvo());
                case SEGURANDO -> CacheMaozinha.ligar(payload.lider(), payload.alvo());
                case DESFEITA -> CacheMaozinha.desfazer(payload.lider(), payload.alvo());
            }
        });
    }

    public static void handlePulo(PacotePuloMaozinha payload, IPayloadContext context) {
        context.enqueueWork(ManipuladorPuxaoCliente::solicitarPulo);
    }

    public static void handleDeslocamento(PacoteDeslocamentoMaozinha payload, IPayloadContext context) {
        context.enqueueWork(() -> SuavizadorPosicaoMaozinha.receber(payload.seguidor(),
                new Vec3(payload.x(), payload.y(), payload.z())));
    }
}
