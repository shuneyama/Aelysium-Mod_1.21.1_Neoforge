package net.aelysium.aelysiummod.sistemas.admin.proibicao;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorEventosProibicao {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        DadosProibicao data = DadosProibicao.get(server);
        if (!data.estaProibido(player.getGameProfile().getName())) return;

        player.connection.disconnect(MensagemFalhaFalsa.gerar());
    }
}
