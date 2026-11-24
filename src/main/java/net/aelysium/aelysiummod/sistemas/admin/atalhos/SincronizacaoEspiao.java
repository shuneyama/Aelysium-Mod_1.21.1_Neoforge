package net.aelysium.aelysiummod.sistemas.admin.atalhos;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.atalhos.rede.PacoteEstadoEspiao;
import net.aelysium.aelysiummod.sistemas.social.chat.GerenciadorEspiaoSocial;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class SincronizacaoEspiao {
    private SincronizacaoEspiao() {
    }

    @SubscribeEvent
    public static void aoEntrar(PlayerEvent.PlayerLoggedInEvent evento) {
        if (evento.getEntity() instanceof ServerPlayer jogador && GerenciadorEspiaoSocial.isAtivo(jogador.getUUID())) {
            PacketDistributor.sendToPlayer(jogador, new PacoteEstadoEspiao(GerenciadorEspiaoSocial.nivel(jogador.getUUID())));
        }
    }
}
