package net.aelysium.aelysiummod.sistemas.chefes.mixin;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.ClienteFlorVazio;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public class MixinBarraChefeFlorVazio {

    @Shadow @Final private static ResourceLocation[] BAR_BACKGROUND_SPRITES;
    @Shadow @Final private static ResourceLocation[] BAR_PROGRESS_SPRITES;

    @Inject(method = "drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V", at = @At("HEAD"), cancellable = true)
    private void aelysium$desenharBarraFlorVazio(GuiGraphics graficos, int x, int y, BossEvent evento, CallbackInfo ci) {
        ClienteFlorVazio.barraChefe().renderBossBar(BAR_BACKGROUND_SPRITES, BAR_PROGRESS_SPRITES, graficos, x, y, evento, ci);
    }
}
