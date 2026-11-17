package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlocosJardimPalido {

    private BlocosJardimPalido() {
    }

    public static class FolhasCarvalhoPalido extends LeavesBlock {
        public static final MapCodec<FolhasCarvalhoPalido> CODEC = simpleCodec(FolhasCarvalhoPalido::new);

        public FolhasCarvalhoPalido(Properties propriedades) {
            super(propriedades);
        }

        @Override
        public MapCodec<? extends LeavesBlock> codec() {
            return CODEC;
        }

        @Override
        public void animateTick(BlockState estado, Level mundo, BlockPos pos, RandomSource aleatorio) {
            super.animateTick(estado, mundo, pos, aleatorio);
            if (aleatorio.nextInt(10) != 0) return;
            BlockPos abaixo = pos.below();
            BlockState estadoAbaixo = mundo.getBlockState(abaixo);
            if (!isFaceFull(estadoAbaixo.getCollisionShape(mundo, abaixo), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(mundo, pos, aleatorio, ModJardimPalido.PARTICULA_FOLHAS.get());
            }
        }
    }

    public static class GrumoResina extends MultifaceBlock {
        public static final MapCodec<GrumoResina> CODEC = simpleCodec(GrumoResina::new);
        private final MultifaceSpreader espalhador = new MultifaceSpreader(this);

        public GrumoResina(Properties propriedades) {
            super(propriedades);
        }

        @Override
        protected MapCodec<GrumoResina> codec() {
            return CODEC;
        }

        @Override
        public MultifaceSpreader getSpreader() {
            return espalhador;
        }
    }

    public static class MusgoPendente extends Block {
        public static final MapCodec<MusgoPendente> CODEC = simpleCodec(MusgoPendente::new);
        public static final BooleanProperty PONTA = BooleanProperty.create("tip");
        private static final VoxelShape FORMA = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

        public MusgoPendente(Properties propriedades) {
            super(propriedades);
            this.registerDefaultState(this.stateDefinition.any().setValue(PONTA, true));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
            construtor.add(PONTA);
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            return FORMA;
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext contexto) {
            BlockState estado = this.defaultBlockState();
            return estado.setValue(PONTA, !contexto.getLevel().getBlockState(contexto.getClickedPos().below()).is(this));
        }

        @Override
        protected boolean canSurvive(BlockState estado, LevelReader mundo, BlockPos pos) {
            BlockPos acima = pos.above();
            BlockState estadoAcima = mundo.getBlockState(acima);
            return estadoAcima.is(this) || estadoAcima.isFaceSturdy(mundo, acima, Direction.DOWN);
        }

        @Override
        protected BlockState updateShape(BlockState estado, Direction direcao, BlockState vizinho, LevelAccessor mundo, BlockPos pos, BlockPos posVizinho) {
            if (!this.canSurvive(estado, mundo, pos)) return Blocks.AIR.defaultBlockState();
            if (direcao == Direction.DOWN) return estado.setValue(PONTA, !vizinho.is(this));
            return estado;
        }
    }

    public static class FlorDeOlho extends FlowerBlock {
        public static final MapCodec<FlorDeOlho> CODEC = simpleCodec(propriedades -> new FlorDeOlho(false, propriedades));
        private final boolean aberta;

        public FlorDeOlho(boolean aberta, Properties propriedades) {
            super(aberta ? MobEffects.BLINDNESS : MobEffects.CONFUSION, aberta ? 11.0F : 7.0F, propriedades);
            this.aberta = aberta;
        }

        @Override
        public MapCodec<? extends FlowerBlock> codec() {
            return CODEC;
        }

        @Override
        protected void randomTick(BlockState estado, ServerLevel mundo, BlockPos pos, RandomSource aleatorio) {
            if (!mundo.dimensionType().natural()) return;
            boolean noite = mundo.isNight();
            if (noite == aberta) return;
            Block alvo = noite ? ModJardimPalido.FLOR_OLHO_ABERTA.get() : ModJardimPalido.FLOR_OLHO_FECHADA.get();
            mundo.setBlockAndUpdate(pos, alvo.defaultBlockState());
        }
    }
}
