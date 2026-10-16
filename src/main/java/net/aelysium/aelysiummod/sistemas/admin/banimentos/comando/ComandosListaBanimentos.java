package net.aelysium.aelysiummod.sistemas.admin.banimentos.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.DadosBanimentoJogador;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.RedeListaBanimentos;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ComandosListaBanimentos {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_ITEMS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    BuiltInRegistries.ITEM.keySet().stream(),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BANNED_ITEMS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    ConfiguracaoListaBanimentos.getBannedItems().stream(),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_MODS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::getNamespace).distinct(),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BANNED_MODS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    ConfiguracaoListaBanimentos.getBannedItems().stream().map(ResourceLocation::getNamespace).distinct(),
                    builder
            );

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("itens")

                .then(Commands.literal("bloquear")
                        .requires(source -> source.hasPermission(2))
                        .executes(ComandosListaBanimentos::bloquearMao)
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_ALL_ITEMS)
                                .executes(ComandosListaBanimentos::bloquear))
                        .then(Commands.literal("mod")
                                .then(Commands.argument("mod_id", StringArgumentType.word())
                                        .suggests(SUGGEST_ALL_MODS)
                                        .executes(ComandosListaBanimentos::bloquearMod))))

                .then(Commands.literal("desbloquear")
                        .requires(source -> source.hasPermission(2))
                        .executes(ComandosListaBanimentos::desbloquearMao)
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_BANNED_ITEMS)
                                .executes(ComandosListaBanimentos::desbloquear))
                        .then(Commands.literal("mod")
                                .then(Commands.argument("mod_id", StringArgumentType.word())
                                        .suggests(SUGGEST_BANNED_MODS)
                                        .executes(ComandosListaBanimentos::desbloquearMod))))

                .then(Commands.literal("jogadores")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("permitir")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ComandosListaBanimentos::permitirMao)
                                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_BANNED_ITEMS)
                                                .executes(ComandosListaBanimentos::permitir))))

                        .then(Commands.literal("revogar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ComandosListaBanimentos::revogarMao)
                                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                                .suggests(SUGGEST_BANNED_ITEMS)
                                                .executes(ComandosListaBanimentos::revogar))))

                        .then(Commands.literal("checar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ComandosListaBanimentos::checar))))

                .then(ComandoBanidos.build())

                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(ComandosListaBanimentos::reload));
    }

    private static ResourceLocation getHeldItemId(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return null;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(held.getItem());
    }

    private static int doBloquear(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.voce_precisa_segurar_um_item_na_mao")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.item_nao_encontrado_no_registro", itemId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (ConfiguracaoListaBanimentos.addBannedItem(itemId)) {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.item_bloqueado", itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            RedeListaBanimentos.syncAllPlayers(ctx.getSource().getServer());
            GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.item_ja_esta_na_lista_de_banidos", itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int doDesbloquear(CommandContext<CommandSourceStack> ctx, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.voce_precisa_segurar_um_item_na_mao")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (ConfiguracaoListaBanimentos.removeBannedItem(itemId)) {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.item_removido_da_lista_de_banidos", itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            RedeListaBanimentos.syncAllPlayers(ctx.getSource().getServer());
            GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.item_nao_estava_na_lista_de_banidos", itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int doPermitir(CommandContext<CommandSourceStack> ctx, ServerPlayer target, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.voce_precisa_segurar_um_item_na_mao")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        DadosBanimentoJogador data = DadosBanimentoJogador.get(ctx.getSource().getServer());
        ServerPlayer autor = ctx.getSource().getPlayer();
        String nomeAutor = autor != null ? autor.getName().getString() : ctx.getSource().getTextName();
        if (data.allowItem(target.getUUID(), itemId, nomeAutor, autor != null ? autor.getUUID() : null)) {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.item_liberado_para", itemId, target.getName().getString())
                    .withStyle(ChatFormatting.GREEN), true);
            RedeListaBanimentos.syncToPlayer(target);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.jogador_ja_tem_permissao_pra_esse_item")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int doRevogar(CommandContext<CommandSourceStack> ctx, ServerPlayer target, ResourceLocation itemId) {
        if (itemId == null) {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.voce_precisa_segurar_um_item_na_mao")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        DadosBanimentoJogador data = DadosBanimentoJogador.get(ctx.getSource().getServer());
        if (data.revokeItem(target.getUUID(), itemId)) {
            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.permissao_de_revogada_de", itemId, target.getName().getString())
                    .withStyle(ChatFormatting.GREEN), true);
            RedeListaBanimentos.syncToPlayer(target);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.banimentos.jogador_nao_tinha_permissao_pra_esse")
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

    private static int bloquearMod(CommandContext<CommandSourceStack> ctx) {
        String modId = StringArgumentType.getString(ctx, "mod_id");

        List<ResourceLocation> itensDoMod = BuiltInRegistries.ITEM.keySet().stream()
                .filter(rl -> rl.getNamespace().equals(modId))
                .collect(Collectors.toList());

        if (itensDoMod.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.nenhum_item_encontrado_para_o_mod", modId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        int adicionados = ConfiguracaoListaBanimentos.addBannedItems(itensDoMod);
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.mod_bloqueado_item_ns_adicionado_s_no", modId, adicionados, itensDoMod.size())
                .withStyle(ChatFormatting.GREEN), true);
        RedeListaBanimentos.syncAllPlayers(ctx.getSource().getServer());
        GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
        return adicionados;
    }

    private static int desbloquearMod(CommandContext<CommandSourceStack> ctx) {
        String modId = StringArgumentType.getString(ctx, "mod_id");

        List<ResourceLocation> itensBanidosDoMod = ConfiguracaoListaBanimentos.getBannedItems().stream()
                .filter(rl -> rl.getNamespace().equals(modId))
                .collect(Collectors.toList());

        if (itensBanidosDoMod.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.nenhum_item_bloqueado_encontrado_para", modId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        int removidos = ConfiguracaoListaBanimentos.removeBannedItems(itensBanidosDoMod);
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.mod_desbloqueado_item_ns_removido_s", modId, removidos)
                .withStyle(ChatFormatting.GREEN), true);
        RedeListaBanimentos.syncAllPlayers(ctx.getSource().getServer());
        GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
        return removidos;
    }

    private static int permitirMao(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation itemId = getHeldItemId(ctx);
        int total = 0;
        for (ServerPlayer target : EntityArgument.getPlayers(ctx, "alvos")) {
            total += doPermitir(ctx, target, itemId);
        }
        return total;
    }

    private static int permitir(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation itemId = ResourceLocationArgument.getId(ctx, "item_id");
        int total = 0;
        for (ServerPlayer target : EntityArgument.getPlayers(ctx, "alvos")) {
            total += doPermitir(ctx, target, itemId);
        }
        return total;
    }

    private static int revogarMao(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation itemId = getHeldItemId(ctx);
        int total = 0;
        for (ServerPlayer target : EntityArgument.getPlayers(ctx, "alvos")) {
            total += doRevogar(ctx, target, itemId);
        }
        return total;
    }

    private static int revogar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation itemId = ResourceLocationArgument.getId(ctx, "item_id");
        int total = 0;
        for (ServerPlayer target : EntityArgument.getPlayers(ctx, "alvos")) {
            total += doRevogar(ctx, target, itemId);
        }
        return total;
    }

    private static int checar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        DadosBanimentoJogador data = DadosBanimentoJogador.get(ctx.getSource().getServer());

        for (ServerPlayer target : EntityArgument.getPlayers(ctx, "alvos")) {
            Set<ResourceLocation> allowed = data.getAllowedItems(target.getUUID());

            if (allowed.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.nao_tem_itens_desbloqueados", target.getName().getString())
                        .withStyle(ChatFormatting.YELLOW), false);
                continue;
            }

            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.itens_desbloqueados_de", target.getName().getString(), allowed.size())
                    .withStyle(ChatFormatting.GOLD), false);

            var liberacoes = data.getLiberacoes(target.getUUID());
            for (ResourceLocation rl : allowed.stream().sorted().collect(Collectors.toList())) {
                DadosBanimentoJogador.Liberacao lib = liberacoes.get(rl);
                String detalhe = lib == null || lib.em().isEmpty() ? "" : " §7(por " + lib.por() + ", " + lib.em() + ")";
                ctx.getSource().sendSuccess(() -> Component.literal("  + " + rl.toString() + detalhe)
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }

        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoListaBanimentos.load();
        int count = ConfiguracaoListaBanimentos.getBannedItems().size();
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.banimentos.config_recarregada_itens_banidos", count)
                .withStyle(ChatFormatting.GREEN), true);
        RedeListaBanimentos.syncAllPlayers(ctx.getSource().getServer());
        return 1;
    }
}