package net.aelysium.aelysiummod.sistemas.rp.habilidade.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.rp.habilidade.GerenciadorHabilidade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

import java.util.Collection;
import java.util.List;

public class ComandoHabilidade {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("habilidade")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("voo")
                        .executes(ctx -> executarVoo(ctx, alvoOuExecutor(ctx)))
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> executarVoo(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                .then(Commands.literal("deus")
                        .executes(ctx -> executarDeus(ctx, alvoOuExecutor(ctx)))
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> executarDeus(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                .then(Commands.literal("cura")
                        .executes(ctx -> executarCura(ctx, alvoOuExecutor(ctx)))
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> executarCura(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                .then(Commands.literal("congelar")
                        .executes(ctx -> executarCongelar(ctx, alvoOuExecutor(ctx)))
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> executarCongelar(ctx, EntityArgument.getPlayers(ctx, "alvos")))))

                .then(Commands.literal("conversar")
                        .executes(ctx -> executarConversar(ctx, alvoOuExecutor(ctx)))
                        .then(Commands.argument("alvos", EntityArgument.players())
                                .executes(ctx -> executarConversar(ctx, EntityArgument.getPlayers(ctx, "alvos")))));
    }

    private static Collection<ServerPlayer> alvoOuExecutor(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
            return List.of(player);
        }
        ctx.getSource().sendFailure(Component.translatable("mensagem.aelysiummod.habilidade.voce_precisa_especificar_um_jogador"));
        return List.of();
    }

    private static int executarVoo(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean ativo = GerenciadorHabilidade.toggleVoo(player);
            if (ativo) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().setFlyingSpeed(0.05f * 0.5f);
                player.onUpdateAbilities();
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.voo_ativado_para", player.getName().getString()), true);
            } else {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.voo_desativado_para", player.getName().getString()), true);
            }
        }
        return alvos.size();
    }

    private static int executarDeus(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean ativo = GerenciadorHabilidade.toggleDeus(player);
            if (ativo) {
                player.setInvulnerable(true);
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.modo_deus_ativado_para", player.getName().getString()), true);
            } else {
                player.setInvulnerable(false);
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.modo_deus_desativado_para", player.getName().getString()), true);
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
                    Component.translatableEscape("mensagem.aelysiummod.habilidade.foi_curado_completamente", player.getName().getString()), true);
        }
        return alvos.size();
    }

    private static int executarCongelar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean ativo = GerenciadorHabilidade.toggleCongelar(player);
            if (ativo) {
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.foi_congelado", player.getName().getString()), true);
            } else {
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.foi_descongelado", player.getName().getString()), true);
            }
        }
        return alvos.size();
    }

    private static int executarConversar(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> alvos) {
        for (ServerPlayer player : alvos) {
            boolean mudo = GerenciadorHabilidade.toggleMudo(player);
            if (mudo) {
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.nao_pode_mais_conversar", player.getName().getString()), true);
                player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.habilidade.voce_foi_silenciado_e_nao_pode_mais"));
            } else {
                ctx.getSource().sendSuccess(() ->
                        Component.translatableEscape("mensagem.aelysiummod.habilidade.pode_conversar_novamente", player.getName().getString()), true);
                player.sendSystemMessage(Component.translatable("mensagem.aelysiummod.habilidade.voce_pode_enviar_mensagens_no_chat"));
            }
        }
        return alvos.size();
    }
}