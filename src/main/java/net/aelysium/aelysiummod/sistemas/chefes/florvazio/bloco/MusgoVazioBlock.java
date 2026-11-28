package net.aelysium.aelysiummod.sistemas.chefes.florvazio.bloco;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MusgoVazioBlock extends Block {

    public MusgoVazioBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return VoidBlossomBlock.progressoQuebra(player);
    }
}
