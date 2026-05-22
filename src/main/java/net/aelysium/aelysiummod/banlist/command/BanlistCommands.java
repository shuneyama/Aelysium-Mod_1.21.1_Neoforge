package net.aelysium.aelysiummod.banlist.command;

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
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;
import net.aelysium.aelysiummod.banlist.data.PlayerBanData;
import net.aelysium.aelysiummod.banlist.network.BanlistNetwork;

import java.util.Set;
import java.util.stream.Collectors;

public class BanlistCommands {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ALL_ITEMS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    BuiltInRegistries.ITEM.keySet().stream(),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BANNED_ITEMS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    BanlistConfig.getBannedItems().stream(),
                    builder
            );

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("banlist")

                .then(Commands.literal("bloquear")
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_ALL_ITEMS)
                                .executes(BanlistCommands::bloquear)))

                .then(Commands.literal("desbloquear")
                        .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                .suggests(SUGGEST_BANNED_ITEMS)
                                .executes(BanlistCommands::desbloquear)))

                .then(Commands.literal("mao")
                        .then(Commands.literal("bloquear")
                                .executes(BanlistCommands::bloquearMao))
                        .then(Commands.literal("desbloquear")
                                .executes(BanlistCommands::desbloquearMao)))

                .then(Commands.literal("permitir")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_BANNED_ITEMS)
                                        .executes(BanlistCommands::permitir))))

                .then(Commands.literal("revogar")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_BANNED_ITEMS)
                                        .executes(BanlistCommands::revogar))))

                .then(Commands.literal("lista")
                        .executes(BanlistCommands::lista))

                .then(Commands.literal("checar")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(BanlistCommands::checar)))

                .then(Commands.literal("reload")
                        .executes(BanlistCommands::reload));
    }

    private static ResourceLocation getHeldItemId(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) return null;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(held.getItem());
    }

    private static int bloquearMao(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation itemId = getHeldItemId(ctx);

        if (itemId == null) {
            ctx.getSource().sendFailure(Component.literal("Você precisa segurar um item na mão principal.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (BanlistConfig.addBannedItem(itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item bloqueado: " + itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            BanlistNetwork.syncAllPlayers(ctx.getSource().getServer());
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Item já está na lista de banidos: " + itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int desbloquearMao(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation itemId = getHeldItemId(ctx);

        if (itemId == null) {
            ctx.getSource().sendFailure(Component.literal("Você precisa segurar um item na mão principal.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (BanlistConfig.removeBannedItem(itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item removido da lista de banidos: " + itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            BanlistNetwork.syncAllPlayers(ctx.getSource().getServer());
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Item não estava na lista de banidos: " + itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int bloquear(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation itemId = ResourceLocationArgument.getId(ctx, "item_id");

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            ctx.getSource().sendFailure(Component.literal("Item não encontrado no registro: " + itemId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (BanlistConfig.addBannedItem(itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item bloqueado: " + itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            BanlistNetwork.syncAllPlayers(ctx.getSource().getServer());
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Item já está na lista de banidos: " + itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int desbloquear(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation itemId = ResourceLocationArgument.getId(ctx, "item_id");

        if (BanlistConfig.removeBannedItem(itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item removido da lista de banidos: " + itemId)
                    .withStyle(ChatFormatting.GREEN), true);
            BanlistNetwork.syncAllPlayers(ctx.getSource().getServer());
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Item não estava na lista de banidos: " + itemId)
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int permitir(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation itemId = ResourceLocationArgument.getId(ctx, "item_id");

        PlayerBanData data = PlayerBanData.get(ctx.getSource().getServer());
        if (data.allowItem(target.getUUID(), itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item " + itemId + " liberado para " + target.getName().getString())
                    .withStyle(ChatFormatting.GREEN), true);
            BanlistNetwork.syncToPlayer(target);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Jogador já tem permissão pra esse item.")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int revogar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        ResourceLocation itemId = ResourceLocationArgument.getId(ctx, "item_id");

        PlayerBanData data = PlayerBanData.get(ctx.getSource().getServer());
        if (data.revokeItem(target.getUUID(), itemId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Permissão de " + itemId + " revogada de " + target.getName().getString())
                    .withStyle(ChatFormatting.GREEN), true);
            BanlistNetwork.syncToPlayer(target);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("Jogador não tinha permissão pra esse item.")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }
    }

    private static int lista(CommandContext<CommandSourceStack> ctx) {
        Set<ResourceLocation> banned = BanlistConfig.getBannedItems();

        if (banned.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Nenhum item banido.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("=== Itens Banidos (" + banned.size() + ") ===")
                .withStyle(ChatFormatting.GOLD), false);

        for (ResourceLocation rl : banned.stream().sorted().collect(Collectors.toList())) {
            ctx.getSource().sendSuccess(() -> Component.literal("  - " + rl.toString())
                    .withStyle(ChatFormatting.RED), false);
        }

        return 1;
    }

    private static int checar(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        PlayerBanData data = PlayerBanData.get(ctx.getSource().getServer());
        Set<ResourceLocation> allowed = data.getAllowedItems(target.getUUID());

        if (allowed.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(target.getName().getString() + " não tem itens desbloqueados individualmente.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("=== Itens desbloqueados de " + target.getName().getString() + " (" + allowed.size() + ") ===")
                .withStyle(ChatFormatting.GOLD), false);

        for (ResourceLocation rl : allowed.stream().sorted().collect(Collectors.toList())) {
            ctx.getSource().sendSuccess(() -> Component.literal("  + " + rl.toString())
                    .withStyle(ChatFormatting.GREEN), false);
        }

        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        BanlistConfig.load();
        int count = BanlistConfig.getBannedItems().size();
        ctx.getSource().sendSuccess(() -> Component.literal("Config recarregada! " + count + " itens banidos.")
                .withStyle(ChatFormatting.GREEN), true);
        BanlistNetwork.syncAllPlayers(ctx.getSource().getServer());
        return 1;
    }
}