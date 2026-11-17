package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.ReaplicarJanela;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class MixinTelaCheiaExclusiva implements ReaplicarJanela {

    @Shadow
    private boolean fullscreen;
    @Shadow
    private int windowedX;
    @Shadow
    private int windowedY;
    @Shadow
    private int windowedWidth;
    @Shadow
    private int windowedHeight;
    @Shadow
    @Final
    private WindowEventHandler eventHandler;

    @Shadow
    private native void setMode();

    @Override
    public void aelysium$reaplicar() {
        if (!this.fullscreen) return;
        int x = this.windowedX;
        int y = this.windowedY;
        int largura = this.windowedWidth;
        int altura = this.windowedHeight;
        this.setMode();
        this.windowedX = x;
        this.windowedY = y;
        this.windowedWidth = largura;
        this.windowedHeight = altura;
        this.eventHandler.resizeDisplay();
    }

    @WrapOperation(method = "setMode", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V"))
    private void aelysium$modo(long janela, long monitor, int x, int y, int largura, int altura, int taxa, Operation<Void> original) {
        if (monitor != 0L && !OpcoesAtualizadas.TELA_CHEIA_EXCLUSIVA.get()) {
            int[] mx = new int[1];
            int[] my = new int[1];
            GLFW.glfwGetMonitorPos(monitor, mx, my);
            GLFW.glfwSetWindowAttrib(janela, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(janela, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
            original.call(janela, 0L, mx[0], my[0], largura, altura, -1);
            return;
        }
        if (monitor == 0L) {
            GLFW.glfwSetWindowAttrib(janela, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(janela, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        } else {
            GLFW.glfwSetWindowAttrib(janela, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
        original.call(janela, monitor, x, y, largura, altura, taxa);
    }

    @Inject(method = "changeFullscreenVideoMode", at = @At("HEAD"), cancellable = true)
    private void aelysium$semModoDeVideo(CallbackInfo ci) {
        if (!OpcoesAtualizadas.TELA_CHEIA_EXCLUSIVA.get()) ci.cancel();
    }
}
