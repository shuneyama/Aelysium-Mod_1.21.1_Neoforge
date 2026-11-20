package net.aelysium.aelysiummod.sistemas.admin.manutencao;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorManutencao {

    private static final Component MENSAGEM_MANUTENCAO = Component.literal(
            "§c§lAelysium está em manutenção!\n\n§7Fique de olho nos avisos do Discord!"
    );

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!PerfilJogador.habilidades().manutencaoAtiva()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.hasPermissions(2)) {
                player.getServer().execute(() -> {
                    player.connection.disconnect(MENSAGEM_MANUTENCAO);
                });
            }
        }
    }
}