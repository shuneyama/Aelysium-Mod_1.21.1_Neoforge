package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.CacheMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InterfaceAvisoMontariaMaozinhaMixin {

    @Inject(method = "setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), cancellable = true)
    private void aelysium_ocultarAvisoMontaria(Component mensagem, boolean animarCor, CallbackInfo ci) {
        if (!(mensagem.getContents() instanceof TranslatableContents traducao) || !"mount.onboard".equals(traducao.getKey())) return;
        LocalPlayer jogador = Minecraft.getInstance().player;
        if (jogador != null && CacheMaozinha.liderDe(jogador.getUUID()) != null) ci.cancel();
    }
}
