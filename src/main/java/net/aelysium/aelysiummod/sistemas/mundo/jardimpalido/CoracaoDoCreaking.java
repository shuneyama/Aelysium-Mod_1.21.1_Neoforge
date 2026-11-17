package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import com.mojang.serialization.MapCodec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nullable;

public class CoracaoDoCreaking extends BaseEntityBlock {

    public static final MapCodec<CoracaoDoCreaking> CODEC = simpleCodec(CoracaoDoCreaking::new);
    public static final EnumProperty<Direction.Axis> EIXO = BlockStateProperties.AXIS;
    public static final EnumProperty<EstadoCoracao> ESTADO = EnumProperty.create("creaking_heart_state", EstadoCoracao.class);
    public static final TagKey<Block> TRONCOS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "pale_oak_logs"));

    public CoracaoDoCreaking(Properties propriedades) {
        super(propriedades);
        this.registerDefaultState(this.stateDefinition.any().setValue(EIXO, Direction.Axis.Y).setValue(ESTADO, EstadoCoracao.UPROOTED));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(EIXO, ESTADO);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        Direction.Axis eixo = contexto.getClickedFace().getAxis();
        return this.defaultBlockState().setValue(EIXO, eixo).setValue(ESTADO, calcular(contexto.getLevel(), contexto.getClickedPos(), eixo));
    }

    @Override
    protected BlockState rotate(BlockState estado, Rotation rotacao) {
        return RotatedPillarBlock.rotatePillar(estado, rotacao);
    }

    @Override
    protected RenderShape getRenderShape(BlockState estado) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState estado) {
        return new CoracaoDoCreakingEntidade(pos, estado);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level mundo, BlockState estado, BlockEntityType<T> tipo) {
        return createTickerHelper(tipo, ModJardimPalido.TIPO_CORACAO.get(), CoracaoDoCreakingEntidade::tick);
    }

    public static boolean noite(Level mundo) {
        return mundo.dimensionType().natural() && mundo.isNight();
    }

    public static EstadoCoracao calcular(Level mundo, BlockPos pos, Direction.Axis eixo) {
        Direction direcao = Direction.get(Direction.AxisDirection.POSITIVE, eixo);
        if (!mundo.getBlockState(pos.relative(direcao)).is(TRONCOS) || !mundo.getBlockState(pos.relative(direcao.getOpposite())).is(TRONCOS)) {
            return EstadoCoracao.UPROOTED;
        }
        return noite(mundo) ? EstadoCoracao.AWAKE : EstadoCoracao.DORMANT;
    }
}
