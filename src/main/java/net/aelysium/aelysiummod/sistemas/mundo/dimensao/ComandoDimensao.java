package net.aelysium.aelysiummod.sistemas.mundo.dimensao;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.aelysium.aelysiummod.sistemas.mundo.renascimento.ConfiguracaoDimensaoRenascimento;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ComandoDimensao {
    private static final String P = "mensagem.aelysiummod.dimensao.";
    private static final float YAW_PADRAO = 180.0F;
    private static final int[][] CANDIDATOS = {{0, 0}, {8, 0}, {-8, 0}, {0, 8}, {0, -8}, {16, 16}, {-16, 16}, {16, -16}, {-16, -16}};

    private record Destino(Vec3 posicao, float yaw, boolean semChao) {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("dimensao")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("dimensao", DimensionArgument.dimension())
                        .executes(ctx -> executar(ctx, null, null))
                        .then(Commands.argument("alvos", EntityArgument.entities())
                                .executes(ctx -> executar(ctx, null, EntityArgument.getEntities(ctx, "alvos")))
                                .then(Commands.argument("posicao", Vec3Argument.vec3())
                                        .executes(ctx -> executar(ctx, Vec3Argument.getCoordinates(ctx, "posicao"), EntityArgument.getEntities(ctx, "alvos"))))));
    }

    private static int executar(CommandContext<CommandSourceStack> ctx, Coordinates coordenadas, Collection<? extends Entity> informados) throws CommandSyntaxException {
        CommandSourceStack fonte = ctx.getSource();
        ServerLevel nivel = DimensionArgument.getDimension(ctx, "dimensao");
        Collection<? extends Entity> alvos = informados;
        if (alvos == null) {
            if (fonte.getEntity() == null) {
                fonte.sendFailure(Component.translatable("mensagem.aelysiummod.nucleo_comando.apenas_jogadores_podem_usar_este"));
                return 0;
            }
            alvos = List.of(fonte.getEntity());
        }
        Destino destino = coordenadas != null
                ? new Destino(coordenadas.getPosition(fonte), YAW_PADRAO, false)
                : destinoPadrao(nivel);
        Vec3 posicao = destino.posicao();
        if (!Level.isInSpawnableBounds(BlockPos.containing(posicao))) {
            fonte.sendFailure(Component.translatable(P + "posicao_invalida").withStyle(ChatFormatting.RED));
            return 0;
        }
        int teleportados = 0;
        for (Entity alvo : alvos) {
            if (teleportar(alvo, nivel, posicao, destino.yaw())) teleportados++;
        }
        String nomeDimensao = nivel.dimension().location().toString();
        if (teleportados == 0) {
            fonte.sendFailure(Component.translatable(P + "nenhum_teleportado").withStyle(ChatFormatting.RED));
            return 0;
        }
        int total = teleportados;
        fonte.sendSuccess(() -> TextoComando.traduzir(P + "teleportados", ChatFormatting.GREEN, total, nomeDimensao,
                Mth.floor(posicao.x), Mth.floor(posicao.y), Mth.floor(posicao.z)), true);
        if (destino.semChao()) {
            fonte.sendSuccess(() -> TextoComando.traduzir(P + "sem_chao", ChatFormatting.GRAY, nomeDimensao), false);
        }
        return teleportados;
    }

    private static boolean teleportar(Entity entidade, ServerLevel nivel, Vec3 posicao, float yaw) {
        var evento = EventHooks.onEntityTeleportCommand(entidade, posicao.x, posicao.y, posicao.z);
        if (evento.isCanceled()) return false;
        if (!entidade.teleportTo(nivel, evento.getTargetX(), evento.getTargetY(), evento.getTargetZ(), Set.of(), yaw, 0.0F)) return false;
        if (!(entidade instanceof LivingEntity vivo) || !vivo.isFallFlying()) {
            entidade.setDeltaMovement(entidade.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            entidade.setOnGround(true);
        }
        if (entidade instanceof PathfinderMob mob) {
            mob.getNavigation().stop();
        }
        return true;
    }

    private static Destino destinoPadrao(ServerLevel nivel) {
        if (ConfiguracaoDimensaoRenascimento.definido() && nivel.dimension().equals(ConfiguracaoDimensaoRenascimento.getDimensaoPadrao())) {
            return new Destino(new Vec3(ConfiguracaoDimensaoRenascimento.getX(), ConfiguracaoDimensaoRenascimento.getY(), ConfiguracaoDimensaoRenascimento.getZ()),
                    ConfiguracaoDimensaoRenascimento.getYaw(), false);
        }
        for (int[] candidato : CANDIDATOS) {
            Vec3 seguro = procurarChao(nivel, candidato[0], candidato[1]);
            if (seguro != null) return new Destino(seguro, YAW_PADRAO, false);
        }
        return new Destino(new Vec3(0.5, nivel.getSeaLevel(), 0.5), YAW_PADRAO, true);
    }

    private static Vec3 procurarChao(ServerLevel nivel, int x, int z) {
        nivel.getChunk(x >> 4, z >> 4);
        int minimo = nivel.getMinBuildHeight();
        if (!nivel.dimensionType().hasCeiling()) {
            int y = nivel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= minimo + 1) return null;
            BlockPos chao = new BlockPos(x, y - 1, z);
            if (!nivel.getFluidState(chao).isEmpty() && nivel.getFluidState(chao).is(net.minecraft.tags.FluidTags.LAVA)) return null;
            return new Vec3(x + 0.5, y, z + 0.5);
        }
        for (int y = nivel.getLogicalHeight() - 2; y > minimo + 1; y--) {
            BlockPos pes = new BlockPos(x, y, z);
            BlockPos abaixo = pes.below();
            if (!nivel.getBlockState(abaixo).isFaceSturdy(nivel, abaixo, Direction.UP)) continue;
            if (!livre(nivel, pes) || !livre(nivel, pes.above())) continue;
            return new Vec3(x + 0.5, y, z + 0.5);
        }
        return null;
    }

    private static boolean livre(ServerLevel nivel, BlockPos pos) {
        return nivel.getBlockState(pos).getCollisionShape(nivel, pos).isEmpty() && nivel.getFluidState(pos).isEmpty();
    }
}
