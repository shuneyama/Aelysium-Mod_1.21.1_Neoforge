package net.aelysium.aelysiummod.conteudo.item.lanterna.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.conteudo.item.lanterna.cliente.LuzLanternaCliente;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class LuzEntidadeLanternaMixin {

    @ModifyReturnValue(method = "getBlockLightLevel", at = @At("RETURN"))
    private int aelysium$aplicarLuzLanterna(int original, @Local(argsOnly = true) BlockPos pos) {
        return Math.max(original, LuzLanternaCliente.nivel(pos));
    }
}
