package net.aelysium.aelysiummod.sistemas.mundo.renascimento;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class ComandoSetWorldSpawn {

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setworldspawn")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> definirGlobal(ctx, BlockPos.containing(ctx.getSource().getPosition()), 0.0F))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> definirGlobal(ctx, BlockPosArgument.getSpawnablePos(ctx, "pos"), 0.0F))
                        .then(Commands.argument("angle", AngleArgument.angle())
                                .executes(ctx -> definirGlobal(ctx, BlockPosArgument.getSpawnablePos(ctx, "pos"), AngleArgument.getAngle(ctx, "angle")))))
                .then(Commands.literal("limpar")
                        .then(Commands.argument("jogadores", EntityArgument.players())
                                .executes(ComandoSetWorldSpawn::limpar)))
                .then(Commands.argument("jogadores", EntityArgument.players())
                        .executes(ctx -> definirIndividual(ctx, ctx.getSource().getLevel(), BlockPos.containing(ctx.getSource().getPosition()), 0.0F))
                        .then(Commands.argument("dimensao", DimensionArgument.dimension())
                                .executes(ctx -> definirIndividual(ctx, DimensionArgument.getDimension(ctx, "dimensao"),
                                        BlockPos.containing(ctx.getSource().getPosition()), 0.0F))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> definirIndividual(ctx, DimensionArgument.getDimension(ctx, "dimensao"),
                                                BlockPosArgument.getBlockPos(ctx, "pos"), 0.0F))
                                        .then(Commands.argument("angle", AngleArgument.angle())
                                                .executes(ctx -> definirIndividual(ctx, DimensionArgument.getDimension(ctx, "dimensao"),
                                                        BlockPosArgument.getBlockPos(ctx, "pos"), AngleArgument.getAngle(ctx, "angle"))))))));
    }

    private static int definirGlobal(CommandContext<CommandSourceStack> ctx, BlockPos pos, float angulo) {
        ServerLevel nivel = ctx.getSource().getLevel();
        ConfiguracaoDimensaoRenascimento.definir(nivel.dimension(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angulo);
        if (nivel.dimension() == Level.OVERWORLD) {
            nivel.setDefaultSpawnPos(pos, angulo);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§aSpawn do mundo definido em §f" + nivel.dimension().location()
                + " §a(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") [" + angulo + "]"), true);
        return 1;
    }

    private static int definirIndividual(CommandContext<CommandSourceStack> ctx, ServerLevel nivel, BlockPos pos, float angulo) throws CommandSyntaxException {
        if (!nivel.isInWorldBounds(pos)) {
            ctx.getSource().sendFailure(Component.literal("§cPosição fora dos limites do mundo."));
            return 0;
        }
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        SpawnsIndividuais.Ponto ponto = new SpawnsIndividuais.Ponto(nivel.dimension(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angulo);
        for (ServerPlayer jogador : jogadores) {
            SpawnsIndividuais.definir(jogador.getUUID(), ponto);
        }
        String local = nivel.dimension().location() + " §a(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") [" + angulo + "]";
        if (jogadores.size() == 1) {
            Component nome = jogadores.iterator().next().getDisplayName();
            ctx.getSource().sendSuccess(() -> Component.literal("§aSpawn de ").append(nome).append(Component.literal(" §adefinido em §f" + local)), true);
        } else {
            int total = jogadores.size();
            ctx.getSource().sendSuccess(() -> Component.literal("§aSpawn de §f" + total + " §ajogadores definido em §f" + local), true);
        }
        return jogadores.size();
    }

    private static int limpar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(ctx, "jogadores");
        int removidos = 0;
        for (ServerPlayer jogador : jogadores) {
            if (SpawnsIndividuais.remover(jogador.getUUID())) removidos++;
        }
        int total = removidos;
        ctx.getSource().sendSuccess(() -> Component.literal("§aSpawn individual removido de §f" + total + " §ajogador(es). Eles voltam a usar o spawn global."), true);
        return removidos;
    }
}
