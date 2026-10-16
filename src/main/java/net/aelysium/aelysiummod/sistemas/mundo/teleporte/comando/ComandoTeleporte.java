package net.aelysium.aelysiummod.sistemas.mundo.teleporte.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.mundo.teleporte.ManipuladorTransicaoTeleporte;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class ComandoTeleporte {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("teleportar")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("alvos", EntityArgument.players())
                        .then(Commands.argument("deus", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    for (TipoDeus d : TipoDeus.values()) {
                                        if (d == TipoDeus.NONE) continue;
                                        builder.suggest(d.displayName);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("dimensao", DimensionArgument.dimension())
                                        .then(Commands.argument("posicao", Vec3Argument.vec3())
                                                .executes(ctx -> executar(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayers(ctx, "alvos"),
                                                        StringArgumentType.getString(ctx, "deus"),
                                                        DimensionArgument.getDimension(ctx, "dimensao").dimension(),
                                                        Vec3Argument.getCoordinates(ctx, "posicao"),
                                                        null
                                                ))
                                                .then(Commands.argument("rotacao", RotationArgument.rotation())
                                                        .executes(ctx -> executar(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayers(ctx, "alvos"),
                                                                StringArgumentType.getString(ctx, "deus"),
                                                                DimensionArgument.getDimension(ctx, "dimensao").dimension(),
                                                                Vec3Argument.getCoordinates(ctx, "posicao"),
                                                                RotationArgument.getRotation(ctx, "rotacao")
                                                        ))
                                                )
                                        )
                                )
                        )
                );
    }

    private static int executar(CommandSourceStack source,
                                Collection<ServerPlayer> alvos,
                                String deusNome,
                                ResourceKey<Level> dimensao,
                                Coordinates posicao,
                                Coordinates rotacao) {
        TipoDeus deus = TipoDeus.NONE;
        for (TipoDeus d : TipoDeus.values()) {
            if (d.displayName.equalsIgnoreCase(deusNome) || d.id.equalsIgnoreCase(deusNome)) {
                deus = d;
                break;
            }
        }

        if (deus == TipoDeus.NONE) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.teleporte.deus_nao_encontrado", deusNome));
            source.sendFailure(Component.translatable("mensagem.aelysiummod.teleporte.deuses_disponiveis_kairos_ronova"));
            return 0;
        }

        for (ServerPlayer jogador : alvos) {
            if (ManipuladorTransicaoTeleporte.isInTransition(jogador.getUUID())) {
                source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.teleporte.ja_esta_sendo_teleportado", jogador.getName().getString()));
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

            ManipuladorTransicaoTeleporte.startTransition(jogador, deus, dimensao,
                    pos.x, pos.y, pos.z, destYaw, destPitch, true);

            String nomeJogador = jogador.getName().getString();
            String nomeDeus = deus.displayName;
            source.sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.teleporte.teleportando_com_efeito_de", nomeJogador, nomeDeus), true);
        }

        return alvos.size();
    }
}