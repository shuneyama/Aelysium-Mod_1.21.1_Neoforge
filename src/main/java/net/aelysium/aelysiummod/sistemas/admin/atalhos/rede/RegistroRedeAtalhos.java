package net.aelysium.aelysiummod.sistemas.admin.atalhos.rede;

import net.aelysium.aelysiummod.sistemas.admin.atalhos.EstadoEspiao;
import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorEspiaoSocial;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RegistroRedeAtalhos {
    private static final int PERMISSAO = 2;

    private RegistroRedeAtalhos() {
    }

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToServer(PacoteAlternarEspiao.TYPE, PacoteAlternarEspiao.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer jogador && jogador.hasPermissions(PERMISSAO)) {
                        alternarEspiao(jogador);
                    }
                }));

        registrar.playToClient(PacoteEstadoEspiao.TYPE, PacoteEstadoEspiao.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> EstadoEspiao.definir(pkt.nivel())));
    }

    private static void alternarEspiao(ServerPlayer jogador) {
        int nivel = GerenciadorEspiaoSocial.alternar(jogador.getUUID());
        PacketDistributor.sendToPlayer(jogador, new PacoteEstadoEspiao(nivel));
    }
}
