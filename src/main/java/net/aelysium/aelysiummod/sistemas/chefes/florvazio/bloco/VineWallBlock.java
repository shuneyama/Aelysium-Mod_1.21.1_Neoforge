package net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class VineWallBlock extends IronBarsBlock {
    public VineWallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        level.destroyBlock(pos, false);
    }
}
