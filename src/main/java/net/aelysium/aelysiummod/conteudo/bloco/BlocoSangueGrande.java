package net.aelysium.aelysiummod.conteudo.bloco;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlocoSangueGrande extends BlocoSangue {

    public static final IntegerProperty PARTE = IntegerProperty.create("parte", 0, 3);

    public BlocoSangueGrande(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PARTE);
    }

    private static BlockPos deslocamento(int colunas, int linhas, Direction face) {
        int x = colunas, y = 0, z = linhas;
        int rotX = 0, rotY = 0;
        switch (face) {
            case DOWN -> rotX = 2;
            case NORTH -> rotX = 1;
            case EAST -> { rotX = 1; rotY = 1; }
            case SOUTH -> { rotX = 1; rotY = 2; }
            case WEST -> { rotX = 1; rotY = 3; }
            default -> { }
        }
        for (int i = 0; i < rotX; i++) {
            int ny = z;
            int nz = -y;
            y = ny;
            z = nz;
        }
        for (int i = 0; i < rotY; i++) {
            int nx = -z;
            int nz = x;
            x = nx;
            z = nz;
        }
        return new BlockPos(x, y, z);
    }

    private static BlockPos posicaoDaParte(BlockPos pos, int parteAtual, int parteAlvo, Direction face) {
        return pos.offset(deslocamento(parteAlvo % 2 - parteAtual % 2, parteAlvo / 2 - parteAtual / 2, face));
    }

    private BlockState estadoDaParte(Direction face, int rotacao, int parte) {
        return defaultBlockState().setValue(FACING, face).setValue(ROTACAO, rotacao).setValue(PARTE, parte);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        int rotacao = face.getAxis().isVertical() ? context.getHorizontalDirection().get2DDataValue() : 0;
        BlockPos clicada = context.getClickedPos();
        Level level = context.getLevel();

        for (int parte = 0; parte < 4; parte++) {
            if (cabe(level, clicada, face, rotacao, parte)) return estadoDaParte(face, rotacao, parte);
        }
        return null;
    }

    private boolean cabe(Level level, BlockPos clicada, Direction face, int rotacao, int parteClicada) {
        for (int parte = 0; parte < 4; parte++) {
            BlockPos pos = posicaoDaParte(clicada, parteClicada, parte, face);
            if (parte != parteClicada) {
                BlockState existente = level.getBlockState(pos);
                if (!existente.canBeReplaced() || existente.getBlock() instanceof BlocoSangue) return false;
            }
            if (!estadoDaParte(face, rotacao, parte).canSurvive(level, pos)) return false;
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;

        Direction face = state.getValue(FACING);
        int rotacao = state.getValue(ROTACAO);
        int parteAtual = state.getValue(PARTE);
        for (int parte = 0; parte < 4; parte++) {
            if (parte == parteAtual) continue;
            level.setBlock(posicaoDaParte(pos, parteAtual, parte, face), estadoDaParte(face, rotacao, parte), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            Direction face = state.getValue(FACING);
            int rotacao = state.getValue(ROTACAO);
            int parteAtual = state.getValue(PARTE);
            for (int parte = 0; parte < 4; parte++) {
                if (parte == parteAtual) continue;
                BlockPos outra = posicaoDaParte(pos, parteAtual, parte, face);
                if (level.getBlockState(outra).equals(estadoDaParte(face, rotacao, parte))) {
                    level.setBlock(outra, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Direction face = state.getValue(FACING);
        int parteAtual = state.getValue(PARTE);
        int novaRotacao = (state.getValue(ROTACAO) + 1) % 4;
        for (int parte = 0; parte < 4; parte++) {
            BlockPos outra = posicaoDaParte(pos, parteAtual, parte, face);
            BlockState estado = level.getBlockState(outra);
            if (estado.is(this) && estado.getValue(PARTE) == parte) {
                level.setBlock(outra, estado.setValue(ROTACAO, novaRotacao), Block.UPDATE_ALL);
            }
        }
        return InteractionResult.CONSUME;
    }
}
