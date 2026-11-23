package net.aelysium.aelysiummod.sistemas.admin.atributos.comando;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;

public final class AcoesDurabilidade {
    private AcoesDurabilidade() {
    }

    public static int definir(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        int valor = IntegerArgumentType.getInteger(ctx, "valor");
        item.set(DataComponents.MAX_DAMAGE, valor);
        item.set(DataComponents.DAMAGE, 0);
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.atributos.durabilidade_maxima_definida_para", ChatFormatting.GREEN, valor), false);
        return 1;
    }

    public static int restaurar(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        int maximo = item.getMaxDamage();
        if (maximo <= 0) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.atributos.este_item_nao_possui_durabilidade"));
            return 0;
        }
        item.setDamageValue(0);
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.atributos.durabilidade_restaurada", ChatFormatting.GREEN, maximo, maximo), false);
        return 1;
    }

    public static int inquebravel(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        if (item.has(DataComponents.UNBREAKABLE)) {
            item.remove(DataComponents.UNBREAKABLE);
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.atributos.item_voltou_a_ter_durabilidade_normal"), false);
        } else {
            item.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.atributos.item_definido_como_indestrutivel"), false);
        }
        return 1;
    }
}
