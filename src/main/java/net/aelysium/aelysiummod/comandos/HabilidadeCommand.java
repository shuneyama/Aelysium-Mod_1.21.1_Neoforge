package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.habilidade.HabilidadeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;

import java.util.Collection;

public class HabilidadeCommand {

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("aelysium")
                .then(Commands.literal("habilidade")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("voo")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> executarVoo(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                        .then(Commands.literal("deus")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> executarDeus(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                        .then(Commands.literal("cura")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> executarCura(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                        .then(Commands.literal("congelar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> executarCongelar(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                        .then(Commands.literal("conversar")
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .executes(ctx -> executarConversar(ctx, EntityArgument.getPlayers(ctx, "alvos")))))
                ));
    }

    private static int executarVoo(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean ativo = HabilidadeManager.toggleVoo(player);
            if (ativo) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().setFlyingSpeed(0.05f * 0.5f);
                player.onUpdateAbilities();
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§fVoo §aativado §fpara §b" + player.getName().getString()), true);
            } else {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§fVoo §cdesativado §fpara §b" + player.getName().getString()), true);
            }
        }
        return alvos.size();
    }

    private static int executarDeus(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean ativo = HabilidadeManager.toggleDeus(player);
            if (ativo) {
                player.setInvulnerable(true);
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§fModo Deus §aativado §fpara §b" + player.getName().getString()), true);
            } else {
                player.setInvulnerable(false);
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§fModo Deus §cdesativado §fpara §b" + player.getName().getString()), true);
            }
        }
        return alvos.size();
    }

    private static int executarCura(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            player.setHealth(player.getMaxHealth());
            FoodData food = player.getFoodData();
            food.setFoodLevel(20);
            food.setSaturation(20f);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§b" + player.getName().getString() + " §ffoi §acurado §fcompletamente!"), true);
        }
        return alvos.size();
    }

    private static final ResourceLocation FREEZE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "congelar_movement");

    private static int executarCongelar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean ativo = HabilidadeManager.toggleCongelar(player);
            if (ativo) {
                player.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                        new AttributeModifier(FREEZE_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                player.getAttribute(Attributes.JUMP_STRENGTH).addPermanentModifier(
                        new AttributeModifier(FREEZE_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§b" + player.getName().getString() + " §ffoi §9congelado§f!"), true);
            } else {
                player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FREEZE_MODIFIER_ID);
                player.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(FREEZE_MODIFIER_ID);
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§b" + player.getName().getString() + " §ffoi §adescongelado§f!"), true);
            }
        }
        return alvos.size();
    }

    private static int executarConversar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean mudo = HabilidadeManager.toggleMudo(player);
            if (mudo) {
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§b" + player.getName().getString() + " §fnão pode mais §cconversar§f!"), true);
                player.sendSystemMessage(Component.literal("§cVocê foi silenciado e não pode mais enviar mensagens no chat."));
            } else {
                ctx.getSource().sendSuccess(() ->
                        Component.literal("§b" + player.getName().getString() + " §fpode §aconversar §fnovamente!"), true);
                player.sendSystemMessage(Component.literal("§aVocê pode enviar mensagens no chat novamente."));
            }
        }
        return alvos.size();
    }
}