package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.holograma.Holograma;
import net.aelysium.aelysiummod.sistemas.midia.holograma.TemporizadorHolograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class UtilitariosComandoHolograma {

    static final SuggestionProvider<CommandSourceStack> SUGESTAO_HOLOGRAMAS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    GerenciadorHologramas.getInstance().getNomesHologramas(), builder);

    static void atualizarVisual(Holograma holo, CommandSourceStack source) {
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        TemporizadorHolograma.atualizarParaTodos(holo, players);
    }
}
