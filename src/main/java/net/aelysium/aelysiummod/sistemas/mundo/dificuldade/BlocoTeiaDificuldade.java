package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlocoTeiaDificuldade extends WebBlock {

    public BlocoTeiaDificuldade(Properties propriedades) {
        super(propriedades);
    }

    @Override
    public void onPlace(BlockState estado, Level mundo, BlockPos pos, BlockState estadoAntigo, boolean movido) {
        super.onPlace(estado, mundo, pos, estadoAntigo, movido);
        if (!mundo.isClientSide()) {
            mundo.scheduleTick(pos, this, PerfilDificuldade.duracaoTeiaTicks());
        }
    }

    @Override
    public void tick(BlockState estado, ServerLevel mundo, BlockPos pos, RandomSource aleatorio) {
        mundo.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    @Override
    protected void entityInside(BlockState estado, Level mundo, BlockPos pos, Entity entidade) {
        if (entidade instanceof Spider) return;
        super.entityInside(estado, mundo, pos, entidade);
    }
}
