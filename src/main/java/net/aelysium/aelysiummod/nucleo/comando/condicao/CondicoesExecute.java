package net.aelysium.aelysiummod.nucleo.comando.condicao;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

public final class CondicoesExecute {

    private static final SimpleCommandExceptionType FALHOU =
            new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));

    private CondicoesExecute() {
    }

    @FunctionalInterface
    public interface Teste {
        boolean testar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }

    public record Ramo(CommandNode<CommandSourceStack> execute, boolean esperado) {
        public <T extends ArgumentBuilder<CommandSourceStack, T>> T condicao(T construtor, Teste teste) {
            return construtor
                    .fork(execute, ctx -> teste.testar(ctx) == esperado ? List.of(ctx.getSource()) : List.<CommandSourceStack>of())
                    .executes(ctx -> {
                        if (teste.testar(ctx) != esperado) throw FALHOU.create();
                        ctx.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                        return 1;
                    });
        }
    }

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher,
                                 Function<Ramo, LiteralArgumentBuilder<CommandSourceStack>> fabrica) {
        CommandNode<CommandSourceStack> execute = dispatcher.getRoot().getChild("execute");
        if (execute == null) return;
        registrarRamo(execute, "if", true, fabrica);
        registrarRamo(execute, "unless", false, fabrica);
    }

    private static void registrarRamo(CommandNode<CommandSourceStack> execute, String literal, boolean esperado,
                                      Function<Ramo, LiteralArgumentBuilder<CommandSourceStack>> fabrica) {
        CommandNode<CommandSourceStack> ramo = execute.getChild(literal);
        if (ramo == null) return;
        CommandNode<CommandSourceStack> aelysium = ramo.getChild("aelysium");
        if (aelysium == null) {
            aelysium = Commands.literal("aelysium").requires(fonte -> AelysiumGuarda.isAutorizado()).build();
            ramo.addChild(aelysium);
        }
        aelysium.addChild(fabrica.apply(new Ramo(execute, esperado)).build());
    }
}
