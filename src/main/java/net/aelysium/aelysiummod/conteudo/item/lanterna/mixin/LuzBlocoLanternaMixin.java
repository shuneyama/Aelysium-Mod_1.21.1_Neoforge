package net.aelysium.aelysiummod.conteudo.item.lanterna.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.conteudo.item.lanterna.cliente.LuzLanternaCliente;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LuzBlocoLanternaMixin {

    @ModifyReturnValue(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"))
    private static int aelysium$aplicarLuzLanterna(int original, @Local(argsOnly = true) BlockPos pos) {
        int nivel = LuzLanternaCliente.nivel(pos);
        if (nivel == 0 || nivel <= LightTexture.block(original)) return original;
        return LightTexture.pack(nivel, LightTexture.sky(original));
    }
}
