package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ComandoHolograma {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("holograma")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("criar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .executes(ctx -> AcoesHologramaCadastro.criarHolograma(ctx, null))
                                .then(Commands.argument("texto", StringArgumentType.greedyString())
                                        .executes(ctx -> AcoesHologramaCadastro.criarHolograma(ctx,
                                                StringArgumentType.getString(ctx, "texto")))
                                )
                        )
                )

                .then(Commands.literal("deletar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .executes(AcoesHologramaCadastro::deletarHolograma)
                        )
                )

                .then(Commands.literal("lista")
                        .executes(ctx -> AcoesHologramaConsulta.listarHologramas(ctx, 1))
                        .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                .executes(ctx -> AcoesHologramaConsulta.listarHologramas(ctx,
                                        IntegerArgumentType.getInteger(ctx, "pagina")))
                        )
                )

                .then(Commands.literal("info")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .executes(AcoesHologramaConsulta::infoHolograma)
                        )
                )

                .then(Commands.literal("mover")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(AcoesHologramaPosicao::moverHolograma)
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("moveraqui")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .executes(AcoesHologramaPosicao::moverAqui)
                        )
                )

                .then(Commands.literal("teleportar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .executes(AcoesHologramaPosicao::teleportar)
                        )
                )

                .then(Commands.literal("renomear")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("novoNome", StringArgumentType.word())
                                        .executes(AcoesHologramaCadastro::renomear)
                                )
                        )
                )

                .then(Commands.literal("clonar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("novoNome", StringArgumentType.word())
                                        .executes(AcoesHologramaCadastro::clonar)
                                )
                        )
                )

                .then(Commands.literal("ativar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .executes(ctx -> AcoesHologramaAjustes.setAtivo(ctx, true))
                        )
                )

                .then(Commands.literal("desativar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .executes(ctx -> AcoesHologramaAjustes.setAtivo(ctx, false))
                        )
                )

                .then(Commands.literal("range")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("blocos", DoubleArgumentType.doubleArg(1, 256))
                                        .executes(AcoesHologramaAjustes::setRange)
                                )
                        )
                )

                .then(Commands.literal("intervalo")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 200))
                                        .executes(AcoesHologramaAjustes::setIntervalo)
                                )
                        )
                )

                .then(Commands.literal("alinhar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("outroNome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("eixo", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        List.of("x", "y", "z", "xz"), builder))
                                                .executes(AcoesHologramaPosicao::alinhar)
                                        )
                                )
                        )
                )

                .then(Commands.literal("linha")

                        .then(Commands.literal("adicionar")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                .executes(AcoesHologramaLinhas::linhaAdicionar)
                                        )
                                )
                        )

                        .then(Commands.literal("inserir")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                        .executes(AcoesHologramaLinhas::linhaInserir)
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("remover")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .executes(AcoesHologramaLinhas::linhaRemover)
                                        )
                                )
                        )

                        .then(Commands.literal("editar")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                        .executes(AcoesHologramaLinhas::linhaEditar)
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("trocar")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice1", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("indice2", IntegerArgumentType.integer(1))
                                                        .executes(AcoesHologramaLinhas::linhaTrocar)
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("altura")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(UtilitariosComandoHolograma.SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("altura", DoubleArgumentType.doubleArg(0.0, 10.0))
                                                        .executes(AcoesHologramaLinhas::linhaAltura)
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("ajuda")
                        .executes(ComandoHolograma::ajuda)
                )

                .executes(ComandoHolograma::ajuda)
        );
    }

    private static int ajuda(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_ajuda"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_criar_nome_texto_cria_um"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_deletar_nome_deleta_um"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_lista_pagina_lista"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_info_nome_informacoes"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_mover_nome_x_y_z_move_o"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_moveraqui_nome_move_para"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_teleportar_nome_tp_ate_o"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_renomear_nome_novo_renomeia"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_clonar_nome_novo_clona_o"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_ativar_desativar_nome_liga"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_range_nome_blocos_range_de"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_alinhar_nome_outro_eixo"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.linhas_2"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_linha_adicionar_nome_texto"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_linha_inserir_nome_indice"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_linha_remover_nome_indice"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_linha_editar_nome_indice"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_linha_trocar_nome_i1_i2"), false);
        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.holograma_linha_altura_nome_indice"), false);
        return 1;
    }
}
