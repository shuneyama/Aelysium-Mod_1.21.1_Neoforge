package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

public class TelaCinematica extends Screen {

    private final TexturaVideo videoTexture = new TexturaVideo();

    public TelaCinematica() {
        super(Component.translatable("gui.aelysiummod.cinematica.aelysiumcutscene"));
    }

    @Override public boolean isPauseScreen()                                                          { return false; }
    @Override public boolean shouldCloseOnEsc()                                                       { return false; }
    @Override public boolean keyPressed(int k, int s, int m)                                          { return true; }
    @Override public boolean keyReleased(int k, int s, int m)                                         { return true; }
    @Override public boolean mouseClicked(double x, double y, int b)                                  { return true; }
    @Override public boolean mouseReleased(double x, double y, int b)                                 { return true; }
    @Override public boolean mouseScrolled(double x, double y, double sx, double sy)                  { return true; }
    @Override public boolean mouseDragged(double x, double y, int b, double dx, double dy)            { return true; }
    @Override public boolean charTyped(char c, int m)                                                  { return true; }
    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt)                   {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int W = this.width;
        int H = this.height;

        graphics.fill(0, 0, W, H, 0xFF000000);

        ControladorCinematica.EstadoRender estado = ControladorCinematica.calcularEstado();
        if (estado == null) {
            ControladorCinematica.parar();
            return;
        }

        if (estado.playingVideo) {
            QuadroVideo frame = ControladorCinematica.obterFrameAtual();
            if (frame != null) {
                videoTexture.update(frame);
                var loc = videoTexture.getLocation();
                if (loc != null) {

                    float aspectVideo = frame.width  / (float) frame.height;
                    float aspectTela  = W / (float) H;
                    int dW, dH, dX, dY;
                    if (aspectVideo > aspectTela) {
                        dW = W;
                        dH = (int) (W / aspectVideo);
                        dX = 0;
                        dY = (H - dH) / 2;
                    } else {
                        dH = H;
                        dW = (int) (H * aspectVideo);
                        dX = (W - dW) / 2;
                        dY = 0;
                    }

                    RenderSystem.enableBlend();
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    graphics.blit(loc, dX, dY, 0, 0, dW, dH, dW, dH);
                    RenderSystem.disableBlend();
                }
            }
        }


        if (estado.fadeAlpha < 1.0f) {
            int alpha = (int) ((1.0f - estado.fadeAlpha) * 255.0f);
            graphics.fill(0, 0, W, H, alpha << 24);
        }
    }

    public void liberarTextura() {
        videoTexture.close();
    }

    @Override
    public void removed() {
        liberarTextura();
        super.removed();
    }
}
