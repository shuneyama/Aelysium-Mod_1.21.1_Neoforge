package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityTick;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

public class LightBlockPlacer implements IEntityTick<ServerLevel> {
    private final Entity entity;

    public LightBlockPlacer(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void tick(ServerLevel level) {
        if (!level.getBlockState(entity.blockPosition()).is(Blocks.LIGHT))
            level.setBlockAndUpdate(entity.blockPosition(), Blocks.LIGHT.defaultBlockState());
    }
}
