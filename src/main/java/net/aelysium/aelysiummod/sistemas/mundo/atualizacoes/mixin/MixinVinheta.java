package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinVinheta {

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    private void aelysium$vinheta(GuiGraphics grafico, @Nullable Entity entidade, CallbackInfo ci) {
        if (!OpcoesAtualizadas.VINHETA.get()) ci.cancel();
    }
}
