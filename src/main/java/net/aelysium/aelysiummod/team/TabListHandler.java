package net.aelysium.aelysiummod.team;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * Handler para aplicar cores customizadas na Tab List
 */
@EventBusSubscriber(modid = "aelysiummod", bus = EventBusSubscriber.Bus.GAME)
public class TabListHandler {

    /**
     * Evento principal que formata o nome do jogador na Tab List
     * Este é o método correto para modificar nomes na tab list no NeoForge
     */
    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            PlayerTeam team = player.getTeam();

            if (team != null) {
                // Verifica se o time tem cor customizada
                TimeCorGerenciador.getTeamColor(team).ifPresent(customColor -> {
                    Component displayName = buildCustomDisplayName(player, team, customColor);
                    event.setDisplayName(displayName);
                });
            }
        }
    }

    /**
     * Força atualização da tab list quando o jogador entra
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            // Agenda a atualização após o join completo
            player.getServer().execute(() -> {
                player.refreshTabListName();
                // Atualiza todos os jogadores para sincronizar
                player.getServer().getPlayerList().getPlayers().forEach(ServerPlayer::refreshTabListName);
            });
        }
    }

    /**
     * Atualiza quando o jogador respawna
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.getServer().execute(() -> player.refreshTabListName());
        }
    }

    /**
     * Atualiza quando o jogador muda de dimensão
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.refreshTabListName();
        }
    }

    /**
     * Atualiza quando o jogador muda de gamemode
     */
    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            player.getServer().execute(() -> player.refreshTabListName());
        }
    }

    /**
     * Constrói o display name customizado com cores do time
     */
    private static Component buildCustomDisplayName(ServerPlayer player, PlayerTeam team, CustomTimeCor customColor) {
        Component prefix = team.getPlayerPrefix();
        Component suffix = team.getPlayerSuffix();

        MutableComponent finalPrefix = Component.literal("");
        MutableComponent finalSuffix = Component.literal("");

        // Aplica cor customizada ao prefixo se definida
        if (!prefix.getString().isEmpty()) {
            TimeCorGerenciador.getPrefixColor(team.getName()).ifPresentOrElse(
                    prefixColor -> finalPrefix.append(
                            Component.literal(prefix.getString()).setStyle(prefixColor.getStyle())
                    ),
                    () -> finalPrefix.append(prefix)
            );
        }

        // Aplica cor customizada ao sufixo se definida
        if (!suffix.getString().isEmpty()) {
            TimeCorGerenciador.getSuffixColor(team.getName()).ifPresentOrElse(
                    suffixColor -> finalSuffix.append(
                            Component.literal(suffix.getString()).setStyle(suffixColor.getStyle())
                    ),
                    () -> finalSuffix.append(suffix)
            );
        }

        // Cria o nome do jogador com a cor e estilo customizados
        MutableComponent playerName = Component.literal(player.getGameProfile().getName())
                .setStyle(customColor.getStyle());

        // Constrói o display name completo
        MutableComponent fullDisplayName = Component.literal("");

        // Adiciona prefixo se existir
        if (!prefix.getString().isEmpty()) {
            fullDisplayName.append(finalPrefix).append(" ");
        }

        // Adiciona o nome do jogador
        fullDisplayName.append(playerName);

        // Adiciona sufixo se existir
        if (!suffix.getString().isEmpty()) {
            fullDisplayName.append(" ").append(finalSuffix);
        }

        return fullDisplayName;
    }

    /**
     * Método auxiliar para atualizar a tab list de todos os jogadores
     * Útil quando você altera configurações de time
     */
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