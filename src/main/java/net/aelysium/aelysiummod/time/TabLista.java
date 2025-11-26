package net.aelysium.aelysiummod.time;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.util.FakePlayer;

@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class TabLista {

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            PlayerTeam team = player.getTeam();

            if (team != null) {
                TimeCorGerenciador.getTeamColor(team).ifPresent(customColor -> {
                    Component displayName = buildCustomDisplayName(player, team, customColor);
                    event.setDisplayName(displayName);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.getServer().execute(() -> {
                player.refreshTabListName();
                player.getServer().getPlayerList().getPlayers().forEach(ServerPlayer::refreshTabListName);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.getServer().execute(() -> player.refreshTabListName());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.refreshTabListName();
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.getServer().execute(() -> player.refreshTabListName());
        }
    }

    private static Component buildCustomDisplayName(ServerPlayer player, PlayerTeam team, CustomTimeCor customColor) {
        Component prefix = team.getPlayerPrefix();
        Component suffix = team.getPlayerSuffix();

        MutableComponent finalPrefix = Component.literal("");
        MutableComponent finalSuffix = Component.literal("");

        if (!prefix.getString().isEmpty()) {
            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresentOrElse(
                    prefixColor -> finalPrefix.append(
                            Component.literal(prefix.getString()).setStyle(prefixColor.getStyle())
                    ),
                    () -> finalPrefix.append(prefix)
            );
        }

        if (!suffix.getString().isEmpty()) {
            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresentOrElse(
                    suffixColor -> finalSuffix.append(
                            Component.literal(suffix.getString()).setStyle(suffixColor.getStyle())
                    ),
                    () -> finalSuffix.append(suffix)
            );
        }

        MutableComponent playerName = Component.literal(player.getGameProfile().getName())
                .setStyle(customColor.getStyle());

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
        if (triggerPlayer.getServer() != null) {
            triggerPlayer.getServer().getPlayerList().getPlayers().forEach(player -> {
                if (!(player instanceof FakePlayer)) {
                    player.refreshTabListName();
                }
            });
        }
    }
}