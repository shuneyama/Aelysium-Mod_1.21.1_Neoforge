package net.aelysium.aelysiummod.sistemas.admin.listabranca.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.admin.listabranca.GerenciadorListaBrancaOffline;
import net.aelysium.aelysiummod.sistemas.admin.listabranca.GerenciadorListaBrancaOffline.Entrada;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public final class ComandoListaBranca {
    private static final int PERMISSAO = 2;
    private static final String BASE = "mensagem.aelysiummod.whitelist.";

    private ComandoListaBranca() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("whitelist")
                .requires(cs -> cs.hasPermission(PERMISSAO))
                .then(Commands.literal("adicionar").then(nome(false).executes(ComandoListaBranca::adicionar)))
                .then(Commands.literal("remover").then(nome(true).executes(ComandoListaBranca::remover)))
                .then(Commands.literal("procurar").then(nome(true).executes(ComandoListaBranca::procurar)))
                .then(Commands.literal("listar").executes(ComandoListaBranca::listar));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> nome(boolean sugerir) {
        var argumento = Commands.argument("nome", StringArgumentType.word());
        if (sugerir) {
            argumento.suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(GerenciadorListaBrancaOffline.nomes(), construtor));
        }
        return argumento;
    }

    private static MutableComponent msg(String chave, Object... args) {
        return Component.translatableEscape(BASE + chave, args);
    }

    private static boolean validar(CommandContext<CommandSourceStack> ctx, String nome) {
        if (GerenciadorListaBrancaOffline.nomeValido(nome)) return true;
        ctx.getSource().sendFailure(msg("nome_invalido", nome));
        return false;
    }

    private static int adicionar(CommandContext<CommandSourceStack> ctx) {
        String nome = StringArgumentType.getString(ctx, "nome");
        if (!validar(ctx, nome)) return 0;
        var resultado = GerenciadorListaBrancaOffline.adicionar(nome);
        switch (resultado.tipo()) {
            case ADICIONADO -> {
                ctx.getSource().sendSuccess(() -> msg("adicionado", nome, resultado.entrada().uuid()), true);
                return 1;
            }
            case JA_EXISTE -> {
                ctx.getSource().sendFailure(msg("ja_existe", resultado.entrada().nome(), resultado.entrada().uuid()));
                return 0;
            }
            default -> {
                ctx.getSource().sendFailure(msg("erro"));
                return 0;
            }
        }
    }

    private static int remover(CommandContext<CommandSourceStack> ctx) {
        String nome = StringArgumentType.getString(ctx, "nome");
        Entrada removida = GerenciadorListaBrancaOffline.remover(nome);
        if (removida != null) {
            ctx.getSource().sendSuccess(() -> msg("removido", removida.nome()), true);
            return 1;
        }
        List<Entrada> parecidas = GerenciadorListaBrancaOffline.procurar(nome);
        if (parecidas.isEmpty()) {
            ctx.getSource().sendFailure(msg("nao_encontrado", nome));
        } else {
            ctx.getSource().sendFailure(msg("nao_encontrado_grafia", nome, String.join(", ", parecidas.stream().map(Entrada::nome).toList())));
        }
        return 0;
    }

    private static MutableComponent linha(Entrada e) {
        MutableComponent linha = msg("linha", e.nome(), e.uuid());
        if (!e.consistente()) linha.append(msg("uuid_inconsistente", e.uuidEsperado()));
        return linha;
    }

    private static int procurar(CommandContext<CommandSourceStack> ctx) {
        String nome = StringArgumentType.getString(ctx, "nome");
        List<Entrada> encontradas = GerenciadorListaBrancaOffline.procurar(nome);
        if (encontradas.isEmpty()) {
            ctx.getSource().sendFailure(msg("nao_encontrado", nome));
            return 0;
        }
        for (Entrada e : encontradas) ctx.getSource().sendSuccess(() -> linha(e), false);
        return encontradas.size();
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        List<Entrada> todas = GerenciadorListaBrancaOffline.entradas();
        if (todas.isEmpty()) {
            ctx.getSource().sendSuccess(() -> msg("lista_vazia"), false);
            return 0;
        }
        ctx.getSource().sendSuccess(() -> msg("listar_titulo", todas.size()), false);
        for (Entrada e : todas) ctx.getSource().sendSuccess(() -> linha(e), false);
        return todas.size();
    }
}
