package net.aelysium.aelysiummod.sistemas.admin.atributos.comando;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AlvoItem {
    private static final String ARGUMENTO_ALVO = "alvo";

    private AlvoItem() {
    }

    @FunctionalInterface
    public interface Acao {
        int executar(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) throws CommandSyntaxException;
    }

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> T executar(T no, Acao acao) {
        no.executes(ctx -> rodar(ctx, null, acao));
        return no;
    }

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> T ligar(T no, Acao acao) {
        no.executes(ctx -> rodar(ctx, null, acao));
        no.then(Commands.argument(ARGUMENTO_ALVO, EntityArgument.player())
                .executes(ctx -> rodar(ctx, EntityArgument.getPlayer(ctx, ARGUMENTO_ALVO), acao)));
        return no;
    }

    private static int rodar(CommandContext<CommandSourceStack> ctx, ServerPlayer alvo, Acao acao) throws CommandSyntaxException {
        CommandSourceStack fonte = ctx.getSource();
        ServerPlayer jogador = alvo;
        if (jogador == null) {
            if (!(fonte.getEntity() instanceof ServerPlayer executor)) {
                fonte.sendFailure(Component.translatable("mensagem.aelysiummod.nucleo_comando.apenas_jogadores_podem_usar_este"));
                return 0;
            }
            jogador = executor;
        }
        ItemStack item = jogador.getMainHandItem();
        if (item.isEmpty()) {
            fonte.sendFailure(Component.translatable("mensagem.aelysiummod.atributos.segure_um_item_na_mao_principal"));
            return 0;
        }
        return acao.executar(ctx, jogador, item);
    }
}
