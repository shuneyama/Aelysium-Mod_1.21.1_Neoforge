package net.aelysium.aelysiummod.sistemas.mundo.flora;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlocosFlora {

    public static final TagKey<Block> SUPORTA_VEGETACAO_SECA = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("aelysiummod", "supports_dry_vegetation"));
    public static final TagKey<Block> SUPORTE_FLOR_CACTO = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("aelysiummod", "support_override_cactus_flower"));
    public static final TagKey<Block> COMESTIVEL_OVELHA = TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("aelysiummod", "edible_for_sheep"));

    private BlocosFlora() {
    }

    private static VoxelShape coluna(double largura, double base, double topo) {
        double metade = largura / 2.0D;
        return Block.box(8.0D - metade, base, 8.0D - metade, 8.0D + metade, topo, 8.0D + metade);
    }

    public static class Arbusto extends BushBlock implements BonemealableBlock {
        public static final MapCodec<Arbusto> CODEC = simpleCodec(Arbusto::new);
        private static final VoxelShape FORMA = coluna(16.0D, 0.0D, 13.0D);

        public Arbusto(Properties propriedades) {
            super(propriedades);
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        @Override
        protected boolean mayPlaceOn(BlockState estado, BlockGetter mundo, BlockPos pos) {
            return estado.is(BlockTags.DIRT) || estado.is(Blocks.FARMLAND);
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            return FORMA;
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader mundo, BlockPos pos, BlockState estado) {
            return Espalhar.possivel(mundo, pos, estado);
        }

        @Override
        public boolean isBonemealSuccess(Level mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            return true;
        }

        @Override
        public void performBonemeal(ServerLevel mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            Espalhar.encontrar(mundo, pos, estado).ifPresent(destino -> mundo.setBlockAndUpdate(destino, defaultBlockState()));
        }
    }

    public static class ArbustoVagalume extends BushBlock implements BonemealableBlock {
        public static final MapCodec<ArbustoVagalume> CODEC = simpleCodec(ArbustoVagalume::new);
        private static final VoxelShape FORMA = coluna(12.0D, 0.0D, 13.0D);

        public ArbustoVagalume(Properties propriedades) {
            super(propriedades);
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        @Override
        protected boolean mayPlaceOn(BlockState estado, BlockGetter mundo, BlockPos pos) {
            return estado.is(BlockTags.DIRT) || estado.is(Blocks.FARMLAND);
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            return FORMA;
        }

        @Override
        public void animateTick(BlockState estado, Level mundo, BlockPos pos, RandomSource aleatorio) {
            if (aleatorio.nextInt(30) == 0 && mundo.isNight()
                    && mundo.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) <= pos.getY()) {
                mundo.playLocalSound(pos, SonsExtras.FIREFLY_IDLE, SoundSource.AMBIENT, 1.0F, 1.0F, false);
            }
            if (mundo.getMaxLocalRawBrightness(pos) <= 13 && aleatorio.nextDouble() <= 0.7D) {
                double x = pos.getX() + aleatorio.nextDouble() * 10.0D - 5.0D;
                double y = pos.getY() + aleatorio.nextDouble() * 5.0D;
                double z = pos.getZ() + aleatorio.nextDouble() * 10.0D - 5.0D;
                mundo.addParticle(ModFlora.PARTICULA_VAGALUME.get(), x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader mundo, BlockPos pos, BlockState estado) {
            return Espalhar.possivel(mundo, pos, estado);
        }

        @Override
        public boolean isBonemealSuccess(Level mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            return true;
        }

        @Override
        public void performBonemeal(ServerLevel mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            Espalhar.encontrar(mundo, pos, estado).ifPresent(destino -> mundo.setBlockAndUpdate(destino, defaultBlockState()));
        }
    }

    public static class FlorSelvagem extends PinkPetalsBlock {
        public static final MapCodec<PinkPetalsBlock> CODEC = simpleCodec(FlorSelvagem::new);

        public FlorSelvagem(Properties propriedades) {
            super(propriedades);
        }

        @Override
        public MapCodec<PinkPetalsBlock> codec() {
            return CODEC;
        }

        @Override
        protected boolean mayPlaceOn(BlockState estado, BlockGetter mundo, BlockPos pos) {
            return estado.is(BlockTags.DIRT) || estado.is(Blocks.FARMLAND);
        }
    }

    public static class FolhasSecas extends BushBlock {
        public static final MapCodec<FolhasSecas> CODEC = simpleCodec(FolhasSecas::new);
        public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        public static final IntegerProperty SEGMENTOS = IntegerProperty.create("segment_amount", 1, 4);
        private static final VoxelShape[] QUADRANTES = {
                Block.box(0.0D, 0.0D, 0.0D, 8.0D, 1.0D, 8.0D),
                Block.box(8.0D, 0.0D, 0.0D, 16.0D, 1.0D, 8.0D),
                Block.box(8.0D, 0.0D, 8.0D, 16.0D, 1.0D, 16.0D),
                Block.box(0.0D, 0.0D, 8.0D, 8.0D, 1.0D, 16.0D)
        };
        private static final Direction[] ORDEM = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        public FolhasSecas(Properties propriedades) {
            super(propriedades);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SEGMENTOS, 1));
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        private static int indice(Direction direcao) {
            for (int i = 0; i < ORDEM.length; i++) {
                if (ORDEM[i] == direcao) return i;
            }
            return 0;
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            VoxelShape forma = Shapes.empty();
            Direction direcao = estado.getValue(FACING);
            int quantidade = estado.getValue(SEGMENTOS);
            for (int i = 0; i < quantidade; i++) {
                forma = Shapes.or(forma, QUADRANTES[indice(direcao)]);
                direcao = direcao.getCounterClockWise();
            }
            return forma.singleEncompassing();
        }

        @Override
        protected BlockState rotate(BlockState estado, Rotation rotacao) {
            return estado.setValue(FACING, rotacao.rotate(estado.getValue(FACING)));
        }

        @Override
        protected BlockState mirror(BlockState estado, Mirror espelho) {
            return estado.rotate(espelho.getRotation(estado.getValue(FACING)));
        }

        @Override
        protected boolean canBeReplaced(BlockState estado, BlockPlaceContext contexto) {
            return !contexto.isSecondaryUseActive() && contexto.getItemInHand().is(asItem()) && estado.getValue(SEGMENTOS) < 4
                    || super.canBeReplaced(estado, contexto);
        }

        @Override
        protected boolean canSurvive(BlockState estado, LevelReader mundo, BlockPos pos) {
            BlockPos abaixo = pos.below();
            return mundo.getBlockState(abaixo).isFaceSturdy(mundo, abaixo, Direction.UP);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext contexto) {
            BlockState existente = contexto.getLevel().getBlockState(contexto.getClickedPos());
            if (existente.is(this)) {
                return existente.setValue(SEGMENTOS, Math.min(4, existente.getValue(SEGMENTOS) + 1));
            }
            return defaultBlockState().setValue(FACING, contexto.getHorizontalDirection().getOpposite());
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
            construtor.add(FACING, SEGMENTOS);
        }
    }

    public static class GramaSecaBaixa extends BushBlock implements BonemealableBlock {
        public static final MapCodec<GramaSecaBaixa> CODEC = simpleCodec(GramaSecaBaixa::new);
        private static final VoxelShape FORMA = coluna(12.0D, 0.0D, 10.0D);

        public GramaSecaBaixa(Properties propriedades) {
            super(propriedades);
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        @Override
        protected boolean mayPlaceOn(BlockState estado, BlockGetter mundo, BlockPos pos) {
            return estado.is(SUPORTA_VEGETACAO_SECA);
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            return FORMA;
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader mundo, BlockPos pos, BlockState estado) {
            return true;
        }

        @Override
        public boolean isBonemealSuccess(Level mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            return true;
        }

        @Override
        public void performBonemeal(ServerLevel mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            mundo.setBlockAndUpdate(pos, ModFlora.GRAMA_SECA_ALTA.get().defaultBlockState());
        }
    }

    public static class GramaSecaAlta extends BushBlock implements BonemealableBlock {
        public static final MapCodec<GramaSecaAlta> CODEC = simpleCodec(GramaSecaAlta::new);
        private static final VoxelShape FORMA = coluna(14.0D, 0.0D, 16.0D);

        public GramaSecaAlta(Properties propriedades) {
            super(propriedades);
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        @Override
        protected boolean mayPlaceOn(BlockState estado, BlockGetter mundo, BlockPos pos) {
            return estado.is(SUPORTA_VEGETACAO_SECA);
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            return FORMA;
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader mundo, BlockPos pos, BlockState estado) {
            return Espalhar.possivel(mundo, pos, ModFlora.GRAMA_SECA_BAIXA.get().defaultBlockState());
        }

        @Override
        public boolean isBonemealSuccess(Level mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            return true;
        }

        @Override
        public void performBonemeal(ServerLevel mundo, RandomSource aleatorio, BlockPos pos, BlockState estado) {
            BlockState baixa = ModFlora.GRAMA_SECA_BAIXA.get().defaultBlockState();
            Espalhar.encontrar(mundo, pos, baixa).ifPresent(destino -> mundo.setBlockAndUpdate(destino, baixa));
        }
    }

    public static class FlorDeCacto extends BushBlock {
        public static final MapCodec<FlorDeCacto> CODEC = simpleCodec(FlorDeCacto::new);
        private static final VoxelShape FORMA = coluna(14.0D, 0.0D, 12.0D);

        public FlorDeCacto(Properties propriedades) {
            super(propriedades);
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return CODEC;
        }

        @Override
        protected VoxelShape getShape(BlockState estado, BlockGetter mundo, BlockPos pos, CollisionContext contexto) {
            return FORMA;
        }

        @Override
        protected boolean mayPlaceOn(BlockState estado, BlockGetter mundo, BlockPos pos) {
            return estado.is(SUPORTE_FLOR_CACTO) || estado.isFaceSturdy(mundo, pos, Direction.UP, SupportType.CENTER);
        }
    }
}
