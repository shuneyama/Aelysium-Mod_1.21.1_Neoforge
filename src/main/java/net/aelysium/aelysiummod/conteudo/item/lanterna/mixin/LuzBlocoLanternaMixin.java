package net.aelysium.aelysiummod.conteudo.item.lanterna.mixin;

import net.aelysium.aelysiummod.conteudo.item.lanterna.cliente.LuzLanternaCliente;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LuzBlocoLanternaMixin {

    @Inject(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"), cancellable = true)
    private static void aelysium$aplicarLuzLanterna(BlockAndTintGetter level, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        int nivel = LuzLanternaCliente.nivel(pos);
        if (nivel == 0) return;

        int original = cir.getReturnValueI();
        if (nivel > LightTexture.block(original)) {
            cir.setReturnValue(LightTexture.pack(nivel, LightTexture.sky(original)));
        }
    }
}
