package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.aelysium.aelysiummod.network.ValkyriaFlightPacket;
import net.aelysium.aelysiummod.raca.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class RacaCommand {

    private static final SuggestionProvider<CommandSourceStack> RACE_SUGGESTIONS =
            (ctx, builder) -> {
                for (RaceType r : RaceType.values()) {
                    if (r != RaceType.NONE) builder.suggest(r.id);
                }
                builder.suggest("nenhum");
                return builder.buildFuture();
            };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("aelysium")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("raca")
                                .then(Commands.literal("definir")
                                        .then(Commands.argument("raca", StringArgumentType.string())
                                                .suggests(RACE_SUGGESTIONS)
                                                .then(Commands.argument("jogador", EntityArgument.player())
                                                        .executes(RacaCommand::executeSet)
                                                )
                                        )
                                )
                                .then(Commands.literal("resetar")
                                        .then(Commands.argument("jogador", EntityArgument.player())
                                                .executes(RacaCommand::executeReset)
                                        )
                                )
                                .then(Commands.literal("ver")
                                        .then(Commands.argument("jogador", EntityArgument.player())
                                                .executes(RacaCommand::executeView)
                                        )
                                )
                        )
        );
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
        try {
            String raceId = StringArgumentType.getString(ctx, "raca");
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            RaceType race = RaceType.fromId(raceId);

            if (RaceRevealAnimation.isAnimating(target.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("§cEsse jogador já está recebendo uma raça."));
                return 0;
            }

            RaceType oldRace = RaceTicker.getCachedRace(target.getUUID());

            if (race == RaceType.NONE) {
                return doReset(ctx, target);
            }

            RaceRevealAnimation.start(target, race, oldRace);

            String msg = target.getName().getString() + " está recebendo a raça " + race.displayName + "...";
            ctx.getSource().sendSuccess(() -> Component.literal("§a" + msg), true);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro ao aplicar raça: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            return doReset(ctx, target);
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro ao resetar raça: " + e.getMessage()));
            return 0;
        }
    }

    private static int doReset(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        target.removeAllEffects();
        RaceManager.applyRace(target, RaceType.NONE);
        RaceData.get(target.serverLevel()).setRace(target.getUUID(), RaceType.NONE);
        RaceTicker.updateCache(target.getUUID(), RaceType.NONE);

        PacketDistributor.sendToPlayer(target, new ValkyriaFlightPacket(false, 0, 0));

        ctx.getSource().sendSuccess(() ->
                Component.literal("§a" + target.getName().getString() + " perdeu sua raça."), true);
        target.sendSystemMessage(Component.literal("§b[Aelysium] §fSua raça foi removida."));

        return 1;
    }

    private static int executeView(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
            RaceType race = RaceTicker.getCachedRace(target.getUUID());

            ctx.getSource().sendSuccess(() ->
                    Component.literal("§b" + target.getName().getString() + " §fé da raça §e" + race.displayName), false);

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cErro: " + e.getMessage()));
            return 0;
        }
    }
}