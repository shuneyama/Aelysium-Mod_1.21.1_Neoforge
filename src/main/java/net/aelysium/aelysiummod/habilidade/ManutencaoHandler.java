package net.aelysium.aelysiummod.habilidade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManutencaoHandler {

    private static final Component MENSAGEM_MANUTENCAO = Component.literal(
            "§c§lAelysium está em manutenção!\n\n§7Fique de olho nos avisos do Discord!"
    );

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!HabilidadeManager.isManutencaoAtiva()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.hasPermissions(2)) {
                player.getServer().execute(() -> {
                    player.connection.disconnect(MENSAGEM_MANUTENCAO);
                });
            }
        }
    }
}