package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelaFlags extends Screen {

    private static final int COR_BORDA = 0xFF555555;
    private static final int COR_TITULO = 0xFFFFDD55;
    private static final int RAIO_CANTO = 6;

    private static final int COLUNAS = 2;
    private static final int LARGURA_TELA = 420;
    private static final int LARGURA_BOTAO = 190;
    private static final int ALTURA_BOTAO = 20;
    private static final int ESPACO_H = 10;
    private static final int ESPACO_V = 4;
    private static final int PADDING = 20;

    private static final FlagRegiao[] FLAGS_PADRAO_ATIVAS = {
            FlagRegiao.ENTRAR_REGIAO,
            FlagRegiao.SAIR_REGIAO
    };

    private static final ResourceLocation TEXTURA_TERRA =
            ResourceLocation.withDefaultNamespace("textures/block/dirt.png");

    private final Screen telaAnterior;
    private final Map<FlagRegiao, Boolean> flags;
    private final List<Button> botoesFlags = new ArrayList<>();

    private int posX, posY, alturaCalculada;

    public TelaFlags(Screen telaAnterior, Map<FlagRegiao, Boolean> flags) {
        super(Component.literal("Configurar Flags"));
        this.telaAnterior = telaAnterior;
        this.flags = flags;

        for (FlagRegiao flagPadrao : FLAGS_PADRAO_ATIVAS) {
            if (!flags.containsKey(flagPadrao) || !flags.get(flagPadrao)) {
                flags.put(flagPadrao, true);
            }
        }
    }

    @Override
    protected void init() {
        botoesFlags.clear();

        FlagRegiao[] todasFlags = FlagRegiao.values();
        int totalLinhas = (int) Math.ceil((double) todasFlags.length / COLUNAS);

        int alturaFlags = totalLinhas * (ALTURA_BOTAO + ESPACO_V);
        int alturaVoltarArea = ALTURA_BOTAO + 16;
        int alturaTitulo = 32;

        alturaCalculada = alturaTitulo + alturaFlags + alturaVoltarArea + PADDING;

        int maxAltura = this.height - 20;
        if (alturaCalculada > maxAltura) alturaCalculada = maxAltura;

        this.posX = (this.width - LARGURA_TELA) / 2;
        this.posY = (this.height - alturaCalculada) / 2;

        int areaW = COLUNAS * LARGURA_BOTAO + (COLUNAS - 1) * ESPACO_H;
        int startX = posX + (LARGURA_TELA - areaW) / 2;
        int startY = posY + alturaTitulo;

        for (int idx = 0; idx < todasFlags.length; idx++) {
            FlagRegiao flag = todasFlags[idx];
            int col = idx % COLUNAS;
            int linha = idx / COLUNAS;

            int bx = startX + col * (LARGURA_BOTAO + ESPACO_H);
            int by = startY + linha * (ALTURA_BOTAO + ESPACO_V);

            boolean valorAtual = flags.getOrDefault(flag, false);

            Button botao = Button.builder(
                    gerarTextoFlag(flag, valorAtual),
                    btn -> toggleFlag(flag, btn)
            ).bounds(bx, by, LARGURA_BOTAO, ALTURA_BOTAO).build();

            botoesFlags.add(botao);
            this.addRenderableWidget(botao);
        }

        int larguraBotaoConcluido = 160;
        int yConcluido = posY + alturaCalculada - alturaVoltarArea + 8;

        this.addRenderableWidget(Button.builder(
                Component.literal("\u2714 Concluído"),
                btn -> Minecraft.getInstance().setScreen(telaAnterior)
        ).bounds(posX + (LARGURA_TELA - larguraBotaoConcluido) / 2, yConcluido, larguraBotaoConcluido, ALTURA_BOTAO).build());
    }

    private Component gerarTextoFlag(FlagRegiao flag, boolean valor) {
        String status = valor ? "\u00a7a\u2714" : "\u00a7c\u2716";
        return Component.literal(status + " \u00a7f" + flag.getNome());
    }

    private void toggleFlag(FlagRegiao flag, Button botao) {
        boolean novoValor = !flags.getOrDefault(flag, false);
        flags.put(flag, novoValor);
        botao.setMessage(gerarTextoFlag(flag, novoValor));
    }

    @Override
    public void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        renderizarPainelComTerra(graphics);
        graphics.drawCenteredString(this.font, "Configurar Flags da Região",
                posX + LARGURA_TELA / 2, posY + 12, COR_TITULO);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderizarPainelComTerra(GuiGraphics graphics) {
        int x1 = posX, y1 = posY, x2 = posX + LARGURA_TELA, y2 = posY + alturaCalculada;
        int tam = 16;
        graphics.enableScissor(x1, y1, x2, y2);
        for (int ix = 0; ix <= LARGURA_TELA / tam + 1; ix++)
            for (int iy = 0; iy <= alturaCalculada / tam + 1; iy++)
                graphics.blit(TEXTURA_TERRA, x1 + ix * tam, y1 + iy * tam, 0, 0, tam, tam, tam, tam);
        graphics.disableScissor();
        graphics.fill(x1, y1, x2, y2, 0xD0232323);
        renderizarBordaArredondada(graphics, x1, y1, x2, y2, RAIO_CANTO);
    }

    private void renderizarBordaArredondada(GuiGraphics graphics, int x1, int y1, int x2, int y2, int r) {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                double dist = Math.sqrt((r - 1 - i) * (r - 1 - i) + (r - 1 - j) * (r - 1 - j));
                if (dist >= r) {
                    graphics.fill(x1 + i, y1 + j, x1 + i + 1, y1 + j + 1, 0xFF000000);
                    graphics.fill(x2 - i - 1, y1 + j, x2 - i, y1 + j + 1, 0xFF000000);
                    graphics.fill(x1 + i, y2 - j - 1, x1 + i + 1, y2 - j, 0xFF000000);
                    graphics.fill(x2 - i - 1, y2 - j - 1, x2 - i, y2 - j, 0xFF000000);
                } else if (dist >= r - 1) {
                    graphics.fill(x1 + i, y1 + j, x1 + i + 1, y1 + j + 1, COR_BORDA);
                    graphics.fill(x2 - i - 1, y1 + j, x2 - i, y1 + j + 1, COR_BORDA);
                    graphics.fill(x1 + i, y2 - j - 1, x1 + i + 1, y2 - j, COR_BORDA);
                    graphics.fill(x2 - i - 1, y2 - j - 1, x2 - i, y2 - j, COR_BORDA);
                }
            }
        }
        graphics.fill(x1 + r, y1, x2 - r, y1 + 2, COR_BORDA);
        graphics.fill(x1 + r, y2 - 2, x2 - r, y2, COR_BORDA);
        graphics.fill(x1, y1 + r, x1 + 2, y2 - r, COR_BORDA);
        graphics.fill(x2 - 2, y1 + r, x2, y2 - r, COR_BORDA);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
