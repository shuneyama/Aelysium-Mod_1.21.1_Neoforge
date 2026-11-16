package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BlocoColuna extends BlocoDecorativoBase {
    public static final EnumProperty<ParteEmpilhada> PARTE = EnumProperty.create("parte", ParteEmpilhada.class);

    public BlocoColuna(Properties propriedades, String id) {
        super(propriedades, id, APOIO_NENHUM, false);
        registerDefaultState(defaultBlockState().setValue(PARTE, ParteEmpilhada.SOLO));
    }

    @Override
    protected DirectionProperty direcao() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        super.createBlockStateDefinition(construtor);
        construtor.add(PARTE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        BlockState estado = super.getStateForPlacement(contexto);
        return estado == null ? null : estado.setValue(PARTE, ParteEmpilhada.calcular(contexto.getLevel(), contexto.getClickedPos(), this));
    }

    @Override
    protected BlockState updateShape(BlockState estado, Direction direcao, BlockState vizinho, LevelAccessor nivel, BlockPos pos, BlockPos posVizinha) {
        BlockState atualizado = super.updateShape(estado, direcao, vizinho, nivel, pos, posVizinha);
        if (atualizado.is(this) && direcao.getAxis().isVertical()) {
            return atualizado.setValue(PARTE, ParteEmpilhada.calcular(nivel, pos, this));
        }
        return atualizado;
    }
}
