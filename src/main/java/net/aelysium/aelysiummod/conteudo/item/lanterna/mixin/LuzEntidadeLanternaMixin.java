package net.aelysium.aelysiummod.conteudo.item.lanterna.mixin;

import net.aelysium.aelysiummod.conteudo.item.lanterna.cliente.LuzLanternaCliente;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class LuzEntidadeLanternaMixin {

    @Inject(method = "getBlockLightLevel", at = @At("RETURN"), cancellable = true)
    private void aelysium$aplicarLuzLanterna(Entity entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        int nivel = LuzLanternaCliente.nivel(pos);
        if (nivel > cir.getReturnValueI()) {
            cir.setReturnValue(nivel);
        }
    }
}
