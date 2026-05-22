package net.aelysium.aelysiummod.menu.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.aelysium.aelysiummod.chat.BalloonStyle;
import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BalloonCustomizationScreen extends Screen {

    private static final ResourceLocation TEXTURA_BALAO =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/balao.png");

    private int corTexto;
    private int corFundo;
    private int corBorda;
    private BalloonStyle estilo;

    private EditBox corTextoInput;
    private EditBox corFundoInput;
    private EditBox corBordaInput;

    private Button estiloButton;
    private Button modoCorButton;

    private boolean modoRGB = false;

    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 210;

    public BalloonCustomizationScreen(int corTexto, int corFundo, int corBorda, float altura, BalloonStyle estilo) {
        super(Component.literal("Customizar Balão"));
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.estilo = estilo;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2 - 80;
        int startY = (this.height - GUI_HEIGHT) / 2 + 20;

        modoCorButton = Button.builder(Component.literal(modoRGB ? "Modo: RGB" : "Modo: HEX"), button -> {
            modoRGB = !modoRGB;
            button.setMessage(Component.literal(modoRGB ? "Modo: RGB" : "Modo: HEX"));
            atualizarInputs();
        }).bounds(centerX - 100, startY, 200, 20).build();
        this.addRenderableWidget(modoCorButton);

        this.addRenderableWidget(Button.builder(Component.literal("Texto"), button -> {})
                .bounds(centerX - 100, startY + 28, 50, 16).build());

        corTextoInput = new EditBox(this.font, centerX - 45, startY + 28, 110, 16, Component.literal(""));
        corTextoInput.setMaxLength(modoRGB ? 11 : 6);
        corTextoInput.setValue(formatarCor(corTexto));
        corTextoInput.setResponder(this::onCorTextoChanged);
        this.addRenderableWidget(corTextoInput);

        this.addRenderableWidget(Button.builder(Component.literal("Fundo"), button -> {})
                .bounds(centerX - 100, startY + 50, 50, 16).build());

        corFundoInput = new EditBox(this.font, centerX - 45, startY + 50, 110, 16, Component.literal(""));
        corFundoInput.setMaxLength(modoRGB ? 11 : 6);
        corFundoInput.setValue(formatarCor(corFundo));
        corFundoInput.setResponder(this::onCorFundoChanged);
        this.addRenderableWidget(corFundoInput);

        this.addRenderableWidget(Button.builder(Component.literal("Borda"), button -> {})
                .bounds(centerX - 100, startY + 72, 50, 16).build());

        corBordaInput = new EditBox(this.font, centerX - 45, startY + 72, 110, 16, Component.literal(""));
        corBordaInput.setMaxLength(modoRGB ? 11 : 6);
        corBordaInput.setValue(formatarCor(corBorda));
        corBordaInput.setResponder(this::onCorBordaChanged);
        this.addRenderableWidget(corBordaInput);

        estiloButton = Button.builder(
                Component.literal("Estilo: " + (estilo == BalloonStyle.ROUNDED ? "Arredondado" : "Quadrado")),
                button -> {
                    estilo = (estilo == BalloonStyle.ROUNDED) ? BalloonStyle.SQUARE : BalloonStyle.ROUNDED;
                    button.setMessage(Component.literal("Estilo: " + (estilo == BalloonStyle.ROUNDED ? "Arredondado" : "Quadrado")));
                }
        ).bounds(centerX - 100, startY + 94, 200, 20).build();
        this.addRenderableWidget(estiloButton);

        this.addRenderableWidget(Button.builder(Component.literal("Resetar"), button -> {
            corTexto = 0x141414;
            corFundo = 0xFFFFFF;
            corBorda = 0x000000;
            estilo = BalloonStyle.ROUNDED;
            atualizarInputs();
            estiloButton.setMessage(Component.literal("Estilo: Arredondado"));
        }).bounds(centerX - 100, startY + 120, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Salvar"), button -> {
            salvarConfiguracoes();
            this.onClose();
        }).bounds(centerX - 100, startY + 150, 95, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancelar"), button -> {
            this.onClose();
        }).bounds(centerX + 5, startY + 150, 95, 20).build());
    }

    private void atualizarInputs() {
        corTextoInput.setValue(formatarCor(corTexto));
        corFundoInput.setValue(formatarCor(corFundo));
        corBordaInput.setValue(formatarCor(corBorda));
        corTextoInput.setMaxLength(modoRGB ? 11 : 6);
        corFundoInput.setMaxLength(modoRGB ? 11 : 6);
        corBordaInput.setMaxLength(modoRGB ? 11 : 6);
    }

    private String formatarCor(int cor) {
        if (modoRGB) {
            int r = (cor >> 16) & 0xFF;
            int g = (cor >> 8) & 0xFF;
            int b = cor & 0xFF;
            return r + "," + g + "," + b;
        } else {
            return String.format("%06X", cor);
        }
    }

    private int parseCor(String input) {
        try {
            if (modoRGB) {
                String[] parts = input.split(",");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    r = Math.max(0, Math.min(255, r));
                    g = Math.max(0, Math.min(255, g));
                    b = Math.max(0, Math.min(255, b));
                    return (r << 16) | (g << 8) | b;
                }
            } else {
                return Integer.parseInt(input.replace("#", ""), 16);
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private void onCorTextoChanged(String value) {
        int cor = parseCor(value);
        if (cor != -1) {
            corTexto = cor;
        }
    }

    private void onCorFundoChanged(String value) {
        int cor = parseCor(value);
        if (cor != -1) {
            corFundo = cor;
        }
    }

    private void onCorBordaChanged(String value) {
        int cor = parseCor(value);
        if (cor != -1) {
            corBorda = cor;
        }
    }

    private void salvarConfiguracoes() {
        AelysiumNetwork.enviarBalloonConfigParaServidor(corTexto, corFundo, corBorda, estilo);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2 - 80;
        int startY = (this.height - GUI_HEIGHT) / 2;

        guiGraphics.blit(TEXTURA_BALAO, centerX - 110, startY, 0, 0, 220, GUI_HEIGHT, 220, GUI_HEIGHT);

        guiGraphics.drawCenteredString(this.font, this.title, centerX, startY + 8, 0xFFFFFF);

        float escala = 1.5f;
        int larguraTextura = 220;
        int alturaTextura = GUI_HEIGHT;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX - 110, startY, 0);
        guiGraphics.pose().scale(escala, escala, 0.9f);
        guiGraphics.blit(TEXTURA_BALAO, 0, 0, 0, 0, larguraTextura, alturaTextura, larguraTextura, alturaTextura);
        guiGraphics.pose().popPose();

        renderBalloonPreview(guiGraphics, this.width / 2 + 120, this.height / 2);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderBalloonPreview(GuiGraphics guiGraphics, int x, int y) {
        String previewText = "Personalize seu Balão!";
        int textWidth = this.font.width(previewText);
        int padding = 6;
        int balloonWidth = textWidth + padding * 2;
        int balloonHeight = 9 + padding * 2;

        int balloonX = x - balloonWidth / 2;
        int balloonY = y - balloonHeight / 2;

        float bgR = ((corFundo >> 16) & 0xFF) / 255.0F;
        float bgG = ((corFundo >> 8) & 0xFF) / 255.0F;
        float bgB = (corFundo & 0xFF) / 255.0F;

        float bordaR = ((corBorda >> 16) & 0xFF) / 255.0F;
        float bordaG = ((corBorda >> 8) & 0xFF) / 255.0F;
        float bordaB = (corBorda & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = guiGraphics.pose().last().pose();

        if (estilo == BalloonStyle.ROUNDED) {
            drawRoundedBalloonPreview(matrix, balloonX, balloonY, balloonWidth, balloonHeight, bgR, bgG, bgB, bordaR, bordaG, bordaB);
        } else {
            drawSquareBalloonPreview(matrix, balloonX, balloonY, balloonWidth, balloonHeight, bgR, bgG, bgB, bordaR, bordaG, bordaB);
        }

        drawTailPreview(matrix, x - 3, balloonY + balloonHeight, bgR, bgG, bgB, bordaR, bordaG, bordaB);

        guiGraphics.drawString(this.font, previewText, x - textWidth / 2, y - 4, corTexto, false);
    }

    private void drawRoundedBalloonPreview(Matrix4f matrix, int x, int y, int width, int height,
                                           float bgR, float bgG, float bgB, float bordaR, float bordaG, float bordaB) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int r = 5;
        addQuad(buffer, matrix, x + r, y, width - r * 2, height, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x, y + r, r, height - r * 2, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - r, y + r, r, height - r * 2, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 2, 1, 3, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 1, 1, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + 1, 2, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 2, y + 2, 1, 3, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + 1, 1, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y + 1, 2, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + height - 5, 1, 3, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + height - 5, 1, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + height - 5, 2, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 2, y + height - 5, 1, 3, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + height - 5, 1, 4, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y + height - 5, 2, 4, bgR, bgG, bgB, 1.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buffer, matrix, x + r, y, width - r * 2, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + r, y + height - 1, width - r * 2, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + r, 1, height - r * 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + r, 1, height - r * 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 3, y, 2, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 1, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 2, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + 3, 1, 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y, 2, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + 1, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 2, y + 2, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + 3, 1, 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + height - 5, 1, 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + height - 3, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + height - 2, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + height - 1, 2, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + height - 5, 1, 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 2, y + height - 3, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + height - 2, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y + height - 1, 2, 1, bordaR, bordaG, bordaB, 1.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void drawSquareBalloonPreview(Matrix4f matrix, int x, int y, int width, int height,
                                          float bgR, float bgG, float bgB, float bordaR, float bordaG, float bordaB) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buffer, matrix, x + 1, y + 1, width - 2, height - 2, bgR, bgG, bgB, 1.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buffer, matrix, x, y, width, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + height - 1, width, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + 1, 1, height - 2, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + 1, 1, height - 2, bordaR, bordaG, bordaB, 1.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void drawTailPreview(Matrix4f matrix, int x, int y, float bgR, float bgG, float bgB,
                                 float bordaR, float bordaG, float bordaB) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buffer, matrix, x, y, 5, 1, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 1, 3, 1, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 2, 1, 1, bgR, bgG, bgB, 1.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buffer, matrix, x - 1, y, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 5, y, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + 1, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 4, y + 1, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 2, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + 2, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 3, 1, 1, bordaR, bordaG, bordaB, 1.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private void addQuad(BufferBuilder buffer, Matrix4f matrix, int x, int y, int width, int height, float r, float g, float b, float a) {
        buffer.addVertex(matrix, x, y + height, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + width, y + height, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + width, y, 0).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
