package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlocoVidroEixo extends RotatedPillarBlock {
    public BlocoVidroEixo(Properties propriedades) {
        super(propriedades);
    }

    @Override
    protected boolean skipRendering(BlockState estado, BlockState vizinho, Direction direcao) {
        return vizinho.is(this) || super.skipRendering(estado, vizinho, direcao);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState estado, BlockGetter nivel, BlockPos pos, CollisionContext contexto) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState estado, BlockGetter nivel, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState estado, BlockGetter nivel, BlockPos pos) {
        return true;
    }
}
