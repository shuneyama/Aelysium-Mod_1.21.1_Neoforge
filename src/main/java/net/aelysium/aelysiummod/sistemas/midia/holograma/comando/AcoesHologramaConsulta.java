package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.holograma.Holograma;
import net.aelysium.aelysiummod.sistemas.midia.holograma.LinhaHolograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AcoesHologramaConsulta {

    private static final int ITENS_POR_PAGINA = 8;

    static int listarHologramas(CommandContext<CommandSourceStack> ctx, int pagina) {
        CommandSourceStack source = ctx.getSource();
        GerenciadorHologramas gerenciador = GerenciadorHologramas.getInstance();

        List<String> nomes = gerenciador.getNomesHologramas();
        if (nomes.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.holograma.nenhum_holograma_criado"), false);
            return 0;
        }

        int totalPaginas = (int) Math.ceil((double) nomes.size() / ITENS_POR_PAGINA);
        if (pagina > totalPaginas) pagina = totalPaginas;

        int inicio = (pagina - 1) * ITENS_POR_PAGINA;
        int fim = Math.min(inicio + ITENS_POR_PAGINA, nomes.size());

        final int paginaFinal = pagina;
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.hologramas_pagina", paginaFinal, totalPaginas), false);

        for (int i = inicio; i < fim; i++) {
            String n = nomes.get(i);
            Holograma h = gerenciador.getHolograma(n);
            if (h == null) continue;

            String status = h.isAtivo() ? "§a●" : "§c●";
            String pos = String.format("%.0f, %.0f, %.0f", h.getX(), h.getY(), h.getZ());
            final Component linha = Component.translatableEscape("mensagem.aelysiummod.holograma.item_lista",
                    status, h.getNome(), pos, h.getQuantidadeLinhas());
            source.sendSuccess(() -> linha, false);
        }

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.total_hologramas", nomes.size()), false);
        return nomes.size();
    }

    static int infoHolograma(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_2", holo.getNome()), false);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.status", Component.translatable(holo.isAtivo() ? "mensagem.aelysiummod.holograma.estado_ativo" : "mensagem.aelysiummod.holograma.estado_desativado")), false);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.posicao", String.format("%.2f", holo.getX()), String.format("%.2f", holo.getY()), String.format("%.2f", holo.getZ()), holo.getMundo()), false);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.range_blocos", holo.getRangeVisibilidade()), false);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.intervalo_ticks", holo.getIntervaloAtualizacao()), false);
        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.linhas", holo.getQuantidadeLinhas()), false);

        for (int i = 0; i < holo.getQuantidadeLinhas(); i++) {
            LinhaHolograma linha = holo.getLinha(i);
            final int index = i + 1;
            source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.offset", index, linha.getConteudo(), String.format("%.2f", linha.getOffsetY())), false);
        }

        return 1;
    }
}
