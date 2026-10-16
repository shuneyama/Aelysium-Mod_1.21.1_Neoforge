package net.aelysium.aelysiummod.sistemas.social.insignia;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.insignia.rede.RedeInsignia;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorEventosInsignia {

    @SubscribeEvent
    public static void aoIniciarServidor(ServerStartedEvent event) {
        GerenciadorInsigniasPersonalizadas.carregar(event.getServer());
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent event) {
        GerenciadorInsigniasPersonalizadas.limpar();
    }

    @SubscribeEvent
    public static void aoEntrar(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer jogador) {
            RedeInsignia.sincronizarPara(jogador);
            DadosInsignia.get(jogador.getServer()).registrar(jogador.getUUID());
        }
    }
}
