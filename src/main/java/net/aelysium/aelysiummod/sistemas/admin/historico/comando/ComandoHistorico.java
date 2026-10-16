package net.aelysium.aelysiummod.sistemas.admin.historico.comando;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosAnotacao;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosHistorico;

public class ComandoHistorico {

    private static final int POR_PAGINA = 8;
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FMT_EXPORTAR = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("historico")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("visualizar")
                        .executes(ctx -> mostrarLista(ctx.getSource(), 1))
                        .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                .executes(ctx -> mostrarLista(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "pagina")))
                        )
                )
                .then(Commands.literal("exportar")
                        .executes(ctx -> exportar(ctx.getSource()))
                );
    }

    public static void registrarDebug(RegisterCommandsEvent event) {
        SuggestionProvider<CommandSourceStack> suggestions = (ctx, builder) -> {
            for (String nome : DadosHistorico.getNomesTodos()) builder.suggest(nome);
            return builder.buildFuture();
        };

        event.getDispatcher().register(
                Commands.literal("debug")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("historico")
                                .then(Commands.literal("sessoes")
                                        .then(Commands.argument("jogador", StringArgumentType.string())
                                                .suggests(suggestions)
                                                .executes(ctx -> mostrarSessoes(ctx.getSource(), StringArgumentType.getString(ctx, "jogador"), 1))
                                                .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> mostrarSessoes(ctx.getSource(), StringArgumentType.getString(ctx, "jogador"), IntegerArgumentType.getInteger(ctx, "pagina")))
                                                )
                                        )
                                )
                                .then(Commands.literal("medias")
                                        .then(Commands.argument("jogador", StringArgumentType.string())
                                                .suggests(suggestions)
                                                .executes(ctx -> mostrarMedias(ctx.getSource(), StringArgumentType.getString(ctx, "jogador")))
                                        )
                                )
                                .then(Commands.literal("anotacoes")
                                        .then(Commands.argument("jogador", StringArgumentType.string())
                                                .suggests(suggestions)
                                                .executes(ctx -> mostrarAnotacoes(ctx.getSource(), StringArgumentType.getString(ctx, "jogador"), 1))
                                                .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> mostrarAnotacoes(ctx.getSource(), StringArgumentType.getString(ctx, "jogador"), IntegerArgumentType.getInteger(ctx, "pagina")))
                                                )
                                        )
                                )
                        )
        );
    }

    private static int mostrarLista(CommandSourceStack source, int pagina) {
        List<UUID> jogadores = DadosHistorico.getJogadoresOrdenadosPorHoras();
        int total = jogadores.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / POR_PAGINA));
        int paginaAtual = Math.max(1, Math.min(pagina, totalPaginas));
        int inicio = (paginaAtual - 1) * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, total);

        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.historico_de_jogadores", paginaAtual, totalPaginas));
        source.sendSystemMessage(separador());

        if (total == 0) {
            source.sendSystemMessage(Component.translatable("mensagem.aelysiummod.historico.nenhum_jogador_registrado"));
        }

        for (int i = inicio; i < fim; i++) {
            UUID uuid = jogadores.get(i);
            String nome = DadosHistorico.getNome(uuid);
            double horas = DadosHistorico.getTotalHoras(uuid);
            int sessoes = DadosHistorico.getTotalSessoes(uuid);

            source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.sess", (i + 1), nome, formatarHoras(horas), sessoes));

            MutableComponent botoes = Component.literal("     ");
            botoes.append(botaoClicavel(Component.translatable("mensagem.aelysiummod.historico.botao_sessoes"),
                    "/debug historico sessoes " + nome,
                    Component.translatableEscape("mensagem.aelysiummod.historico.ver_sessoes_de", nome)));
            botoes.append(Component.literal("  "));
            botoes.append(botaoClicavel(Component.translatable("mensagem.aelysiummod.historico.botao_medias"),
                    "/debug historico medias " + nome,
                    Component.translatableEscape("mensagem.aelysiummod.historico.ver_medias_de", nome)));
            botoes.append(Component.literal("  "));
            botoes.append(botaoClicavel(Component.translatable("mensagem.aelysiummod.historico.botao_anotacoes"),
                    "/debug historico anotacoes " + nome,
                    Component.translatableEscape("mensagem.aelysiummod.historico.ver_anotacoes_de", nome)));
            source.sendSystemMessage(botoes);
        }

        source.sendSystemMessage(separador());
        source.sendSystemMessage(navegacao(paginaAtual, totalPaginas, "/aelysium historico visualizar"));
        return 1;
    }

    private static int mostrarSessoes(CommandSourceStack source, String identificador, int pagina) {
        UUID uuid = DadosHistorico.resolverIdentificador(identificador);
        if (uuid == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.jogador_nao_encontrado_no_historico", identificador));
            return 0;
        }

        String nome = DadosHistorico.getNome(uuid);
        List<DadosHistorico.Sessao> sessoes = DadosHistorico.getSessoes(uuid);
        int total = sessoes.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / POR_PAGINA));
        int paginaAtual = Math.max(1, Math.min(pagina, totalPaginas));
        int inicio = (paginaAtual - 1) * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, total);

        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.sessoes_de", nome, paginaAtual, totalPaginas));
        source.sendSystemMessage(separador());

        if (total == 0) {
            source.sendSystemMessage(Component.translatable("mensagem.aelysiummod.historico.nenhuma_sessao_registrada"));
        }

        for (int i = inicio; i < fim; i++) {
            DadosHistorico.Sessao s = sessoes.get(total - 1 - i);
            ZonedDateTime loginZdt = Instant.ofEpochMilli(s.loginMs()).atZone(ZoneId.systemDefault());
            String dataLogin = loginZdt.format(FMT_DATA);
            String horaLogin = loginZdt.format(FMT_HORA);
            Object horaLogout = s.finalizada()
                    ? Instant.ofEpochMilli(s.logoutMs()).atZone(ZoneId.systemDefault()).format(FMT_HORA)
                    : Component.translatable("mensagem.aelysiummod.historico.online");

            source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.login_logout_duracao_ip", dataLogin, horaLogin, horaLogout, formatarHoras(s.duracaoHoras()), s.ip()));
        }

        source.sendSystemMessage(separador());
        source.sendSystemMessage(navegacao(paginaAtual, totalPaginas, "/debug historico sessoes " + identificador));
        return 1;
    }

    private static int mostrarMedias(CommandSourceStack source, String identificador) {
        UUID uuid = DadosHistorico.resolverIdentificador(identificador);
        if (uuid == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.jogador_nao_encontrado_no_historico", identificador));
            return 0;
        }

        String nome = DadosHistorico.getNome(uuid);
        double totalHoras = DadosHistorico.getTotalHoras(uuid);
        int totalSessoes = DadosHistorico.getTotalSessoes(uuid);
        double mediaDiaria = DadosHistorico.getMediaDiaria(uuid);
        double mediaSemanal = DadosHistorico.getMediaSemanal(uuid);

        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.medias_de", nome));
        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.total_jogado", formatarHoras(totalHoras)));
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.total_de_sessoes", totalSessoes));
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.media_diaria", formatarHoras(mediaDiaria)));
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.media_semanal", formatarHoras(mediaSemanal)));
        source.sendSystemMessage(separador());
        return 1;
    }

    private static int exportar(CommandSourceStack source) {
        if (DadosHistorico.getPastaData() == null) {
            source.sendFailure(Component.translatable("mensagem.aelysiummod.historico.historico_nao_inicializado"));
            return 0;
        }

        String carimbo = LocalDateTime.now().format(FMT_EXPORTAR);
        Path pasta = DadosHistorico.getPastaData().resolve("exportacoes");

        try {
            Files.createDirectories(pasta);
            exportarJogadores(pasta, carimbo);
            exportarSessoes(pasta, carimbo);
            exportarMedias(pasta, carimbo);
            exportarAnotacoes(pasta, carimbo);
            source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.historico.aelysium_exportacao_concluida_arquivos", pasta), false);
        } catch (IOException e) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.erro_ao_exportar", e.getMessage()));
            return 0;
        }
        return 1;
    }

    private static void exportarJogadores(Path pasta, String carimbo) throws IOException {
        List<UUID> jogadores = DadosHistorico.getJogadoresOrdenadosPorHoras();
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("Posição;Nome;UUID;Total Horas;Total Minutos;Total Sessões\n");
        int pos = 1;
        for (UUID uuid : jogadores) {
            double horas = DadosHistorico.getTotalHoras(uuid);
            sb.append(pos++).append(";")
                    .append(DadosHistorico.getNome(uuid)).append(";")
                    .append(uuid).append(";")
                    .append(String.format("%.2f", horas)).append(";")
                    .append((long)(horas * 60)).append(";")
                    .append(DadosHistorico.getTotalSessoes(uuid)).append("\n");
        }
        Files.writeString(pasta.resolve("historico_jogadores_" + carimbo + ".csv"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static void exportarSessoes(Path pasta, String carimbo) throws IOException {
        List<UUID> jogadores = DadosHistorico.getJogadoresOrdenadosPorHoras();
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("Nome;UUID;Data Login;Hora Login;Data Logout;Hora Logout;Duração (h);Duração (min);IP\n");
        for (UUID uuid : jogadores) {
            String nome = DadosHistorico.getNome(uuid);
            for (DadosHistorico.Sessao s : DadosHistorico.getSessoes(uuid)) {
                ZonedDateTime login = Instant.ofEpochMilli(s.loginMs()).atZone(ZoneId.systemDefault());
                String dataLogin = login.format(FMT_DATA);
                String horaLogin = login.format(FMT_HORA);
                String dataLogout = "";
                String horaLogout = "";
                if (s.finalizada()) {
                    ZonedDateTime logout = Instant.ofEpochMilli(s.logoutMs()).atZone(ZoneId.systemDefault());
                    dataLogout = logout.format(FMT_DATA);
                    horaLogout = logout.format(FMT_HORA);
                }
                sb.append(nome).append(";")
                        .append(uuid).append(";")
                        .append(dataLogin).append(";")
                        .append(horaLogin).append(";")
                        .append(dataLogout).append(";")
                        .append(horaLogout).append(";")
                        .append(String.format("%.2f", s.duracaoHoras())).append(";")
                        .append(s.duracaoMs() / 60_000).append(";")
                        .append(s.ip()).append("\n");
            }
        }
        Files.writeString(pasta.resolve("historico_sessoes_" + carimbo + ".csv"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static void exportarMedias(Path pasta, String carimbo) throws IOException {
        List<UUID> jogadores = DadosHistorico.getJogadoresOrdenadosPorHoras();
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("Posição;Nome;UUID;Média Diária (h);Média Semanal (h);Total Horas;Total Sessões\n");
        int pos = 1;
        for (UUID uuid : jogadores) {
            sb.append(pos++).append(";")
                    .append(DadosHistorico.getNome(uuid)).append(";")
                    .append(uuid).append(";")
                    .append(String.format("%.2f", DadosHistorico.getMediaDiaria(uuid))).append(";")
                    .append(String.format("%.2f", DadosHistorico.getMediaSemanal(uuid))).append(";")
                    .append(String.format("%.2f", DadosHistorico.getTotalHoras(uuid))).append(";")
                    .append(DadosHistorico.getTotalSessoes(uuid)).append("\n");
        }
        Files.writeString(pasta.resolve("historico_medias_" + carimbo + ".csv"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static int mostrarAnotacoes(CommandSourceStack source, String identificador, int pagina) {
        UUID uuid = DadosHistorico.resolverIdentificador(identificador);
        if (uuid == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.historico.jogador_nao_encontrado_no_historico", identificador));
            return 0;
        }

        String nome = DadosHistorico.getNome(uuid);
        List<DadosAnotacao.Anotacao> anotacoes = DadosAnotacao.getAnotacoes(uuid);
        int total = anotacoes.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / POR_PAGINA));
        int paginaAtual = Math.max(1, Math.min(pagina, totalPaginas));
        int inicio = (paginaAtual - 1) * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, total);

        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.anotacoes_de", nome, paginaAtual, totalPaginas));
        source.sendSystemMessage(separador());

        if (total == 0) {
            source.sendSystemMessage(Component.translatable("mensagem.aelysiummod.historico.nenhuma_anotacao_registrada"));
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (int i = inicio; i < fim; i++) {
            DadosAnotacao.Anotacao a = anotacoes.get(total - 1 - i);
            String tipoDisplay = Character.toUpperCase(a.tipo().charAt(0)) + a.tipo().substring(1);
            String horario = Instant.ofEpochMilli(a.timestamp()).atZone(ZoneId.systemDefault()).format(fmt);
            source.sendSystemMessage(Component.literal(
                    "  §7#" + (total - i) + "  §d" + tipoDisplay + "  §7" + horario
            ));
            source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.historico.resp_comentario", a.responsavelNome(), a.comentario()));
        }

        source.sendSystemMessage(separador());
        source.sendSystemMessage(navegacao(paginaAtual, totalPaginas, "/debug historico anotacoes " + identificador));
        return 1;
    }

    private static void exportarAnotacoes(Path pasta, String carimbo) throws IOException {
        Map<UUID, List<DadosAnotacao.Anotacao>> todas = DadosAnotacao.getTodas();
        List<UUID> jogadores = DadosHistorico.getJogadoresOrdenadosPorHoras();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("Nome;UUID;Tipo;Horário;Responsável;Comentário\n");
        for (UUID uuid : jogadores) {
            String nome = DadosHistorico.getNome(uuid);
            List<DadosAnotacao.Anotacao> lista = todas.getOrDefault(uuid, List.of());
            for (DadosAnotacao.Anotacao a : lista) {
                String horario = Instant.ofEpochMilli(a.timestamp()).atZone(ZoneId.systemDefault()).format(fmt);
                String tipoDisplay = Character.toUpperCase(a.tipo().charAt(0)) + a.tipo().substring(1);
                sb.append(nome).append(";")
                        .append(uuid).append(";")
                        .append(tipoDisplay).append(";")
                        .append(horario).append(";")
                        .append(a.responsavelNome()).append(";")
                        .append(a.comentario().replace(";", ",")).append("\n");
            }
        }
        Files.writeString(pasta.resolve("historico_anotacoes_" + carimbo + ".csv"), sb.toString(), StandardCharsets.UTF_8);
    }

    private static String formatarHoras(double horas) {
        long totalMin = (long)(horas * 60);
        long h = totalMin / 60;
        long m = totalMin % 60;
        return h + "h " + m + "m";
    }

    private static MutableComponent separador() {
        return Component.literal("§8§m──────────────────────────────§r");
    }

    private static MutableComponent botaoClicavel(Component texto, String comando, Component tooltip) {
        return texto.copy().withStyle(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }

    private static MutableComponent navegacao(int paginaAtual, int totalPaginas, String baseCmd) {
        MutableComponent nav = Component.literal("  ");
        if (paginaAtual > 1) {
            nav.append(botaoClicavel(Component.literal("§7[§f◀§7]"), baseCmd + " " + (paginaAtual - 1),
                    Component.translatable("mensagem.aelysiummod.comum.pagina_anterior")));
            nav.append(Component.literal("  "));
        }
        nav.append(Component.translatableEscape("mensagem.aelysiummod.banimentos.pagina", paginaAtual, totalPaginas));
        if (paginaAtual < totalPaginas) {
            nav.append(Component.literal("  "));
            nav.append(botaoClicavel(Component.literal("§7[§f▶§7]"), baseCmd + " " + (paginaAtual + 1),
                    Component.translatable("mensagem.aelysiummod.comum.proxima_pagina")));
        }
        return nav;
    }
}
