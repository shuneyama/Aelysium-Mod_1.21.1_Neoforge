package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.SuavizadorPosicaoMaozinha;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class DespachanteRenderizacaoEntidadeMixin {

    @Unique
    private boolean aelysium_renderizandoMaozinha = false;

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void aelysium_deslocarMaozinha(Entity entidade, double x, double y, double z, float yaw, float parcial,
                                           PoseStack poseStack, MultiBufferSource buffer, int luz, CallbackInfo ci) {
        if (aelysium_renderizandoMaozinha || !(entidade instanceof Player jogador)) return;

        Vec3 deslocamento = SuavizadorPosicaoMaozinha.deslocamento(jogador);
        if (deslocamento.lengthSqr() < 1.0E-8) return;

        ci.cancel();
        aelysium_renderizandoMaozinha = true;
        try {
            ((EntityRenderDispatcher) (Object) this).render(entidade, x + deslocamento.x, y, z + deslocamento.z,
                    yaw, parcial, poseStack, buffer, luz);
        } finally {
            aelysium_renderizandoMaozinha = false;
        }
    }
}
