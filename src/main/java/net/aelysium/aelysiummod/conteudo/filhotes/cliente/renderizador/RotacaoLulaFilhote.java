package net.aelysium.aelysiummod.conteudo.filhotes.cliente.renderizador;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

final class RotacaoLulaFilhote {

    private RotacaoLulaFilhote() {
    }

    static void aplicar(Squid entidade, PoseStack poseStack, float yBodyRot, float parcial) {
        float x = Mth.lerp(parcial, entidade.xBodyRotO, entidade.xBodyRot);
        float z = Mth.lerp(parcial, entidade.zBodyRotO, entidade.zBodyRot);
        poseStack.translate(0.0F, 0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(x));
        poseStack.mulPose(Axis.YP.rotationDegrees(z));
        poseStack.translate(0.0F, -0.6F, 0.0F);
    }
}
