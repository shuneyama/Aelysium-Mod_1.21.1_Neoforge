package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.holograma.Holograma;
import net.aelysium.aelysiummod.holograma.HologramaTicker;
import net.aelysium.aelysiummod.holograma.LinhaHolograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class HologramaCommand {

    private static final int ITENS_POR_PAGINA = 8;

    private static final SuggestionProvider<CommandSourceStack> SUGESTAO_HOLOGRAMAS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    GerenciadorHologramas.getInstance().getNomesHologramas(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("holograma")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("criar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .executes(ctx -> criarHolograma(ctx, null))
                                .then(Commands.argument("texto", StringArgumentType.greedyString())
                                        .executes(ctx -> criarHolograma(ctx,
                                                StringArgumentType.getString(ctx, "texto")))
                                )
                        )
                )

                .then(Commands.literal("deletar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .executes(HologramaCommand::deletarHolograma)
                        )
                )

                .then(Commands.literal("lista")
                        .executes(ctx -> listarHologramas(ctx, 1))
                        .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                .executes(ctx -> listarHologramas(ctx,
                                        IntegerArgumentType.getInteger(ctx, "pagina")))
                        )
                )

                .then(Commands.literal("info")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .executes(HologramaCommand::infoHolograma)
                        )
                )

                .then(Commands.literal("mover")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(HologramaCommand::moverHolograma)
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("moveraqui")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .executes(HologramaCommand::moverAqui)
                        )
                )

                .then(Commands.literal("teleportar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .executes(HologramaCommand::teleportar)
                        )
                )

                .then(Commands.literal("renomear")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("novoNome", StringArgumentType.word())
                                        .executes(HologramaCommand::renomear)
                                )
                        )
                )

                .then(Commands.literal("clonar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("novoNome", StringArgumentType.word())
                                        .executes(HologramaCommand::clonar)
                                )
                        )
                )

                .then(Commands.literal("ativar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .executes(ctx -> setAtivo(ctx, true))
                        )
                )

                .then(Commands.literal("desativar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .executes(ctx -> setAtivo(ctx, false))
                        )
                )

                .then(Commands.literal("range")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("blocos", DoubleArgumentType.doubleArg(1, 256))
                                        .executes(HologramaCommand::setRange)
                                )
                        )
                )

                .then(Commands.literal("intervalo")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 200))
                                        .executes(HologramaCommand::setIntervalo)
                                )
                        )
                )

                .then(Commands.literal("alinhar")
                        .then(Commands.argument("nome", StringArgumentType.word())
                                .suggests(SUGESTAO_HOLOGRAMAS)
                                .then(Commands.argument("outroNome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("eixo", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        List.of("x", "y", "z", "xz"), builder))
                                                .executes(HologramaCommand::alinhar)
                                        )
                                )
                        )
                )

                .then(Commands.literal("linha")

                        .then(Commands.literal("adicionar")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                .executes(HologramaCommand::linhaAdicionar)
                                        )
                                )
                        )

                        .then(Commands.literal("inserir")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                        .executes(HologramaCommand::linhaInserir)
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("remover")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .executes(HologramaCommand::linhaRemover)
                                        )
                                )
                        )

                        .then(Commands.literal("editar")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("texto", StringArgumentType.greedyString())
                                                        .executes(HologramaCommand::linhaEditar)
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("trocar")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice1", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("indice2", IntegerArgumentType.integer(1))
                                                        .executes(HologramaCommand::linhaTrocar)
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("altura")
                                .then(Commands.argument("nome", StringArgumentType.word())
                                        .suggests(SUGESTAO_HOLOGRAMAS)
                                        .then(Commands.argument("indice", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("altura", DoubleArgumentType.doubleArg(0.0, 10.0))
                                                        .executes(HologramaCommand::linhaAltura)
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("ajuda")
                        .executes(HologramaCommand::ajuda)
                )

                .executes(HologramaCommand::ajuda)
        );
    }

    private static int criarHolograma(CommandContext<CommandSourceStack> ctx, String texto) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        if (GerenciadorHologramas.getInstance().existeHolograma(nome)) {
            source.sendFailure(Component.literal("§cJá existe um holograma com o nome '§e" + nome + "§c'."));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser executado por um jogador."));
            return 0;
        }

        String mundo = player.level().dimension().location().toString();
        Holograma holo = new Holograma(nome, mundo, player.getX(), player.getY() + 2.0, player.getZ());

        if (texto != null && !texto.isEmpty()) {
            holo.adicionarLinha(texto);
        } else {
            holo.adicionarLinha("§7Holograma: §f" + nome);
        }

        GerenciadorHologramas.getInstance().adicionarHolograma(holo);
        source.sendSuccess(() -> Component.literal("§aHolograma '§e" + nome + "§a' criado com sucesso!"), true);
        return 1;
    }

    private static int deletarHolograma(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        if (!GerenciadorHologramas.getInstance().existeHolograma(nome)) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        HologramaTicker.destruirParaTodos(nome, players);

        GerenciadorHologramas.getInstance().removerHolograma(nome);
        source.sendSuccess(() -> Component.literal("§aHolograma '§e" + nome + "§a' deletado!"), true);
        return 1;
    }

    private static int listarHologramas(CommandContext<CommandSourceStack> ctx, int pagina) {
        CommandSourceStack source = ctx.getSource();
        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        List<String> nomes = gerenciador.getNomesHologramas();
        if (nomes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Nenhum holograma criado."), false);
            return 0;
        }

        int totalPaginas = (int) Math.ceil((double) nomes.size() / ITENS_POR_PAGINA);
        if (pagina > totalPaginas) pagina = totalPaginas;

        int inicio = (pagina - 1) * ITENS_POR_PAGINA;
        int fim = Math.min(inicio + ITENS_POR_PAGINA, nomes.size());

        final int paginaFinal = pagina;
        source.sendSuccess(() -> Component.literal(
                "§6§l═══ Hologramas §7(Página " + paginaFinal + "/" + totalPaginas + ") §6§l═══"), false);

        for (int i = inicio; i < fim; i++) {
            String n = nomes.get(i);
            Holograma h = gerenciador.getHolograma(n);
            if (h == null) continue;

            String status = h.isAtivo() ? "§a●" : "§c●";
            String pos = String.format("%.0f, %.0f, %.0f", h.getX(), h.getY(), h.getZ());
            final String linha = status + " §e" + h.getNome()
                    + " §7[" + pos + "] §8(" + h.getQuantidadeLinhas() + " linhas)";
            source.sendSuccess(() -> Component.literal(linha), false);
        }

        source.sendSuccess(() -> Component.literal("§7Total: §a" + nomes.size() + " hologramas"), false);
        return nomes.size();
    }

    private static int infoHolograma(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6§l═══ Holograma: §e" + holo.getNome() + " §6§l═══"), false);
        source.sendSuccess(() -> Component.literal("§7Status: " + (holo.isAtivo() ? "§aAtivo" : "§cDesativado")), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "§7Posição: §f%.2f, %.2f, %.2f §7(%s)", holo.getX(), holo.getY(), holo.getZ(), holo.getMundo())), false);
        source.sendSuccess(() -> Component.literal("§7Range: §f" + holo.getRangeVisibilidade() + " blocos"), false);
        source.sendSuccess(() -> Component.literal("§7Intervalo: §f" + holo.getIntervaloAtualizacao() + " ticks"), false);
        source.sendSuccess(() -> Component.literal("§7Linhas: §f" + holo.getQuantidadeLinhas()), false);

        for (int i = 0; i < holo.getQuantidadeLinhas(); i++) {
            LinhaHolograma linha = holo.getLinha(i);
            final int index = i + 1;
            source.sendSuccess(() -> Component.literal(
                    "  §7#" + index + " §f" + linha.getConteudo()
                            + " §8(offset: " + String.format("%.2f", linha.getOffsetY()) + ")"
            ), false);
        }

        return 1;
    }

    private static int moverHolograma(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        double x = DoubleArgumentType.getDouble(ctx, "x");
        double y = DoubleArgumentType.getDouble(ctx, "y");
        double z = DoubleArgumentType.getDouble(ctx, "z");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        holo.setPosicao(x, y, z);
        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(String.format(
                "§aHolograma '§e%s§a' movido para §f%.2f, %.2f, %.2f§a.", nome, x, y, z)), true);
        return 1;
    }

    private static int moverAqui(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser executado por um jogador."));
            return 0;
        }

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        String mundo = player.level().dimension().location().toString();
        holo.setPosicao(mundo, player.getX(), player.getY() + 2.0, player.getZ());
        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal("§aHolograma '§e" + nome + "§a' movido para sua posição!"), true);
        return 1;
    }

    private static int teleportar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§cEsse comando só pode ser executado por um jogador."));
            return 0;
        }

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        player.teleportTo(holo.getX(), holo.getY(), holo.getZ());
        source.sendSuccess(() -> Component.literal("§aTeleportado para '§e" + nome + "§a'."), true);
        return 1;
    }

    private static int renomear(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String novoNome = StringArgumentType.getString(ctx, "novoNome");

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        if (!gerenciador.existeHolograma(nome)) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        if (gerenciador.existeHolograma(novoNome)) {
            source.sendFailure(Component.literal("§cJá existe um holograma com o nome '§e" + novoNome + "§c'."));
            return 0;
        }

        Holograma holo = gerenciador.getHolograma(nome);

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        HologramaTicker.destruirParaTodos(nome, players);

        gerenciador.removerHolograma(nome);
        holo.setNome(novoNome);
        gerenciador.adicionarHolograma(holo);

        source.sendSuccess(() -> Component.literal(
                "§aHolograma '§e" + nome + "§a' renomeado para '§e" + novoNome + "§a'."), true);
        return 1;
    }

    private static int clonar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String novoNome = StringArgumentType.getString(ctx, "novoNome");

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        Holograma original = gerenciador.getHolograma(nome);
        if (original == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        if (gerenciador.existeHolograma(novoNome)) {
            source.sendFailure(Component.literal("§cJá existe um holograma com o nome '§e" + novoNome + "§c'."));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        double x, y, z;
        String mundo;
        if (player != null) {
            x = player.getX();
            y = player.getY() + 2.0;
            z = player.getZ();
            mundo = player.level().dimension().location().toString();
        } else {
            x = original.getX();
            y = original.getY();
            z = original.getZ();
            mundo = original.getMundo();
        }

        Holograma clone = new Holograma(novoNome, mundo, x, y, z);
        clone.setAtivo(original.isAtivo());
        clone.setRangeVisibilidade(original.getRangeVisibilidade());
        clone.setIntervaloAtualizacao(original.getIntervaloAtualizacao());

        for (LinhaHolograma linha : original.getLinhas()) {
            clone.getLinhas().add(new LinhaHolograma(linha.getConteudo(), linha.getOffsetY()));
        }

        gerenciador.adicionarHolograma(clone);
        source.sendSuccess(() -> Component.literal(
                "§aHolograma '§e" + nome + "§a' clonado como '§e" + novoNome + "§a'."), true);
        return 1;
    }

    private static int setAtivo(CommandContext<CommandSourceStack> ctx, boolean ativo) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        holo.setAtivo(ativo);
        GerenciadorHologramas.getInstance().salvarHologramas();

        if (!ativo) {
            List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
            HologramaTicker.destruirParaTodos(nome, players);
        }

        String status = ativo ? "§aativado" : "§cdesativado";
        source.sendSuccess(() -> Component.literal("§aHolograma '§e" + nome + "§a' " + status + "§a."), true);
        return 1;
    }

    private static int setRange(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        double range = DoubleArgumentType.getDouble(ctx, "blocos");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        holo.setRangeVisibilidade(range);
        GerenciadorHologramas.getInstance().salvarHologramas();

        source.sendSuccess(() -> Component.literal(
                "§aRange de '§e" + nome + "§a' definido para §f" + range + " blocos§a."), true);
        return 1;
    }

    private static int setIntervalo(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        holo.setIntervaloAtualizacao(ticks);
        GerenciadorHologramas.getInstance().salvarHologramas();

        source.sendSuccess(() -> Component.literal(
                "§aIntervalo de '§e" + nome + "§a' definido para §f" + ticks + " ticks§a."), true);
        return 1;
    }

    private static int alinhar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String outroNome = StringArgumentType.getString(ctx, "outroNome");
        String eixo = StringArgumentType.getString(ctx, "eixo").toLowerCase();

        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        Holograma holo = gerenciador.getHolograma(nome);
        Holograma outro = gerenciador.getHolograma(outroNome);

        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }
        if (outro == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + outroNome + "§c' não encontrado."));
            return 0;
        }

        double x = holo.getX(), y = holo.getY(), z = holo.getZ();

        switch (eixo) {
            case "x" -> x = outro.getX();
            case "y" -> y = outro.getY();
            case "z" -> z = outro.getZ();
            case "xz" -> { x = outro.getX(); z = outro.getZ(); }
            default -> {
                source.sendFailure(Component.literal("§cEixo inválido. Use: §ex§c, §ey§c, §ez§c ou §exz§c."));
                return 0;
            }
        }

        holo.setPosicao(x, y, z);
        gerenciador.salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aHolograma '§e" + nome + "§a' alinhado com '§e" + outroNome + "§a' no eixo §f" + eixo + "§a."), true);
        return 1;
    }

    private static int linhaAdicionar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String texto = StringArgumentType.getString(ctx, "texto");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        holo.adicionarLinha(texto);
        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aLinha adicionada ao holograma '§e" + nome + "§a'. Total: §f"
                        + holo.getQuantidadeLinhas() + " linhas§a."), true);
        return 1;
    }

    private static int linhaInserir(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1; // Converte para 0-based
        String texto = StringArgumentType.getString(ctx, "texto");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        holo.inserirLinha(indice, texto);
        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aLinha inserida na posição §f" + (indice + 1) + "§a no holograma '§e" + nome + "§a'."), true);
        return 1;
    }

    private static int linhaRemover(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        if (!holo.removerLinha(indice)) {
            source.sendFailure(Component.literal("§cÍndice inválido. O holograma tem §e"
                    + holo.getQuantidadeLinhas() + "§c linhas."));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aLinha §f" + (indice + 1) + "§a removida do holograma '§e" + nome + "§a'."), true);
        return 1;
    }

    private static int linhaEditar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;
        String texto = StringArgumentType.getString(ctx, "texto");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        if (!holo.editarLinha(indice, texto)) {
            source.sendFailure(Component.literal("§cÍndice inválido. O holograma tem §e"
                    + holo.getQuantidadeLinhas() + "§c linhas."));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aLinha §f" + (indice + 1) + "§a do holograma '§e" + nome + "§a' editada."), true);
        return 1;
    }

    private static int linhaTrocar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice1 = IntegerArgumentType.getInteger(ctx, "indice1") - 1;
        int indice2 = IntegerArgumentType.getInteger(ctx, "indice2") - 1;

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        if (!holo.trocarLinhas(indice1, indice2)) {
            source.sendFailure(Component.literal("§cÍndices inválidos. O holograma tem §e"
                    + holo.getQuantidadeLinhas() + "§c linhas."));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aLinhas §f" + (indice1 + 1) + "§a e §f" + (indice2 + 1)
                        + "§a trocadas no holograma '§e" + nome + "§a'."), true);
        return 1;
    }

    private static int linhaAltura(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;
        double altura = DoubleArgumentType.getDouble(ctx, "altura");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.literal("§cHolograma '§e" + nome + "§c' não encontrado."));
            return 0;
        }

        if (!holo.definirAlturaLinha(indice, altura)) {
            source.sendFailure(Component.literal("§cÍndice inválido. O holograma tem §e"
                    + holo.getQuantidadeLinhas() + "§c linhas."));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.literal(
                "§aAltura da linha §f" + (indice + 1) + "§a definida para §f"
                        + String.format("%.2f", altura) + "§a."), true);
        return 1;
    }

    private static int ajuda(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("§6§l═══ Holograma - Ajuda ═══"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma criar <nome> [texto] §7- Cria um holograma"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma deletar <nome> §7- Deleta um holograma"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma lista [pagina] §7- Lista hologramas"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma info <nome> §7- Informações detalhadas"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma mover <nome> <x> <y> <z> §7- Move o holograma"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma moveraqui <nome> §7- Move para você"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma teleportar <nome> §7- TP até o holograma"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma renomear <nome> <novo> §7- Renomeia"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma clonar <nome> <novo> §7- Clona o holograma"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma ativar/desativar <nome> §7- Liga/desliga"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma range <nome> <blocos> §7- Range de visibilidade"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma alinhar <nome> <outro> <eixo> §7- Alinha hologramas"), false);
        source.sendSuccess(() -> Component.literal("§6§l--- Linhas ---"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma linha adicionar <nome> <texto>"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma linha inserir <nome> <indice> <texto>"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma linha remover <nome> <indice>"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma linha editar <nome> <indice> <texto>"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma linha trocar <nome> <i1> <i2>"), false);
        source.sendSuccess(() -> Component.literal("§e/holograma linha altura <nome> <indice> <valor>"), false);
        return 1;
    }

    private static void atualizarVisual(Holograma holo, CommandSourceStack source) {
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        HologramaTicker.atualizarParaTodos(holo, players);
    }
}