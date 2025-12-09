package net.aelysium.aelysiummod.time;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class TabLista {

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof FakePlayer) return;

            PlayerTeam team = serverPlayer.getTeam();
            if (team != null) {
                TimeCorGerenciador.getTeamColor(team).ifPresent(customColor -> {
                    Component displayName = buildCustomDisplayName(serverPlayer, team, customColor);
                    event.setDisplayName(displayName);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof FakePlayer) return;

            serverPlayer.getServer().execute(() -> {
                serverPlayer.refreshTabListName();
                serverPlayer.getServer().getPlayerList().getPlayers().forEach(ServerPlayer::refreshTabListName);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof FakePlayer) return;

            serverPlayer.getServer().execute(serverPlayer::refreshTabListName);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof FakePlayer) return;

            serverPlayer.refreshTabListName();
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof FakePlayer) return;

            serverPlayer.getServer().execute(serverPlayer::refreshTabListName);
        }
    }

    private static Component buildCustomDisplayName(ServerPlayer player, PlayerTeam team, CustomTimeCor customColor) {
        Component prefix = team.getPlayerPrefix();
        Component suffix = team.getPlayerSuffix();

        MutableComponent finalPrefix = Component.literal("");
        MutableComponent finalSuffix = Component.literal("");

        // Processa o prefixo
        if (!prefix.getString().isEmpty()) {
            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresentOrElse(
                    prefixColor -> finalPrefix.append(
                            Component.literal(prefix.getString()).setStyle(prefixColor.getStyle())
                    ),
                    () -> finalPrefix.append(prefix)
            );
        }

        // Processa o sufixo
        if (!suffix.getString().isEmpty()) {
            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresentOrElse(
                    suffixColor -> finalSuffix.append(
                            Component.literal(suffix.getString()).setStyle(suffixColor.getStyle())
                    ),
                    () -> finalSuffix.append(suffix)
            );
        }

        // Nome do jogador com estilo customizado
        MutableComponent playerName = Component.literal(player.getGameProfile().getName())
                .setStyle(customColor.getStyle());

        // Monta o nome completo
        MutableComponent fullDisplayName = Component.literal("");

        if (!prefix.getString().isEmpty()) {
            fullDisplayName.append(finalPrefix).append(" ");
        }

        fullDisplayName.append(playerName);

        if (!suffix.getString().isEmpty()) {
            fullDisplayName.append(" ").append(finalSuffix);
        }

        return fullDisplayName;
    }

    public static void updateAllPlayersTabList(ServerPlayer triggerPlayer) {
        if (triggerPlayer != null && triggerPlayer.getServer() != null) {
            triggerPlayer.getServer().getPlayerList().getPlayers().forEach(player -> {
                if (!(player instanceof FakePlayer)) {
                    player.refreshTabListName();
                }
            });
        }
    }
}