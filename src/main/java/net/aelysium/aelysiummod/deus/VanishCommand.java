package net.aelysium.aelysiummod.deus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class VanishCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> vanishNode = Commands.literal("vanish");

        for (DeusType deus : DeusType.values()) {
            if (deus == DeusType.NONE) continue;
            final DeusType finalDeus = deus;
            vanishNode.then(
                    Commands.literal(deus.id)
                            .executes(context -> {
                                if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                                    return 0;
                                }
                                VanishTransitionHandler.startTransition(player, finalDeus);
                                return 1;
                            })
            );
        }

        dispatcher.register(
                Commands.literal("aelysium")
                        .requires(src -> src.hasPermission(2))
                        .then(vanishNode)
        );
    }
}
