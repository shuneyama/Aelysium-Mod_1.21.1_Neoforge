package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.deus.DeusType;
import net.aelysium.aelysiummod.teleporte.TeleportTransitionHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class TeleportCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("aelysium")
                        .then(Commands.literal("teleportar")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.argument("alvos", EntityArgument.players())
                                        .then(Commands.argument("deus", StringArgumentType.string())
                                                .suggests((ctx, builder) -> {
                                                    for (DeusType d : DeusType.values()) {
                                                        if (d == DeusType.NONE) continue;
                                                        builder.suggest(d.displayName);
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("posicao", Vec3Argument.vec3())
                                                        .executes(ctx -> executar(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayers(ctx, "alvos"),
                                                                StringArgumentType.getString(ctx, "deus"),
                                                                Vec3Argument.getCoordinates(ctx, "posicao"),
                                                                null
                                                        ))
                                                        .then(Commands.argument("rotacao", RotationArgument.rotation())
                                                                .executes(ctx -> executar(
                                                                        ctx.getSource(),
                                                                        EntityArgument.getPlayers(ctx, "alvos"),
                                                                        StringArgumentType.getString(ctx, "deus"),
                                                                        Vec3Argument.getCoordinates(ctx, "posicao"),
                                                                        RotationArgument.getRotation(ctx, "rotacao")
                                                                ))
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static int executar(CommandSourceStack source,
                                Collection<ServerPlayer> alvos,
                                String deusNome,
                                Coordinates posicao,
                                Coordinates rotacao) {
        DeusType deus = DeusType.NONE;
        for (DeusType d : DeusType.values()) {
            if (d.displayName.equalsIgnoreCase(deusNome) || d.id.equalsIgnoreCase(deusNome)) {
                deus = d;
                break;
            }
        }

        if (deus == DeusType.NONE) {
            source.sendFailure(Component.literal("§cDeus '" + deusNome + "' não encontrado!"));
            source.sendFailure(Component.literal("§7Deuses disponíveis: Kairos, Ronova, Azarus, Damselette, Ashyra, Velgrynd, Velzard, Klaus"));
            return 0;
        }

        for (ServerPlayer jogador : alvos) {
            if (TeleportTransitionHandler.isInTransition(jogador.getUUID())) {
                source.sendFailure(Component.literal("§c" + jogador.getName().getString() + " já está sendo teleportado!"));
                continue;
            }

            Vec3 pos = posicao.getPosition(source);
            float destYaw, destPitch;

            if (rotacao != null) {
                Vec2 rot = rotacao.getRotation(source);
                destYaw = rot.y;
                destPitch = rot.x;
            } else {
                destYaw = jogador.getYRot();
                destPitch = jogador.getXRot();
            }

            TeleportTransitionHandler.startTransition(jogador, deus,
                    pos.x, pos.y, pos.z, destYaw, destPitch, true);

            String nomeJogador = jogador.getName().getString();
            String nomeDeus = deus.displayName;
            source.sendSuccess(() -> Component.literal(
                    "§eTeleportando §f" + nomeJogador + "§e com efeito de §f" + nomeDeus + "§e..."
            ), true);
        }

        return alvos.size();
    }
}