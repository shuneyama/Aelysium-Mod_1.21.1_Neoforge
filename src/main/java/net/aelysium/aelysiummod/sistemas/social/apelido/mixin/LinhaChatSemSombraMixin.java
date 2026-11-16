package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.aelysium.aelysiummod.sistemas.social.apelido.cliente.OrdemListaJogadores;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class LinhaChatSemSombraMixin {

    @WrapMethod(method = "render")
    private void aelysium$agruparDesenho(GuiGraphics graficos, int tickCount, int mouseX, int mouseY, boolean focado, Operation<Void> original) {
        AcessorGraficosGui acessor = (AcessorGraficosGui) graficos;
        boolean anterior = acessor.aelysium$gerenciado();
        graficos.flush();
        acessor.aelysium$definirGerenciado(true);
        try {
            original.call(graficos, tickCount, mouseX, mouseY, focado);
        } finally {
            acessor.aelysium$definirGerenciado(anterior);
            OrdemListaJogadores.descarregar(graficos);
        }
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;drawTagIcon(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/GuiMessageTag$Icon;)V")
    )
    private void aelysium$descarregarAntesDoIcone(GuiGraphics graficos, int tickCount, int mouseX, int mouseY, boolean focado, CallbackInfo ci) {
        OrdemListaJogadores.descarregar(graficos);
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getChatListener()Lnet/minecraft/client/multiplayer/chat/ChatListener;")
    )
    private void aelysium$descarregarLinhas(GuiGraphics graficos, int tickCount, int mouseX, int mouseY, boolean focado, CallbackInfo ci) {
        OrdemListaJogadores.descarregar(graficos);
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I")
    )
    private int aelysium$linhaSemSombraNaInsignia(GuiGraphics graficos, Font fonte, FormattedCharSequence linha, int x, int y, int cor) {
        return OrdemListaJogadores.desenharLinhaSemSombraNaInsignia(graficos, fonte, linha, x, y, cor);
    }
}
