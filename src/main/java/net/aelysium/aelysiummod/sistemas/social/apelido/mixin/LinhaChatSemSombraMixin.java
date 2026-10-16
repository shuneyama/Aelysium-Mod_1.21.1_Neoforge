package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import net.aelysium.aelysiummod.sistemas.social.apelido.cliente.OrdemListaJogadores;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatComponent.class)
public abstract class LinhaChatSemSombraMixin {

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I")
    )
    private int aelysium$linhaSemSombraNaInsignia(GuiGraphics graficos, Font fonte, FormattedCharSequence linha, int x, int y, int cor) {
        return OrdemListaJogadores.desenharLinhaSemSombraNaInsignia(graficos, fonte, linha, x, y, cor);
    }
}
