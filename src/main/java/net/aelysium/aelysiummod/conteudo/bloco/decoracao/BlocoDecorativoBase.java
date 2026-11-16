package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BlocoDecorativoBase extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty AGUA = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty ACESA = BlockStateProperties.LIT;

    public static final int APOIO_NENHUM = 0;
    public static final int APOIO_COSTAS = 1;
    public static final int APOIO_FRENTE = 2;
    public static final int APOIO_ABAIXO = 3;
    public static final int APOIO_ACIMA = 4;

    private final String id;
    private final int apoio;
    private final boolean semColisao;
    private final Map<BlockState, VoxelShape> formas = new ConcurrentHashMap<>();

    protected BlocoDecorativoBase(Properties propriedades, String id, int apoio, boolean semColisao) {
        super(propriedades);
        this.id = id;
        this.apoio = apoio;
        this.semColisao = semColisao;
        registerDefaultState(defaultBlockState().setValue(AGUA, false));
    }

    protected abstract DirectionProperty direcao();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(AGUA);
    }

    @Override
    protected VoxelShape getShape(BlockState estado, BlockGetter nivel, BlockPos pos, CollisionContext contexto) {
        return formas.computeIfAbsent(estado, e -> FormasDecoracao.obter(id, e));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState estado, BlockGetter nivel, BlockPos pos, CollisionContext contexto) {
        return semColisao ? Shapes.empty() : getShape(estado, nivel, pos, contexto);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState estado, BlockGetter nivel, BlockPos pos) {
        return estado.getFluidState().isEmpty();
    }

    @Override
    protected FluidState getFluidState(BlockState estado) {
        return estado.getValue(AGUA) ? Fluids.WATER.getSource(false) : super.getFluidState(estado);
    }

    protected Direction ladoApoio(BlockState estado) {
        DirectionProperty direcao = direcao();
        return switch (apoio) {
            case APOIO_COSTAS -> direcao == null ? null : estado.getValue(direcao).getOpposite();
            case APOIO_FRENTE -> direcao == null ? null : estado.getValue(direcao);
            case APOIO_ABAIXO -> Direction.DOWN;
            case APOIO_ACIMA -> Direction.UP;
            default -> null;
        };
    }

    @Override
    protected boolean canSurvive(BlockState estado, LevelReader nivel, BlockPos pos) {
        Direction lado = ladoApoio(estado);
        if (lado == null) {
            return true;
        }
        BlockPos alvo = pos.relative(lado);
        return nivel.getBlockState(alvo).isFaceSturdy(nivel, alvo, lado.getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        FluidState fluido = contexto.getLevel().getFluidState(contexto.getClickedPos());
        BlockState base = defaultBlockState().setValue(AGUA, fluido.getType() == Fluids.WATER);
        DirectionProperty direcao = direcao();
        if (direcao == null) {
            return base.canSurvive(contexto.getLevel(), contexto.getClickedPos()) ? base : null;
        }
        boolean seis = direcao.getPossibleValues().contains(Direction.UP);
        if (apoio == APOIO_COSTAS || apoio == APOIO_FRENTE) {
            for (Direction olhar : contexto.getNearestLookingDirections()) {
                if (!seis && olhar.getAxis().isVertical()) {
                    continue;
                }
                Direction escolhida = apoio == APOIO_COSTAS ? olhar.getOpposite() : olhar;
                BlockState candidato = base.setValue(direcao, escolhida);
                if (candidato.canSurvive(contexto.getLevel(), contexto.getClickedPos())) {
                    return candidato;
                }
            }
            return null;
        }
        Direction escolhida = seis ? contexto.getNearestLookingDirection().getOpposite() : contexto.getHorizontalDirection().getOpposite();
        BlockState candidato = base.setValue(direcao, escolhida);
        return candidato.canSurvive(contexto.getLevel(), contexto.getClickedPos()) ? candidato : null;
    }

    @Override
    protected BlockState updateShape(BlockState estado, Direction direcao, BlockState vizinho, LevelAccessor nivel, BlockPos pos, BlockPos posVizinha) {
        if (estado.getValue(AGUA)) {
            nivel.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(nivel));
        }
        if (apoio != APOIO_NENHUM && !estado.canSurvive(nivel, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(estado, direcao, vizinho, nivel, pos, posVizinha);
    }

    @Override
    protected BlockState rotate(BlockState estado, Rotation rotacao) {
        DirectionProperty direcao = direcao();
        return direcao == null ? estado : estado.setValue(direcao, rotacao.rotate(estado.getValue(direcao)));
    }

    @Override
    protected BlockState mirror(BlockState estado, Mirror espelho) {
        DirectionProperty direcao = direcao();
        return direcao == null ? estado : estado.rotate(espelho.getRotation(estado.getValue(direcao)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState estado, Level nivel, BlockPos pos, Player jogador, BlockHitResult acerto) {
        if (!estado.hasProperty(ACESA)) {
            return InteractionResult.PASS;
        }
        BlockState novo = estado.cycle(ACESA);
        nivel.setBlock(pos, novo, 3);
        nivel.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.6F, novo.getValue(ACESA) ? 0.6F : 0.5F);
        return InteractionResult.sidedSuccess(nivel.isClientSide);
    }
}
