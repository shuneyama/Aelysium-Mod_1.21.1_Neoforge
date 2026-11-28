package net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco;

import net.aelysium.aelysiummod.sistemas.chefes.base.evento.AgendadorNivel;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.util.UtilFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.rede.*;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.VoidBlossomEntity;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.EntityAdapter;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.EntityStats;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.EfeitosBlocoFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class VoidBlossomBlock extends Block {
    private static final int healAnimationDelay = 16;
    private static final int healDelay = 64;
    private final VoxelShape shape = box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);

    public VoidBlossomBlock(Properties properties) {
        super(properties);
    }

    public static final int TICKS_QUEBRA = 100;

    public static float progressoQuebra(Player player) {
        return player.getAbilities().instabuild ? 1.0F : 1.0F / TICKS_QUEBRA;
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return progressoQuebra(player);
    }

    @Override
    public void onPlace(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        level.scheduleTick(pos, this, healDelay);
        healNearbyEntities(level, pos);
    }

    private void healNearbyEntities(ServerLevel level, BlockPos pos){
        level.getEntitiesOfClass(VoidBlossomEntity.class, new AABB(pos).inflate(40.0, 20.0, 40.0))
                .forEach(voidBlossom -> {
                    AgendadorNivel.de(level).addEvent(
                            new TimedEvent(
                                    () -> UtilFlorVazio.cappedHeal(new EntityAdapter(voidBlossom), new EntityStats(voidBlossom), VoidBlossomEntity.hpMilestones, 10f, voidBlossom::heal),
                                    healAnimationDelay
                            ));

                    RedeChefes.enviarPerto(new PacoteCuraFlor(VecUtils.asVec3(pos).add(VecUtils.unit.scale(0.5)), voidBlossom.position().add(VecUtils.yAxis.scale(5.0))), level, voidBlossom.position());
                });
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)){
            destroy(level, pos, state);
            return Blocks.AIR.defaultBlockState();
        }else
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return canSupportCenter(level, pos, Direction.UP) && !level.isWaterAt(pos);
    }

    @Override
    public void destroy(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        for(int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for(int y = -1; y <= 1; y ++) {
                    if((x != 0 || z != 0)) {
                        BlockPos pos1 = pos.offset(x, y, z);
                        if(level.getBlockState(pos1).getBlock() == RegistroFlorVazio.VINE_WALL.get()) {
                            level.scheduleTick(pos1, RegistroFlorVazio.VINE_WALL.get(), (2 - y) * 20 + RandomUtils.range(0, 19));
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shape;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (level.isClientSide) {
            EfeitosBlocoFlorVazio.quebrar(pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
