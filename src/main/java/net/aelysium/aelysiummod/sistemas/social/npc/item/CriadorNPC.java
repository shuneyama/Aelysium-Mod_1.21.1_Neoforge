package net.aelysium.aelysiummod.sistemas.social.npc.item;

import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class CriadorNPC extends Item {

    public CriadorNPC(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;
        if (!player.hasPermissions(2) || !player.isCreative()) return InteractionResult.PASS;

        if (!level.isClientSide) {
            BlockPos pos = context.getClickedPos().above();
            EntidadeNpcPersonalizado npc = new EntidadeNpcPersonalizado(level,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

            float yaw = (float) Math.toDegrees(Math.atan2(
                    player.getZ() - npc.getZ(),
                    player.getX() - npc.getX()
            )) - 90.0F;
            npc.setYRot(yaw);
            npc.setYHeadRot(yaw);
            npc.setYBodyRot(yaw);

            level.addFreshEntity(npc);
            player.displayClientMessage(
                    Component.translatable("mensagem.aelysiummod.npc.npc_criado_com_sucesso"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.npc.clique_no_chao_para_criar_um_npc"));
        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.npc.clique_em_um_npc_para_editar"));
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}
