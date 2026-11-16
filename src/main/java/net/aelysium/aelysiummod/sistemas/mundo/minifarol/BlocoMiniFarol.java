package net.aelysium.aelysiummod.sistemas.mundo.minifarol;

import com.mojang.serialization.MapCodec;
import net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede.PacoteAbrirMiniFarol;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

public class BlocoMiniFarol extends BaseEntityBlock {

    public static final MapCodec<BlocoMiniFarol> CODEC = simpleCodec(BlocoMiniFarol::new);

    private static final VoxelShape FORMA = Block.box(6.4, 0.0, 6.4, 9.6, 3.2, 9.6);

    public BlocoMiniFarol(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FORMA;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntidadeMiniFarol(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.hasPermissions(2)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof EntidadeMiniFarol be) {
            PacketDistributor.sendToPlayer(serverPlayer, new PacoteAbrirMiniFarol(pos, be.isAtivo(), be.getCor()));
        }
        return InteractionResult.CONSUME;
    }
}
