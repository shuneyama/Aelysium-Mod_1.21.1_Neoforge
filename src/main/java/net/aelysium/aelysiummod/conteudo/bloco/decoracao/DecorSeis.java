package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class DecorSeis extends BlocoDecorativoBase {
    public static final DirectionProperty DIRECAO = BlockStateProperties.FACING;

    public DecorSeis(Properties propriedades, String id, int apoio, boolean semColisao) {
        super(propriedades, id, apoio, semColisao);
        registerDefaultState(defaultBlockState().setValue(DIRECAO, Direction.NORTH));
    }

    @Override
    protected DirectionProperty direcao() {
        return DIRECAO;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        super.createBlockStateDefinition(construtor);
        construtor.add(DIRECAO);
    }
}
