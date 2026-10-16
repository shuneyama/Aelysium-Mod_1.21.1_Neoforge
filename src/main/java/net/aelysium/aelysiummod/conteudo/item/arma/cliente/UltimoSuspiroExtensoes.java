package net.aelysium.aelysiummod.conteudo.item.arma.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class UltimoSuspiroExtensoes implements IClientItemExtensions {

    private static final float DESLOCAMENTO_X = 0.75F;
    private static final float DESLOCAMENTO_Y = 0.05F;
    private static final float DESLOCAMENTO_Z = 0.15F;
    private static final float GIRO_X = 20.0F;
    private static final float GIRO_Y = 0.0F;
    private static final float GIRO_Z = -55.0F;

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (entity.isUsingItem() && entity.getUsedItemHand() == hand) {
            return PoseSabre.pose();
        }
        return null;
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack,
                                           float partialTick, float equipProcess, float swingProcess) {
        if (!player.isUsingItem() || player.getUseItem() != stack) {
            return false;
        }

        float progresso = Mth.clamp((player.getTicksUsingItem() + partialTick) / 8.0F, 0.0F, 1.0F);
        float lado = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;

        poseStack.translate(lado * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
        poseStack.translate(-lado * DESLOCAMENTO_X * progresso, DESLOCAMENTO_Y * progresso, DESLOCAMENTO_Z * progresso);
        poseStack.mulPose(Axis.ZP.rotationDegrees(lado * GIRO_Z * progresso));
        poseStack.mulPose(Axis.XP.rotationDegrees(GIRO_X * progresso));
        poseStack.mulPose(Axis.YP.rotationDegrees(lado * GIRO_Y * progresso));
        return true;
    }
}
