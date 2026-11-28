package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EntityBlockSwapper extends Entity {
    private static final EntityDataAccessor<BlockState> ORIG_BLOCK_STATE = SynchedEntityData.defineId(EntityBlockSwapper.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Integer> RESTORE_TIME = SynchedEntityData.defineId(EntityBlockSwapper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> POS = SynchedEntityData.defineId(EntityBlockSwapper.class, EntityDataSerializers.BLOCK_POS);
    protected int duration;
    protected boolean breakParticlesEnd;
    private BlockPos pos;

    public EntityBlockSwapper(EntityType<? extends EntityBlockSwapper> type, Level world) {
        super(type, world);
        breakParticlesEnd = false;
    }

    public EntityBlockSwapper(EntityType<? extends EntityBlockSwapper> type, Level world, BlockPos pos, BlockState newBlock, int duration, boolean breakParticlesStart, boolean breakParticlesEnd) {
        super(type, world);
        setStorePos(pos);
        setRestoreTime(duration);
        this.breakParticlesEnd = breakParticlesEnd;
        setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (!world.isClientSide) {
            setOrigBlock(world.getBlockState(pos));
            if (breakParticlesStart) world.destroyBlock(pos, false);
            world.setBlock(pos, newBlock, 19);
        }
        List<EntityBlockSwapper> swappers = world.getEntitiesOfClass(EntityBlockSwapper.class, getBoundingBox());
        for (EntityBlockSwapper swapper : swappers) {
            if (swapper == this) continue;
            setOrigBlock(swapper.getOrigBlock());
            swapper.discard();
        }
    }

    public static void swapBlock(Level world, BlockPos pos, BlockState newBlock, int duration, boolean breakParticlesStart, boolean breakParticlesEnd) {
        if (!world.isClientSide) {
            EntityBlockSwapper swapper = new EntityBlockSwapper(RegistroFrostmaw.BLOCK_SWAPPER.get(), world, pos, newBlock, duration, breakParticlesStart, breakParticlesEnd);
            world.addFreshEntity(swapper);
        }
    }

    public boolean isBlockPosInsideSwapper(BlockPos pos) {
        return pos.equals(getStorePos());
    }

    @Override
    public boolean shouldRender(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
        return false;
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        builder.define(ORIG_BLOCK_STATE, Blocks.DIRT.defaultBlockState());
        builder.define(RESTORE_TIME, 20);
        builder.define(POS, new BlockPos(0, 0, 0));
    }

    public int getRestoreTime() {
        return entityData.get(RESTORE_TIME);
    }

    public void setRestoreTime(int restoreTime) {
        entityData.set(RESTORE_TIME, restoreTime);
        duration = restoreTime;
    }

    public BlockPos getStorePos() {
        return entityData.get(POS);
    }

    public void setStorePos(BlockPos bpos) {
        entityData.set(POS, bpos);
        pos = bpos;
    }

    @Nullable
    public BlockState getOrigBlock() {
        return getEntityData().get(ORIG_BLOCK_STATE);
    }

    public void setOrigBlock(BlockState block) {
        getEntityData().set(ORIG_BLOCK_STATE, block);
    }

    public void restoreBlock() {
        List<EntityBlockSwapper> swappers = level().getEntitiesOfClass(EntityBlockSwapper.class, getBoundingBox());
        if (!level().isClientSide) {
            boolean canReplace = true;
            for (EntityBlockSwapper swapper : swappers) {
                if (swapper == this) continue;
                if (swapper.isBlockPosInsideSwapper(pos)) {
                    canReplace = false;
                    break;
                }
            }
            if (canReplace) {
                if (breakParticlesEnd) level().destroyBlock(pos, false);
                level().setBlock(pos, getOrigBlock(), 19);
            }
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (canRestoreBlock()) restoreBlock();
    }

    protected boolean canRestoreBlock() {
        return tickCount > duration && level().getEntitiesOfClass(Player.class, getBoundingBox()).isEmpty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        BlockState blockState = getEntityData().get(ORIG_BLOCK_STATE);
        compound.put("block", NbtUtils.writeBlockState(blockState));
        compound.putInt("restoreTime", getRestoreTime());
        compound.putInt("storePosX", getStorePos().getX());
        compound.putInt("storePosY", getStorePos().getY());
        compound.putInt("storePosZ", getStorePos().getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        Tag blockNBT = compound.get("block");
        if (blockNBT != null) {
            BlockState blockState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), (CompoundTag) blockNBT);
            setOrigBlock(blockState);
        }
        setRestoreTime(compound.getInt("restoreTime"));
        setStorePos(new BlockPos(
                compound.getInt("storePosX"),
                compound.getInt("storePosY"),
                compound.getInt("storePosZ")
        ));
    }

}
