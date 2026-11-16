package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class DecorHorizontalLuz extends DecorHorizontal {
    public DecorHorizontalLuz(Properties propriedades, String id, int apoio, boolean semColisao) {
        super(propriedades, id, apoio, semColisao);
        registerDefaultState(defaultBlockState().setValue(ACESA, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        super.createBlockStateDefinition(construtor);
        construtor.add(ACESA);
    }
}
