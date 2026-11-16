package net.aelysium.aelysiummod.sistemas.mundo.portal;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BlocoPortalLar extends Block implements Portal {

    public static final MapCodec<BlocoPortalLar> CODEC = simpleCodec(BlocoPortalLar::new);

    public static final ResourceKey<Level> LAR_DOS_QUE_NAO_VAO = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("aelysium", "lar_dos_que_nao_vao"));

    private static final VoxelShape SHAPE = Block.box(0.0, 8.0, 0.0, 16.0, 9.0, 16.0);

    public BlocoPortalLar(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlocoPortalLar> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> destino = level.dimension() == LAR_DOS_QUE_NAO_VAO ? Level.OVERWORLD : LAR_DOS_QUE_NAO_VAO;
        ServerLevel destinoLevel = level.getServer().getLevel(destino);
        if (destinoLevel == null) return null;

        return new DimensionTransition(destinoLevel, entity, DimensionTransition.PLAY_PORTAL_SOUND);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(corAleatoria(random), x, y, z,
                    (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3);
        }
    }

    public static DustParticleOptions corAleatoria(RandomSource random) {
        Vector3f cor = random.nextBoolean()
                ? new Vector3f(0.85F, 0.08F, 0.08F)
                : new Vector3f(1.0F, 1.0F, 1.0F);
        return new DustParticleOptions(cor, 1.3F);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }
}
