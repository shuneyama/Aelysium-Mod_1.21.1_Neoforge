package net.aelysium.aelysiummod.sistemas.social.apelido;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorEventosApelido {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            player.getServer().execute(() -> {
                DadosApelido data = DadosApelido.get(player.getServer());

                data.syncToPlayer(player);

                for (ServerPlayer online : player.getServer().getPlayerList().getPlayers()) {
                    if (data.hasNickname(online.getUUID())) {
                        online.refreshTabListName();
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            DadosApelido data = DadosApelido.get(player.getServer());
            MutableComponent styled = data.getStyledFullNameComPronomes(player.getUUID());
            MutableComponent badge = PerfilJogador.insignia().componente(player);

            if (badge != null) {
                Component base = styled;
                if (base == null) base = event.getDisplayName();
                if (base == null) base = Component.literal(player.getGameProfile().getName());
                event.setDisplayName(Component.empty().append(badge).append(" ").append(base));
            } else if (styled != null) {
                event.setDisplayName(styled);
            }
        }
    }

    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            DadosApelido data = DadosApelido.get(player.getServer());
            MutableComponent styled = data.getStyledFullName(player.getUUID());
            if (styled != null) {
                event.setDisplayname(styled);
            }
        }
    }
}