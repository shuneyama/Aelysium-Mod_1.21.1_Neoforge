package net.aelysium.aelysiummod.menu.gui;

import net.aelysium.aelysiummod.protecao.network.PacoteDeletarRegiao;
import net.aelysium.aelysiummod.protecao.network.PacoteListaRegioes;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TelaListaRegioes extends Screen {

    private static final int COR_BORDA = 0xFF555555;
    private static final int COR_TITULO = 0xFFFFDD55;
    private static final int RAIO_CANTO = 6;
    private static final int LARGURA_TELA = 340;
    private static final int PADDING = 20;
    private static final int PADDING_TOPO = 35;
    private static final int ESPACAMENTO_Y = 26;
    private static final int ALTURA_BOTAO = 20;
    private static final int LARGURA_BOTAO_X = 20;
    private static final int ESPACO_ENTRE = 4;

    private static final ResourceLocation TEXTURA_MUD =
            ResourceLocation.withDefaultNamespace("textures/block/mud.png");

    private final List<PacoteListaRegioes.RegiaoResumo> regioes;
    private int posX, posY, alturaTelaCalculada;
    private int scrollOffset = 0;
    private int maxVisiveis;

    private String regiaoParaDeletar = null;

    public TelaListaRegioes(List<PacoteListaRegioes.RegiaoResumo> regioes) {
        super(Component.literal("Regiões Protegidas"));
        this.regioes = regioes;
    }

    @Override
    protected void init() {
        int larguraCampo = LARGURA_TELA - PADDING * 2;

        int alturaDisponivel = this.height - 80;
        maxVisiveis = Math.max(1, (alturaDisponivel - PADDING_TOPO - PADDING - ESPACAMENTO_Y) / ESPACAMENTO_Y);

        if (scrollOffset > Math.max(0, regioes.size() - maxVisiveis)) {
            scrollOffset = Math.max(0, regioes.size() - maxVisiveis);
        }

        int linhasExibidas = Math.min(regioes.size(), maxVisiveis);
        alturaTelaCalculada = PADDING_TOPO + linhasExibidas * ESPACAMENTO_Y + ESPACAMENTO_Y + PADDING;

        if (regioes.isEmpty()) {
            alturaTelaCalculada = PADDING_TOPO + ESPACAMENTO_Y * 2 + PADDING;
        }

        if (regiaoParaDeletar != null) {
            alturaTelaCalculada = PADDING_TOPO + ESPACAMENTO_Y * 3 + PADDING;
        }

        this.posX = (this.width - LARGURA_TELA) / 2;
        this.posY = (this.height - alturaTelaCalculada) / 2;

        int offsetY = posY + PADDING_TOPO;

        if (regiaoParaDeletar != null) {
            int larguraBotaoConf = (larguraCampo - 10) / 2;
            this.addRenderableWidget(Button.builder(
                    Component.literal("§c✔ Sim, deletar"),
                    btn -> confirmarDeletar()
            ).bounds(posX + PADDING, offsetY + ESPACAMENTO_Y, larguraBotaoConf, ALTURA_BOTAO).build());

            this.addRenderableWidget(Button.builder(
                    Component.literal("§a✖ Cancelar"),
                    btn -> cancelarDeletar()
            ).bounds(posX + PADDING + larguraBotaoConf + 10, offsetY + ESPACAMENTO_Y, larguraBotaoConf, ALTURA_BOTAO).build());
            return;
        }

        if (regioes.isEmpty()) {
            offsetY += ESPACAMENTO_Y;
        } else {
            int inicio = scrollOffset;
            int fim = Math.min(regioes.size(), scrollOffset + maxVisiveis);
            int larguraBotaoRegiao = larguraCampo - LARGURA_BOTAO_X - ESPACO_ENTRE;

            for (int i = inicio; i < fim; i++) {
                PacoteListaRegioes.RegiaoResumo regiao = regioes.get(i);
                String label = "§e" + regiao.nome() + " §7("
                        + regiao.posicaoMinima().getX() + "," + regiao.posicaoMinima().getY() + ","
                        + regiao.posicaoMinima().getZ() + ")";

                final int index = i;
                this.addRenderableWidget(Button.builder(
                        Component.literal(label),
                        btn -> abrirEdicao(regioes.get(index))
                ).bounds(posX + PADDING, offsetY, larguraBotaoRegiao, ALTURA_BOTAO).build());

                final String nomeRegiao = regiao.nome();
                this.addRenderableWidget(Button.builder(
                        Component.literal("§c✖"),
                        btn -> pedirConfirmacao(nomeRegiao)
                ).bounds(posX + PADDING + larguraBotaoRegiao + ESPACO_ENTRE, offsetY, LARGURA_BOTAO_X, ALTURA_BOTAO).build());

                offsetY += ESPACAMENTO_Y;
            }
        }

        int larguraBotaoFechar = 120;
        this.addRenderableWidget(Button.builder(
                Component.literal("✖ Fechar"),
                btn -> this.onClose()
        ).bounds(posX + (LARGURA_TELA - larguraBotaoFechar) / 2, offsetY, larguraBotaoFechar, ALTURA_BOTAO).build());
    }

    private void pedirConfirmacao(String nomeRegiao) {
        regiaoParaDeletar = nomeRegiao;
        this.rebuildWidgets();
    }

    private void confirmarDeletar() {
        if (regiaoParaDeletar != null) {
            PacketDistributor.sendToServer(new PacoteDeletarRegiao(regiaoParaDeletar));
            regioes.removeIf(r -> r.nome().equals(regiaoParaDeletar));
            regiaoParaDeletar = null;
            this.rebuildWidgets();
        }
    }

    private void cancelarDeletar() {
        regiaoParaDeletar = null;
        this.rebuildWidgets();
    }

    private void abrirEdicao(PacoteListaRegioes.RegiaoResumo resumo) {
        Regiao regiao = new Regiao(resumo.nome(), resumo.posicaoMinima(), resumo.posicaoMaxima());
        for (Map.Entry<FlagRegiao, Boolean> e : resumo.flags().entrySet()) {
            regiao.setFlag(e.getKey(), e.getValue());
        }
        for (UUID uuid : resumo.donos()) regiao.adicionarDono(uuid);
        regiao.setMensagemEntrada(resumo.mensagemEntrada());
        regiao.setMensagemSaida(resumo.mensagemSaida());
        Minecraft.getInstance().setScreen(new TelaConfigRegiao(regiao));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (regiaoParaDeletar != null) return false;
        if (regioes.size() > maxVisiveis) {
            scrollOffset -= (int) scrollY;
            scrollOffset = Math.max(0, Math.min(scrollOffset, regioes.size() - maxVisiveis));
            this.rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);
        renderizarPainelComMud(graphics);

        if (regiaoParaDeletar != null) {
            graphics.drawCenteredString(this.font, "§cDeletar região?",
                    posX + LARGURA_TELA / 2, posY + 12, COR_TITULO);
            graphics.drawCenteredString(this.font, "§f" + regiaoParaDeletar,
                    posX + LARGURA_TELA / 2, posY + PADDING_TOPO + 2, 0xFFFFFFFF);
        } else {
            String titulo = regioes.isEmpty()
                    ? "Nenhuma região criada"
                    : "Regiões Protegidas (" + regioes.size() + ")";
            graphics.drawCenteredString(this.font, titulo, posX + LARGURA_TELA / 2, posY + 12, COR_TITULO);

            if (regioes.size() > maxVisiveis) {
                String scrollInfo = "§7" + (scrollOffset + 1) + "-"
                        + Math.min(scrollOffset + maxVisiveis, regioes.size()) + " de " + regioes.size();
                graphics.drawCenteredString(this.font, scrollInfo, posX + LARGURA_TELA / 2, posY + 24, 0xFF888888);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderizarPainelComMud(GuiGraphics graphics) {
        int x1 = posX, y1 = posY, x2 = posX + LARGURA_TELA, y2 = posY + alturaTelaCalculada;
        int tam = 16;
        graphics.enableScissor(x1, y1, x2, y2);
        for (int ix = 0; ix <= LARGURA_TELA / tam + 1; ix++)
            for (int iy = 0; iy <= alturaTelaCalculada / tam + 1; iy++)
                graphics.blit(TEXTURA_MUD, x1 + ix * tam, y1 + iy * tam, 0, 0, tam, tam, tam, tam);
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