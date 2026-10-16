package net.aelysium.aelysiummod.sistemas.midia.holograma.comando;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.holograma.Holograma;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class AcoesHologramaLinhas {

    static int linhaAdicionar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        String texto = StringArgumentType.getString(ctx, "texto");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        holo.adicionarLinha(texto);
        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.linha_adicionada_ao_holograma_total", nome, holo.getQuantidadeLinhas()), true);
        return 1;
    }

    static int linhaInserir(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;
        String texto = StringArgumentType.getString(ctx, "texto");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        holo.inserirLinha(indice, texto);
        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.linha_inserida_na_posicao_no_holograma", (indice + 1), nome), true);
        return 1;
    }

    static int linhaRemover(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        if (!holo.removerLinha(indice)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.indice_invalido_o_holograma_tem_linhas", holo.getQuantidadeLinhas()));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.linha_removida_do_holograma", (indice + 1), nome), true);
        return 1;
    }

    static int linhaEditar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;
        String texto = StringArgumentType.getString(ctx, "texto");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        if (!holo.editarLinha(indice, texto)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.indice_invalido_o_holograma_tem_linhas", holo.getQuantidadeLinhas()));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.linha_do_holograma_editada", (indice + 1), nome), true);
        return 1;
    }

    static int linhaTrocar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice1 = IntegerArgumentType.getInteger(ctx, "indice1") - 1;
        int indice2 = IntegerArgumentType.getInteger(ctx, "indice2") - 1;

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        if (!holo.trocarLinhas(indice1, indice2)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.indices_invalidos_o_holograma_tem", holo.getQuantidadeLinhas()));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.linhas_e_trocadas_no_holograma", (indice1 + 1), (indice2 + 1), nome), true);
        return 1;
    }

    static int linhaAltura(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String nome = StringArgumentType.getString(ctx, "nome");
        int indice = IntegerArgumentType.getInteger(ctx, "indice") - 1;
        double altura = DoubleArgumentType.getDouble(ctx, "altura");

        Holograma holo = GerenciadorHologramas.getInstance().getHolograma(nome);
        if (holo == null) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.holograma_nao_encontrado", nome));
            return 0;
        }

        if (!holo.definirAlturaLinha(indice, altura)) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.holograma.indice_invalido_o_holograma_tem_linhas", holo.getQuantidadeLinhas()));
            return 0;
        }

        GerenciadorHologramas.getInstance().salvarHologramas();
        UtilitariosComandoHolograma.atualizarVisual(holo, source);

        source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.holograma.altura_da_linha_definida_para", (indice + 1), String.format("%.2f", altura)), true);
        return 1;
    }
}
