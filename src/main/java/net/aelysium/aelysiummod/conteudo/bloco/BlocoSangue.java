package net.aelysium.aelysiummod.conteudo.bloco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlocoSangue extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty ROTACAO = IntegerProperty.create("rotacao", 0, 3);

    private static final VoxelShape CIMA = Block.box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape BAIXO = Block.box(0, 15, 0, 16, 16, 16);
    private static final VoxelShape NORTE = Block.box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape SUL = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape LESTE = Block.box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape OESTE = Block.box(15, 0, 0, 16, 16, 16);

    public BlocoSangue(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ROTACAO, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTACAO);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> CIMA;
            case DOWN -> BAIXO;
            case NORTH -> NORTE;
            case SOUTH -> SUL;
            case EAST -> LESTE;
            case WEST -> OESTE;
        };
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (context.getItemInHand().getItem() instanceof BlockItem item && item.getBlock() instanceof BlocoSangue) return false;
        return super.canBeReplaced(state, context);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction normal = state.getValue(FACING);
        BlockPos apoio = pos.relative(normal.getOpposite());
        return level.getBlockState(apoio).isFaceSturdy(level, apoio, normal);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction normal = context.getClickedFace();
        int rotacao = normal.getAxis().isVertical() ? context.getHorizontalDirection().get2DDataValue() : 0;
        BlockState state = defaultBlockState().setValue(FACING, normal).setValue(ROTACAO, rotacao);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        level.setBlock(pos, state.cycle(ROTACAO), Block.UPDATE_ALL);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
