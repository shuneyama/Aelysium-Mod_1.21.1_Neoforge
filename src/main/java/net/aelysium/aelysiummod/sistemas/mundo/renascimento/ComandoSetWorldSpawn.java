package net.aelysium.aelysiummod.sistemas.mundo.renascimento;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class ComandoSetWorldSpawn {

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setworldspawn")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> definir(ctx, BlockPos.containing(ctx.getSource().getPosition()), 0.0F))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> definir(ctx, BlockPosArgument.getSpawnablePos(ctx, "pos"), 0.0F))
                        .then(Commands.argument("angle", AngleArgument.angle())
                                .executes(ctx -> definir(ctx, BlockPosArgument.getSpawnablePos(ctx, "pos"), AngleArgument.getAngle(ctx, "angle"))))));
    }

    private static int definir(CommandContext<CommandSourceStack> ctx, BlockPos pos, float angulo) {
        ServerLevel nivel = ctx.getSource().getLevel();
        ConfiguracaoDimensaoRenascimento.definir(nivel.dimension(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angulo);
        if (nivel.dimension() == Level.OVERWORLD) {
            nivel.setDefaultSpawnPos(pos, angulo);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§aSpawn do mundo definido em §f" + nivel.dimension().location()
                + " §a(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") [" + angulo + "]"), true);
        return 1;
    }
}
