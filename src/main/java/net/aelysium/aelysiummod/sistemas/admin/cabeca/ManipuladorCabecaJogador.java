package net.aelysium.aelysiummod.sistemas.admin.cabeca;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.cabeca.rede.PacoteAbrirCabeca;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorCabecaJogador {

    public static SkullBlockEntity cabecaDeJogador(Level level, BlockPos pos) {
        BlockState estado = level.getBlockState(pos);
        if (!(estado.getBlock() instanceof AbstractSkullBlock cabeca)) return null;
        if (cabeca.getType() != SkullBlock.Types.PLAYER) return null;
        return level.getBlockEntity(pos) instanceof SkullBlockEntity be ? be : null;
    }

    @SubscribeEvent
    public static void aoClicarNaCabeca(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (!jogador.hasPermissions(2)) return;
        if (!jogador.getMainHandItem().isEmpty()) return;

        SkullBlockEntity cabeca = cabecaDeJogador(event.getLevel(), event.getPos());
        if (cabeca == null) return;

        String atual = cabeca.getOwnerProfile() != null ? cabeca.getOwnerProfile().name().orElse("") : "";
        PacketDistributor.sendToPlayer(jogador, new PacoteAbrirCabeca(event.getPos(), atual));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
