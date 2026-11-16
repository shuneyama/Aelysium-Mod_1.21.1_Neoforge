package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class BlocoConcha extends DecorHorizontal {
    public static final BooleanProperty ABERTA = BlockStateProperties.OPEN;
    public static final BooleanProperty PEROLA = BooleanProperty.create("perola");
    public static final BooleanProperty ENERGIZADA = BlockStateProperties.POWERED;

    private final Supplier<Item> itemPerola;

    public BlocoConcha(Properties propriedades, String id, Supplier<Item> itemPerola) {
        super(propriedades, id, APOIO_NENHUM, false);
        this.itemPerola = itemPerola;
        registerDefaultState(defaultBlockState().setValue(ABERTA, false).setValue(PEROLA, false).setValue(ENERGIZADA, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        super.createBlockStateDefinition(construtor);
        construtor.add(ABERTA, PEROLA, ENERGIZADA);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState estado, Level nivel, BlockPos pos, Player jogador, InteractionHand mao, BlockHitResult acerto) {
        if (item.is(Items.SAND) && estado.getValue(ABERTA) && !estado.getValue(PEROLA)) {
            if (!nivel.isClientSide) {
                nivel.setBlock(pos, estado.setValue(PEROLA, true), 3);
                nivel.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!jogador.getAbilities().instabuild) {
                    item.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(nivel.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState estado, Level nivel, BlockPos pos, Player jogador, BlockHitResult acerto) {
        alternar(nivel, pos, estado, !estado.getValue(ABERTA));
        return InteractionResult.sidedSuccess(nivel.isClientSide);
    }

    @Override
    protected void neighborChanged(BlockState estado, Level nivel, BlockPos pos, Block bloco, BlockPos posOrigem, boolean movendo) {
        if (nivel.isClientSide) {
            return;
        }
        boolean sinal = nivel.hasNeighborSignal(pos);
        if (sinal == estado.getValue(ENERGIZADA)) {
            return;
        }
        BlockState novo = estado.setValue(ENERGIZADA, sinal);
        if (sinal) {
            if (!estado.getValue(ABERTA)) {
                novo = novo.setValue(ABERTA, true);
                nivel.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 1.1F);
            }
            if (novo.getValue(PEROLA)) {
                novo = novo.setValue(PEROLA, false);
                Block.popResource(nivel, pos, new ItemStack(itemPerola.get()));
            }
        } else if (estado.getValue(ABERTA)) {
            novo = novo.setValue(ABERTA, false);
            nivel.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.8F, 1.1F);
        }
        nivel.setBlock(pos, novo, 3);
    }

    private void alternar(Level nivel, BlockPos pos, BlockState estado, boolean abrir) {
        nivel.setBlock(pos, estado.setValue(ABERTA, abrir), 3);
        nivel.playSound(null, pos, abrir ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.8F, 1.1F);
    }
}
