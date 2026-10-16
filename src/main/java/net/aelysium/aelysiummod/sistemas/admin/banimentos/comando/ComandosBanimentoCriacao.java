package net.aelysium.aelysiummod.sistemas.admin.banimentos.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoBanimentoReceitas;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Collectors;

public class ComandosBanimentoCriacao {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_ITEMS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet().stream(), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BLOCKED = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(ConfiguracaoBanimentoReceitas.getBlockedItems().stream(), builder);

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("receitas")

                .then(Commands.literal("bloquear")
                        .executes(ComandosBanimentoCriacao::bloquearMao)
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_ALL_ITEMS)
                                .executes(ComandosBanimentoCriacao::bloquear)))

                .then(Commands.literal("desbloquear")
                        .executes(ComandosBanimentoCriacao::desbloquearMao)
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_BLOCKED)
                                .executes(ComandosBanimentoCriacao::desbloquear)))

                .then(Commands.literal("lista")
                        .executes(ComandosBanimentoCriacao::lista));
    }

    private static ResourceLocation getHeldItemId(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return null;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(held.getItem());
    }

    private static int doBloquear(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.segure_um_item_na_mao_ou_informe_o_id")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.item_nao_encontrado", itemId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        ServerPlayer autor = ctx.getSource().getEntity() instanceof ServerPlayer jogador ? jogador : null;
        String nomeAutor = autor != null ? autor.getName().getString() : ctx.getSource().getTextName();
        if (ConfiguracaoBanimentoReceitas.addBlockedItem(itemId, nomeAutor, autor != null ? autor.getUUID() : null)) {
            GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.receita_bloqueada", itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.receita_de_ja_estava_bloqueada", itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int doDesbloquear(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.segure_um_item_na_mao_ou_informe_o_id")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (ConfiguracaoBanimentoReceitas.removeBlockedItem(itemId)) {
            GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.receita_desbloqueada", itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.receita_de_nao_estava_bloqueada", itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int bloquearMao(CommandContext<CommandSourceStack> ctx) {
        return doBloquear(ctx, getHeldItemId(ctx));
    }

    private static int desbloquearMao(CommandContext<CommandSourceStack> ctx) {
        return doDesbloquear(ctx, getHeldItemId(ctx));
    }

    private static int bloquear(CommandContext<CommandSourceStack> ctx) {
        return doBloquear(ctx, ResourceLocationArgument.getId(ctx, "item_id"));
    }

    private static int desbloquear(CommandContext<CommandSourceStack> ctx) {
        return doDesbloquear(ctx, ResourceLocationArgument.getId(ctx, "item_id"));
    }

    private static int lista(CommandContext<CommandSourceStack> ctx) {
        var blocked = ConfiguracaoBanimentoReceitas.getBlockedItems();
        if (blocked.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("mensagem.aelysiummod.banimentos.nenhuma_receita_bloqueada")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.receitas_bloqueadas", blocked.size())
                .withStyle(ChatFormatting.GOLD), false);
        for (var rl : blocked.stream().sorted().collect(Collectors.toList())) {
            var remocao = ConfiguracaoBanimentoReceitas.getRemocoes().get(rl);
            String detalhe = remocao == null || remocao.em().isEmpty() ? "" : " §7(por " + remocao.por() + ", " + remocao.em() + ")";
            ctx.getSource().sendSuccess(() -> Component.literal("  - " + rl + detalhe)
                    .withStyle(ChatFormatting.RED), false);
        }
        return 1;
    }
}
