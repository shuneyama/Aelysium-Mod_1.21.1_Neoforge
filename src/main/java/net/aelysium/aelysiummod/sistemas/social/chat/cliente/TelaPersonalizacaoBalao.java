package net.aelysium.aelysiummod.sistemas.social.chat.cliente;

import net.aelysium.aelysiummod.sistemas.social.chat.ConfiguracaoBalao;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.aelysium.aelysiummod.sistemas.social.chat.EstiloBalao;
import net.aelysium.aelysiummod.sistemas.social.chat.cliente.ManipuladorRenderizacaoBalao;
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
import net.aelysium.aelysiummod.sistemas.social.chat.rede.RedeChat;

@OnlyIn(Dist.CLIENT)
public class TelaPersonalizacaoBalao extends Screen {

    private static final ResourceLocation TEXTURA_BALAO =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "textures/gui/balao.png");

    private int corTexto;
    private int corFundo;
    private int corBorda;
    private EstiloBalao estilo;

    private EditBox corTextoInput;
    private EditBox corFundoInput;
    private EditBox corBordaInput;
    private EditBox alturaInput;
    private float altura;

    private Button estiloButton;

    private final boolean modoRGB = false;

    private static final int BALLOON_PADDING_PREVIEW = 8;

    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 235;

    public TelaPersonalizacaoBalao(int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo) {
        super(Component.translatable("gui.aelysiummod.chat.customizar_balao"));
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.estilo = estilo;
        this.altura = altura;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2 - 80;
        int startY = (this.height - GUI_HEIGHT) / 2 + 20;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.texto"), button -> {})
                .bounds(centerX - 100, startY + 4, 50, 16).build());

        corTextoInput = new EditBox(this.font, centerX - 45, startY + 4, 110, 16, Component.literal(""));
        corTextoInput.setMaxLength(modoRGB ? 11 : 6);
        corTextoInput.setValue(formatarCor(corTexto));
        corTextoInput.setResponder(this::onCorTextoChanged);
        this.addRenderableWidget(corTextoInput);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.fundo"), button -> {})
                .bounds(centerX - 100, startY + 26, 50, 16).build());

        corFundoInput = new EditBox(this.font, centerX - 45, startY + 26, 110, 16, Component.literal(""));
        corFundoInput.setMaxLength(modoRGB ? 11 : 6);
        corFundoInput.setValue(formatarCor(corFundo));
        corFundoInput.setResponder(this::onCorFundoChanged);
        this.addRenderableWidget(corFundoInput);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.borda"), button -> {})
                .bounds(centerX - 100, startY + 48, 50, 16).build());

        corBordaInput = new EditBox(this.font, centerX - 45, startY + 48, 110, 16, Component.literal(""));
        corBordaInput.setMaxLength(modoRGB ? 11 : 6);
        corBordaInput.setValue(formatarCor(corBorda));
        corBordaInput.setResponder(this::onCorBordaChanged);
        this.addRenderableWidget(corBordaInput);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.altura"), button -> {})
                .bounds(centerX - 100, startY + 70, 50, 16).build());

        alturaInput = new EditBox(this.font, centerX - 45, startY + 70, 90, 16, Component.literal(""));
        alturaInput.setMaxLength(6);
        alturaInput.setValue(formatarAltura(altura));
        alturaInput.setResponder(this::onAlturaChanged);
        this.addRenderableWidget(alturaInput);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.blocos"), button -> {})
                .bounds(centerX + 49, startY + 70, 51, 16).build());

        estiloButton = Button.builder(
                Component.translatableEscape("gui.aelysiummod.chat.estilo", (estilo == EstiloBalao.ROUNDED ? "Arredondado" : "Quadrado")),
                button -> {
                    estilo = (estilo == EstiloBalao.ROUNDED) ? EstiloBalao.SQUARE : EstiloBalao.ROUNDED;
                    button.setMessage(Component.translatableEscape("gui.aelysiummod.chat.estilo", (estilo == EstiloBalao.ROUNDED ? "Arredondado" : "Quadrado")));
                }
        ).bounds(centerX - 100, startY + 92, 200, 20).build();
        this.addRenderableWidget(estiloButton);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.resetar"), button -> {
            corTexto = 0x141414;
            corFundo = 0xFFFFFF;
            corBorda = 0x000000;
            estilo = EstiloBalao.ROUNDED;
            altura = ConfiguracaoBalao.ALTURA_PADRAO_BLOCOS;
            alturaInput.setValue(formatarAltura(altura));
            atualizarInputs();
            estiloButton.setMessage(Component.translatable("gui.aelysiummod.chat.estilo_arredondado"));
        }).bounds(centerX - 100, startY + 116, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable(ManipuladorRenderizacaoBalao.baloesVisiveis ? "gui.aelysiummod.chat.baloes_visiveis" : "gui.aelysiummod.chat.baloes_ocultos"),
                button -> {
                    ManipuladorRenderizacaoBalao.baloesVisiveis = !ManipuladorRenderizacaoBalao.baloesVisiveis;
                    button.setMessage(Component.translatable(ManipuladorRenderizacaoBalao.baloesVisiveis ? "gui.aelysiummod.chat.baloes_visiveis" : "gui.aelysiummod.chat.baloes_ocultos"));
                }
        ).bounds(centerX - 100, startY + 139, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.salvar"), button -> {
            salvarConfiguracoes();
            this.onClose();
        }).bounds(centerX - 100, startY + 163, 95, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.chat.cancelar"), button -> {
            this.onClose();
        }).bounds(centerX + 5, startY + 163, 95, 20).build());
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

    private static String formatarAltura(float valor) {
        return String.format(java.util.Locale.ROOT, "%.2f", valor).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private void onAlturaChanged(String valor) {
        try {
            float lida = Float.parseFloat(valor.trim().replace(',', '.'));
            if (Float.isFinite(lida)) {
                altura = Math.max(ConfiguracaoBalao.ALTURA_MINIMA_BLOCOS, Math.min(ConfiguracaoBalao.ALTURA_MAXIMA_BLOCOS, lida));
            }
        } catch (NumberFormatException ignored) {}
    }

    private void salvarConfiguracoes() {
        RedeChat.enviarBalloonConfigParaServidor(corTexto, corFundo, corBorda, altura, estilo);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2 - 80;
        int startY = (this.height - GUI_HEIGHT) / 2;

        guiGraphics.blit(TEXTURA_BALAO, centerX - 110, startY, 0, 0, 220, GUI_HEIGHT, 220, GUI_HEIGHT);

        guiGraphics.drawCenteredString(this.font, this.title, centerX, startY + 8, 0x000000);

        float escala = 1.5f;
        int larguraTextura = 220;
        int alturaTextura = GUI_HEIGHT;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX - 110, startY, 0);
        guiGraphics.pose().scale(escala, escala, 0.9f);
        guiGraphics.blit(TEXTURA_BALAO, 0, 0, 0, 0, larguraTextura, alturaTextura, larguraTextura, alturaTextura);
        guiGraphics.pose().popPose();

        renderPreviewComJogador(guiGraphics, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPreviewComJogador(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.width / 2 + 120;
        int pesY = this.height / 2 + 70;

        float pixelsPorBloco = 40f;
        float espacoAcima = pesY - 14;
        float necessario = Math.max(altura + 0.05f, ConfiguracaoBalao.ALTURA_JOGADOR_BLOCOS) * pixelsPorBloco;
        if (necessario > espacoAcima) {
            pixelsPorBloco = Math.max(8f, espacoAcima / Math.max(altura + 0.05f, ConfiguracaoBalao.ALTURA_JOGADOR_BLOCOS));
        }

        var jogador = net.minecraft.client.Minecraft.getInstance().player;
        if (jogador != null) {
            int escalaJogador = Math.round(pixelsPorBloco);
            int centroY = Math.round(pesY - (jogador.getBbHeight() / 2f + 0.0625f) * pixelsPorBloco);
            try {
                net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse(
                        guiGraphics, x - 40, centroY - 40, x + 40, centroY + 40,
                        escalaJogador, 0.0625f, mouseX, mouseY, jogador);
            } catch (Exception ignored) {}
        }

        float ancoraY = pesY - altura * pixelsPorBloco;
        float escalaBalao = pixelsPorBloco / 40f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, ancoraY, 0);
        guiGraphics.pose().scale(escalaBalao, escalaBalao, 1f);
        renderBalloonPreview(guiGraphics, 0, BALLOON_PADDING_PREVIEW + 4 - 1);
        guiGraphics.pose().popPose();
    }

    private void renderBalloonPreview(GuiGraphics guiGraphics, int x, int y) {
        String previewText = "Personalize seu Balão!";
        int textWidth = this.font.width(previewText);
        int padding = BALLOON_PADDING_PREVIEW;
        int balloonWidth = textWidth + padding * 2 + 4;
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

        if (estilo == EstiloBalao.ROUNDED) {
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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
