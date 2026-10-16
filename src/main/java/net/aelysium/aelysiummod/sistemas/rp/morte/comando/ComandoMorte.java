package net.aelysium.aelysiummod.sistemas.rp.morte.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.aelysium.aelysiummod.sistemas.rp.morte.AnimacaoMorte;
import net.aelysium.aelysiummod.sistemas.rp.morte.ConfiguracaoMorte;
import net.aelysium.aelysiummod.sistemas.rp.morte.UtilitariosEfeitoMorte;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ComandoMorte {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("morte")
                .then(Commands.literal("habilitar")
                        .executes(ctx -> {
                            if (ConfiguracaoMorte.isHabilitada()) {
                                ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.morte.mortes_canonicas_ja_estao_habilitadas"));
                                return 0;
                            }

                            ConfiguracaoMorte.setHabilitada(true);
                            MinecraftServer server = ctx.getSource().getServer();
                            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                UtilitariosEfeitoMorte.aplicar(player);
                            }
                            AnimacaoMorte.start(server);

                            ctx.getSource().sendSuccess(() ->
                                    Component.translatable("mensagem.aelysiummod.morte.mortes_canonicas_habilitadas"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("desabilitar")
                        .executes(ctx -> {
                            if (!ConfiguracaoMorte.isHabilitada()) {
                                ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.morte.mortes_canonicas_ja_estao"));
                                return 0;
                            }

                            ConfiguracaoMorte.setHabilitada(false);
                            MinecraftServer server = ctx.getSource().getServer();
                            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                UtilitariosEfeitoMorte.remover(player);
                            }
                            AnimacaoMorte.startDisable(server);

                            ctx.getSource().sendSuccess(() ->
                                    Component.translatable("mensagem.aelysiummod.morte.mortes_canonicas_desabilitadas"), true);
                            return 1;
                        })
                );
    }
}
