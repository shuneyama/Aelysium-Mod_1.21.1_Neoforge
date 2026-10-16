package net.aelysium.aelysiummod.sistemas.admin.protecao.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTela;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTelaEdicao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class ComandoProtecao {

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES_REGIOES = (context, builder) -> {
        List<String> nomes = GerenciadorRegioes.getInstance().getNomesRegioes();
        return SharedSuggestionProvider.suggest(nomes, builder);
    };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("protecao")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("criar")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> criar(context.getSource()))
                )
                .then(Commands.literal("lista")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> listar(context.getSource()))
                )
                .then(Commands.literal("remover")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("região", StringArgumentType.string())
                                .suggests(SUGESTOES_REGIOES)
                                .executes(context -> remover(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "região")
                                ))
                        )
                )
                .then(Commands.literal("editar")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("região", StringArgumentType.string())
                                .suggests(SUGESTOES_REGIOES)
                                .executes(context -> editar(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "região")
                                ))
                        )
                )
                .then(Commands.literal("domo")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("região", StringArgumentType.string())
                                .suggests(SUGESTOES_REGIOES)
                                .then(Commands.literal("ativar")
                                        .executes(context -> domoAtivar(context.getSource(),
                                                StringArgumentType.getString(context, "região"), true))
                                )
                                .then(Commands.literal("desativar")
                                        .executes(context -> domoAtivar(context.getSource(),
                                                StringArgumentType.getString(context, "região"), false))
                                )
                                .then(Commands.literal("cor")
                                        .then(Commands.argument("hex", StringArgumentType.word())
                                                .executes(context -> domoCor(context.getSource(),
                                                        StringArgumentType.getString(context, "região"),
                                                        StringArgumentType.getString(context, "hex")))
                                        )
                                )
                                .then(Commands.literal("distancia")
                                        .then(Commands.argument("blocos", IntegerArgumentType.integer(10, 500))
                                                .executes(context -> domoDistancia(context.getSource(),
                                                        StringArgumentType.getString(context, "região"),
                                                        IntegerArgumentType.getInteger(context, "blocos")))
                                        )
                                )
                        )
                );
    }

    private static Regiao obterRegiaoOuFalhar(CommandSourceStack source, String nomeRegiao) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (!gerenciador.existeRegiao(nomeRegiao)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_nao_existe", nomeRegiao));
            return null;
        }
        return gerenciador.getRegiao(nomeRegiao);
    }

    private static int domoAtivar(CommandSourceStack source, String nomeRegiao, boolean ativo) {
        Regiao regiao = obterRegiaoOuFalhar(source, nomeRegiao);
        if (regiao == null) return 0;

        regiao.setDomoAtivo(ativo);
        GerenciadorRegioes.getInstance().marcarAlteracao();

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.pra_regiao", Component.translatable(ativo ? "mensagem.aelysiummod.protecao.domo_ativado" : "mensagem.aelysiummod.protecao.domo_desativado"), nomeRegiao), true);
        return 1;
    }

    private static int domoCor(CommandSourceStack source, String nomeRegiao, String hex) {
        Regiao regiao = obterRegiaoOuFalhar(source, nomeRegiao);
        if (regiao == null) return 0;

        String limpo = hex.startsWith("#") ? hex.substring(1) : hex;
        int cor;
        try {
            cor = Integer.parseInt(limpo, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.cor_invalida_use_um_hexadecimal_ex"));
            return 0;
        }

        regiao.setDomoCor(cor);
        GerenciadorRegioes.getInstance().marcarAlteracao();

        String corFinal = String.format("%06X", cor);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.cor_do_domo_de_definida_como", nomeRegiao, corFinal), true);
        return 1;
    }

    private static int domoDistancia(CommandSourceStack source, String nomeRegiao, int blocos) {
        Regiao regiao = obterRegiaoOuFalhar(source, nomeRegiao);
        if (regiao == null) return 0;

        regiao.setDomoDistancia((float) blocos);
        GerenciadorRegioes.getInstance().marcarAlteracao();

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.distancia_de_renderizacao_do_domo_de", nomeRegiao, blocos), true);
        return 1;
    }

    private static int criar(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer jogador)) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.apenas_jogadores_podem_usar_este"));
            return 0;
        }

        if (GerenciadorSelecao.temSelecaoCompleta(jogador.getUUID())) {
            abrirTelaCriacao(jogador);
            return 1;
        }

        GerenciadorSelecao.iniciarSelecaoRapida(jogador.getUUID());
        jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.quebre_bata_em_um_bloco_para_marcar_a"));
        jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.a_tela_de_criacao_abrira"));
        return 1;
    }

    public static void abrirTelaCriacao(ServerPlayer jogador) {
        BlockPos min = GerenciadorSelecao.getMinimo(jogador.getUUID());
        BlockPos max = GerenciadorSelecao.getMaximo(jogador.getUUID());
        PacketDistributor.sendToPlayer(jogador, new PacoteAbrirTela(min, max));
    }

    private static int editar(CommandSourceStack source, String nomeRegiao) {
        if (!(source.getEntity() instanceof ServerPlayer jogador)) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.apenas_jogadores_podem_usar_este"));
            return 0;
        }

        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (!gerenciador.existeRegiao(nomeRegiao)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_nao_existe", nomeRegiao));
            return 0;
        }

        PacketDistributor.sendToPlayer(jogador, PacoteAbrirTelaEdicao.deRegiao(jogador.server, gerenciador.getRegiao(nomeRegiao)));
        return 1;
    }

    private static int listar(CommandSourceStack source) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        List<String> nomesRegioes = gerenciador.getNomesRegioes();

        if (nomesRegioes.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.protecao.nenhuma_regiao_protegida_foi_criada"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.protecao.regioes_protegidas"), false);

        for (String nome : nomesRegioes) {
            Regiao regiao = gerenciador.getRegiao(nome);
            if (regiao == null) continue;

            BlockPos centro = regiao.getPosicaoCentral();

            MutableComponent mensagem = Component.literal("§e" + nome + " §7| ");

            MutableComponent botaoFlags = Component.translatable("mensagem.aelysiummod.protecao.ver_flags")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/debug _verflags " + nome))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("mensagem.aelysiummod.protecao.clique_para_ver_as_flags_desta_regiao")))
                            .withColor(ChatFormatting.GREEN));

            MutableComponent botaoDonos = Component.translatable("mensagem.aelysiummod.protecao.ver_donos")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/debug _verdonos " + nome))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("mensagem.aelysiummod.protecao.clique_para_ver_os_donos_desta_regiao")))
                            .withColor(ChatFormatting.AQUA));

            MutableComponent botaoTeleporte = Component.translatable("mensagem.aelysiummod.protecao.teleportar")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/debug _tp " + nome))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatableEscape("mensagem.aelysiummod.protecao.clique_para_teleportar_ao_centro", centro.getX(), centro.getY(), centro.getZ())))
                            .withColor(ChatFormatting.LIGHT_PURPLE));

            mensagem.append(botaoFlags).append(" ").append(botaoDonos).append(" ").append(botaoTeleporte);
            source.sendSuccess(() -> mensagem, false);
        }

        return 1;
    }

    private static int remover(CommandSourceStack source, String nomeRegiao) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (!gerenciador.existeRegiao(nomeRegiao)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_nao_existe", nomeRegiao));
            return 0;
        }

        gerenciador.removerRegiao(nomeRegiao);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_removida_com_sucesso", nomeRegiao), true);
        return 1;
    }

    public static void registrarSubComandosLista(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("debug")
                        .then(Commands.literal("_verflags")
                                .then(Commands.argument("região", StringArgumentType.string())
                                        .executes(context -> verFlags(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "região")
                                        ))
                                )
                        )
                        .then(Commands.literal("_verdonos")
                                .then(Commands.argument("região", StringArgumentType.string())
                                        .executes(context -> verDonos(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "região")
                                        ))
                                )
                        )
                        .then(Commands.literal("_tp")
                                .then(Commands.argument("região", StringArgumentType.string())
                                        .executes(context -> teleportarRegiao(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "região")
                                        ))
                                )
                        )
        );
    }

    private static int verFlags(CommandSourceStack source, String nomeRegiao) {
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiao(nomeRegiao);
        if (regiao == null) { source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.regiao_nao_encontrada")); return 0; }

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.flags_da_regiao", nomeRegiao), false);
        for (FlagRegiao flag : FlagRegiao.values()) {
            boolean valor = regiao.getFlagValor(flag);
            source.sendSuccess(() -> Component.translatableEscape(valor
                    ? "mensagem.aelysiummod.protecao.flag_ativada"
                    : "mensagem.aelysiummod.protecao.flag_desativada", flag.getNome()), false);
        }
        return 1;
    }

    private static int verDonos(CommandSourceStack source, String nomeRegiao) {
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiao(nomeRegiao);
        if (regiao == null) { source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.regiao_nao_encontrada")); return 0; }

        if (regiao.getDonos().isEmpty()) {
            source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.a_regiao_nao_possui_donos", nomeRegiao), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.protecao.donos_da_regiao", nomeRegiao), false);
        if (source.getServer() != null) {
            regiao.getDonos().forEach(uuid -> {
                ServerPlayer jogador = source.getServer().getPlayerList().getPlayer(uuid);
                String nome = jogador != null ? getNomeExibicao(jogador) : uuid.toString();
                source.sendSuccess(() -> Component.literal("§7- §e" + nome), false);
            });
        }
        return 1;
    }

    private static int teleportarRegiao(CommandSourceStack source, String nomeRegiao) {
        if (!(source.getEntity() instanceof ServerPlayer jogador)) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.apenas_jogadores_podem_se_teleportar"));
            return 0;
        }

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiao(nomeRegiao);
        if (regiao == null) { source.sendFailure(Component.translatable("mensagem.aelysiummod.protecao.regiao_nao_encontrada")); return 0; }

        BlockPos centro = regiao.getPosicaoCentral();
        jogador.setGameMode(GameType.SPECTATOR);
        jogador.teleportTo(centro.getX() + 0.5, centro.getY(), centro.getZ() + 0.5);
        jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.teleportado_ao_centro_da_regiao_em", nomeRegiao));
        return 1;
    }

    private static String getNomeExibicao(ServerPlayer jogador) {
        String nick = PerfilJogador.apelido().apelido(jogador.server, jogador.getUUID());
        if (nick != null && !nick.isEmpty()) return nick;
        return jogador.getName().getString();
    }
}