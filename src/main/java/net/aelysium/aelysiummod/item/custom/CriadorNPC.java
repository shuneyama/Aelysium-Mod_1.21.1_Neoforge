package net.aelysium.aelysiummod.item.custom;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
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
            CustomNpcEntity npc = new CustomNpcEntity(level,
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
                    Component.literal("NPC criado com sucesso!"), true);
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
        tooltipComponents.add(Component.literal("§7Clique no chão para criar um NPC"));
        tooltipComponents.add(Component.literal("§7Clique em um NPC para editar"));
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}
