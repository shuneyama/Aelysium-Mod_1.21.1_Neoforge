package net.aelysium.aelysiummod.conteudo.item.arma.cliente;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class PoseSabre {

    private static final float BRACO_X = (float) Math.toRadians(-102.09);
    private static final float BRACO_Y = (float) Math.toRadians(-39.64);
    private static final float BRACO_Z = (float) Math.toRadians(84.32);

    public static final EnumProxy<HumanoidModel.ArmPose> POSE_PROXY =
            new EnumProxy<>(HumanoidModel.ArmPose.class, false, (IArmPoseTransformer) PoseSabre::aplicar);

    private static void aplicar(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float progresso = Mth.clamp(entity.getTicksUsingItem() / 8.0F, 0.0F, 1.0F);
        float lado = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ModelPart braco = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        braco.xRot = BRACO_X * progresso;
        braco.yRot = BRACO_Y * lado * progresso;
        braco.zRot = BRACO_Z * lado * progresso;
    }

    public static HumanoidModel.ArmPose pose() {
        try {
            return POSE_PROXY.getValue();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
