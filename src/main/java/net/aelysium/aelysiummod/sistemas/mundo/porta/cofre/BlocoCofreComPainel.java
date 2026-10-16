package net.aelysium.aelysiummod.sistemas.mundo.porta.cofre;

import com.mojang.serialization.MapCodec;
import net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem;
import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteAbrirPainelSenha;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class BlocoCofreComPainel extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<BlocoCofreComPainel> CODEC = simpleCodec(BlocoCofreComPainel::new);

    public BlocoCofreComPainel(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<BlocoCofreComPainel> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof EntidadeBlocoCofreComPainel be)) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        if (CartaoItem.temCartaoValido(player, be.getId())) {
            be.onAcessoConcedido(level, pos, serverPlayer);
            return InteractionResult.SUCCESS;
        }

        boolean definindo = be.getSenha().isEmpty();
        PacketDistributor.sendToPlayer(serverPlayer, new PacoteAbrirPainelSenha(pos, definindo));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!movedByPiston && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof EntidadeBlocoCofreComPainel be) {
                Containers.dropContents(level, pos, be);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntidadeBlocoCofreComPainel(pos, state);
    }
}
