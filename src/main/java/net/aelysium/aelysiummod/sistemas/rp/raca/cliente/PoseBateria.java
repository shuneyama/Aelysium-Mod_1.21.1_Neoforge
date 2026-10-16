package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class PoseBateria {

    private static final float BRACO_X = (float) Math.toRadians(-112.0);
    private static final float BRACO_Y = (float) Math.toRadians(-32.0);
    private static final float BRACO_Z = (float) Math.toRadians(6.0);
    private static final float OSCILACAO = 0.05F;

    public static final EnumProxy<HumanoidModel.ArmPose> POSE_PROXY =
            new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) PoseBateria::aplicar);

    private static void aplicar(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float progresso = Mth.clamp(entity.getTicksUsingItem() / 6.0F, 0.0F, 1.0F);
        float mordida = Mth.sin(entity.tickCount * 1.3F) * OSCILACAO * progresso;

        model.rightArm.xRot = BRACO_X * progresso + mordida;
        model.rightArm.yRot = BRACO_Y * progresso;
        model.rightArm.zRot = BRACO_Z * progresso;

        model.leftArm.xRot = BRACO_X * progresso - mordida;
        model.leftArm.yRot = -BRACO_Y * progresso;
        model.leftArm.zRot = -BRACO_Z * progresso;
    }

    public static HumanoidModel.ArmPose pose() {
        try {
            return POSE_PROXY.getValue();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
