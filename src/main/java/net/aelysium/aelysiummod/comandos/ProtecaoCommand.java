package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.protecao.network.PacoteAbrirTela;
import net.aelysium.aelysiummod.protecao.network.PacoteAbrirTelaEdicao;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.protecao.regiao.SelecaoManager;
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
import java.util.UUID;

public class ProtecaoCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES_REGIOES = (context, builder) -> {
        List<String> nomes = GerenciadorRegioes.getInstance().getNomesRegioes();
        return SharedSuggestionProvider.suggest(nomes, builder);
    };

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("aelysium")
                        .then(Commands.literal("proteção")
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
                        )
        );
    }

    private static int criar(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer jogador)) {
            source.sendFailure(Component.literal("Apenas jogadores podem usar este comando!"));
            return 0;
        }

        if (!SelecaoManager.temSelecaoCompleta(jogador.getUUID())) {
            jogador.sendSystemMessage(Component.literal("§cVocê precisa selecionar duas posições com a Varinha de Proteção primeiro!"));
            return 0;
        }

        BlockPos min = SelecaoManager.getMinimo(jogador.getUUID());
        BlockPos max = SelecaoManager.getMaximo(jogador.getUUID());

        PacketDistributor.sendToPlayer(jogador, new PacoteAbrirTela(min, max));
        return 1;
    }

    private static int editar(CommandSourceStack source, String nomeRegiao) {
        if (!(source.getEntity() instanceof ServerPlayer jogador)) {
            source.sendFailure(Component.literal("Apenas jogadores podem usar este comando!"));
            return 0;
        }

        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (!gerenciador.existeRegiao(nomeRegiao)) {
            source.sendFailure(Component.literal("§cRegião '" + nomeRegiao + "' não existe!"));
            return 0;
        }

        PacketDistributor.sendToPlayer(jogador, PacoteAbrirTelaEdicao.deRegiao(gerenciador.getRegiao(nomeRegiao)));
        return 1;
    }

    private static int listar(CommandSourceStack source) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        List<String> nomesRegioes = gerenciador.getNomesRegioes();

        if (nomesRegioes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§eNenhuma região protegida foi criada ainda."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Regiões Protegidas ==="), false);

        for (String nome : nomesRegioes) {
            Regiao regiao = gerenciador.getRegiao(nome);
            if (regiao == null) continue;

            BlockPos centro = regiao.getPosicaoCentral();

            MutableComponent mensagem = Component.literal("§e" + nome + " §7| ");

            MutableComponent botaoFlags = Component.literal("§a[Ver Flags]")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/debug _verflags " + nome))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§7Clique para ver as flags desta região")))
                            .withColor(ChatFormatting.GREEN));

            MutableComponent botaoDonos = Component.literal("§b[Ver Donos]")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/debug _verdonos " + nome))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§7Clique para ver os donos desta região")))
                            .withColor(ChatFormatting.AQUA));

            MutableComponent botaoTeleporte = Component.literal("§d[Teleportar]")
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/debug _tp " + nome))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§7Clique para teleportar ao centro\n§7Coordenadas: " +
                                            centro.getX() + ", " + centro.getY() + ", " + centro.getZ())))
                            .withColor(ChatFormatting.LIGHT_PURPLE));

            mensagem.append(botaoFlags).append(" ").append(botaoDonos).append(" ").append(botaoTeleporte);
            source.sendSuccess(() -> mensagem, false);
        }

        return 1;
    }

    private static int remover(CommandSourceStack source, String nomeRegiao) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (!gerenciador.existeRegiao(nomeRegiao)) {
            source.sendFailure(Component.literal("§cRegião '" + nomeRegiao + "' não existe!"));
            return 0;
        }

        gerenciador.removerRegiao(nomeRegiao);
        source.sendSuccess(() -> Component.literal("§aRegião '" + nomeRegiao + "' removida com sucesso!"), true);
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
        if (regiao == null) { source.sendFailure(Component.literal("§cRegião não encontrada!")); return 0; }

        source.sendSuccess(() -> Component.literal("§6=== Flags da Região '" + nomeRegiao + "' ==="), false);
        for (FlagRegiao flag : FlagRegiao.values()) {
            boolean valor = regiao.getFlagValor(flag);
            String status = valor ? "§aAtivada" : "§cDesativada";
            source.sendSuccess(() -> Component.literal(status + " §7- §e" + flag.getNome()), false);
        }
        return 1;
    }

    private static int verDonos(CommandSourceStack source, String nomeRegiao) {
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiao(nomeRegiao);
        if (regiao == null) { source.sendFailure(Component.literal("§cRegião não encontrada!")); return 0; }

        if (regiao.getDonos().isEmpty()) {
            source.sendSuccess(() -> Component.literal("§eA região '" + nomeRegiao + "' não possui donos."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§6=== Donos da Região '" + nomeRegiao + "' ==="), false);
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
            source.sendFailure(Component.literal("Apenas jogadores podem se teleportar!"));
            return 0;
        }

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiao(nomeRegiao);
        if (regiao == null) { source.sendFailure(Component.literal("§cRegião não encontrada!")); return 0; }

        BlockPos centro = regiao.getPosicaoCentral();
        jogador.setGameMode(GameType.SPECTATOR);
        jogador.teleportTo(centro.getX() + 0.5, centro.getY(), centro.getZ() + 0.5);
        jogador.sendSystemMessage(Component.literal("§aTeleportado ao centro da região '" + nomeRegiao + "' em modo espectador!"));
        return 1;
    }

    private static String getNomeExibicao(ServerPlayer jogador) {
        try {
            Class<?> nicknameDataClass = Class.forName("net.aelysium.aelysiummod.nickname.NicknameData");
            Object data = nicknameDataClass.getMethod("get", net.minecraft.server.MinecraftServer.class)
                    .invoke(null, jogador.server);
            Object entry = nicknameDataClass.getMethod("getNickname", UUID.class)
                    .invoke(data, jogador.getUUID());
            if (entry != null) {
                String nick = (String) entry.getClass().getMethod("nick").invoke(entry);
                if (nick != null && !nick.isEmpty()) return nick;
            }
        } catch (Exception ignored) {}
        return jogador.getName().getString();
    }
}