package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.render;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ferramentas.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.modelo.ferramentas.AdvancedModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class MowzieRenderUtils {
    public static void matrixStackFromModel(PoseStack matrixStack, AdvancedModelRenderer modelRenderer) {
        AdvancedModelRenderer parent = modelRenderer.getParent();
        if (parent != null) matrixStackFromModel(matrixStack, parent);
        modelRenderer.translateRotate(matrixStack);
    }

    public static Vec3 getWorldPosFromModel(Entity entity, float entityYaw, AdvancedModelRenderer modelRenderer) {
        PoseStack matrixStack = new PoseStack();
        matrixStack.translate(entity.getX(), entity.getY(), entity.getZ());
        matrixStack.mulPose(MathUtils.quatFromRotationXYZ(0, -entityYaw + 180, 0, true));
        matrixStack.scale(-1, -1, 1);
        matrixStack.translate(0, -1.5f, 0);
        MowzieRenderUtils.matrixStackFromModel(matrixStack, modelRenderer);
        PoseStack.Pose matrixEntry = matrixStack.last();
        Matrix4f matrix4f = matrixEntry.pose();

        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.mul(matrix4f);
        return new Vec3(vec.x(), vec.y(), vec.z());
    }
}
