package net.aelysium.aelysiummod.sistemas.mundo.porta;

import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteAbrirPainelSenha;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem;
import net.aelysium.aelysiummod.registro.ModEntidadesBloco;

public class BlocoPortaSenha extends DoorBlock implements EntityBlock {

    public BlocoPortaSenha(Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(this) || baseState.getValue(OPEN)) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(basePos) instanceof EntidadeBlocoPortaSenha be)) return InteractionResult.SUCCESS;

        if (CartaoItem.temCartaoValido(player, be.getId())) {
            abrirPorta(level, basePos, baseState);
            return InteractionResult.SUCCESS;
        }

        boolean definindo = be.getSenha().isEmpty();
        PacketDistributor.sendToPlayer(serverPlayer, new PacoteAbrirPainelSenha(basePos, definindo));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
    }

    public void abrirPorta(Level level, BlockPos basePos, BlockState baseState) {
        level.setBlock(basePos, baseState.setValue(OPEN, true), 10);
        BlockPos topPos = basePos.above();
        BlockState topState = level.getBlockState(topPos);
        if (topState.is(this)) level.setBlock(topPos, topState.setValue(OPEN, true), 10);

        level.playSound(null, basePos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.BLOCK_OPEN, basePos);

        if (level.getBlockEntity(basePos) instanceof EntidadeBlocoPortaSenha be) {
            be.iniciarFechamento(30);
        }
    }

    public void fecharPorta(Level level, BlockPos basePos, BlockState baseState) {
        BlockState currentBase = level.getBlockState(basePos);
        if (!currentBase.is(this) || !currentBase.getValue(OPEN)) return;

        level.setBlock(basePos, currentBase.setValue(OPEN, false), 10);
        BlockPos topPos = basePos.above();
        BlockState topState = level.getBlockState(topPos);
        if (topState.is(this)) level.setBlock(topPos, topState.setValue(OPEN, false), 10);

        level.playSound(null, basePos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, GameEvent.BLOCK_CLOSE, basePos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new EntidadeBlocoPortaSenha(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER || type != ModEntidadesBloco.PORTA_SENHA.get()) return null;
        return (lvl, pos, st, be) -> EntidadeBlocoPortaSenha.tick(lvl, pos, st, (EntidadeBlocoPortaSenha) be);
    }
}
