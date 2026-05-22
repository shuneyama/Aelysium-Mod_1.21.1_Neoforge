package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class NpcCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("npc")

                .then(Commands.literal("lista")
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            ServerLevel level = source.getLevel();

                            source.sendSuccess(() -> Component.literal("§6§l═══ NPCs no mundo ═══"), false);

                            int count = 0;
                            for (var entity : level.getAllEntities()) {
                                if (entity instanceof CustomNpcEntity npc) {
                                    count++;
                                    String pos = String.format("%.0f, %.0f, %.0f",
                                            npc.getX(), npc.getY(), npc.getZ());
                                    final int finalId = npc.getNpcId();
                                    source.sendSuccess(() -> Component.literal(
                                            "§7[§b#" + finalId + "§7] §e" + npc.getNpcName()
                                                    + " §7[" + pos + "]"
                                    ), false);
                                }
                            }

                            final int finalCount = count;
                            source.sendSuccess(() -> Component.literal(
                                    "§7Total: §a" + finalCount + " NPCs"), false);
                            return count;
                        })
                )

                .then(Commands.literal("remover")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    int targetId = IntegerArgumentType.getInteger(ctx, "id");
                                    ServerLevel level = source.getLevel();

                                    for (var entity : level.getAllEntities()) {
                                        if (entity instanceof CustomNpcEntity npc
                                                && npc.getNpcId() == targetId) {
                                            String name = npc.getNpcName();
                                            npc.discard();
                                            source.sendSuccess(() -> Component.literal(
                                                    "§aNPC §e" + name + "§a removido!"), true);
                                            return 1;
                                        }
                                    }

                                    source.sendFailure(Component.literal(
                                            "§cNPC #" + targetId + " não encontrado!"));
                                    return 0;
                                })
                        )
                )

                .then(Commands.literal("teleportar")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    int targetId = IntegerArgumentType.getInteger(ctx, "id");
                                    ServerLevel level = source.getLevel();

                                    for (var entity : level.getAllEntities()) {
                                        if (entity instanceof CustomNpcEntity npc
                                                && npc.getNpcId() == targetId) {
                                            if (source.getEntity() instanceof ServerPlayer player) {
                                                player.teleportTo(npc.getX(), npc.getY(), npc.getZ());
                                                source.sendSuccess(() -> Component.literal(
                                                        "§aTeleportado para §e" + npc.getNpcName()
                                                                + " §7(#" + npc.getNpcId() + ")"), true);
                                                return 1;
                                            }
                                        }
                                    }

                                    source.sendFailure(Component.literal(
                                            "§cNPC #" + targetId + " não encontrado!"));
                                    return 0;
                                })
                        )
                );
    }
}