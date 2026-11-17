package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityRenderer.class)
public abstract class MixinAmarraEspessura {

    @ModifyConstant(method = "renderLeash", constant = @Constant(floatValue = 0.025F))
    private float aelysium$espessura(float original) {
        return original * 2.0F;
    }
}
