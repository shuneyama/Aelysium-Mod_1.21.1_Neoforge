package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteEscolherDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteListaDeuses;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteSolicitarDeuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class TelaDeuses extends Screen {
    private static final int LARGURA = 196;
    private static final int ALTURA = 186;
    private static final int LINHA = 20;
    private static final int LINHAS_VISIVEIS = 6;
    private static final int LISTA_ALTURA = LINHAS_VISIVEIS * LINHA + 4;
    private static final int BARRA_LARGURA = 7;
    private static final String SEM_DEUS = "none";

    private List<PacoteListaDeuses.Entrada> deuses = List.of();
    private String atual = SEM_DEUS;
    private int rolagem;
    private boolean arrastando;
    private int x0;
    private int y0;
    private Button botaoDesabilitar;

    public TelaDeuses() {
        super(Component.translatable("gui.aelysiummod.deuses.titulo"));
    }

    public static void receber(List<PacoteListaDeuses.Entrada> deuses, String atual) {
        if (Minecraft.getInstance().screen instanceof TelaDeuses tela) {
            tela.atualizar(deuses, atual);
        }
    }

    private void atualizar(List<PacoteListaDeuses.Entrada> novos, String novoAtual) {
        this.deuses = novos;
        this.atual = novoAtual;
        this.rolagem = Mth.clamp(rolagem, 0, rolagemMaxima());
        if (botaoDesabilitar != null) botaoDesabilitar.active = !SEM_DEUS.equalsIgnoreCase(atual);
    }

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        int larguraBotao = (LARGURA - 20 - 4) / 2;
        int y = y0 + 22 + LISTA_ALTURA + 8;
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.deuses.criar"),
                        b -> minecraft.setScreen(new TelaCriarDeus()))
                .bounds(x0 + 10, y, larguraBotao, 20).build());
        botaoDesabilitar = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.deuses.desabilitar"),
                        b -> PacketDistributor.sendToServer(new PacoteEscolherDeus(SEM_DEUS)))
                .bounds(x0 + 10 + larguraBotao + 4, y, larguraBotao, 20).build());
        botaoDesabilitar.active = !SEM_DEUS.equalsIgnoreCase(atual);
        PacketDistributor.sendToServer(new PacoteSolicitarDeuses());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.renderBackground(g, mouseX, mouseY, parcial);
        PainelGui.painel(g, x0, y0, LARGURA, ALTURA);
        PainelGui.rebaixado(g, listaX(), listaY(), listaLargura(), LISTA_ALTURA);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.render(g, mouseX, mouseY, parcial);
        g.drawString(font, title, x0 + (LARGURA - font.width(title)) / 2, y0 + 8, 0xFF404040, false);
        desenharLista(g, mouseX, mouseY);
        desenharBarra(g);
    }

    private void desenharLista(GuiGraphics g, int mouseX, int mouseY) {
        if (deuses.isEmpty()) {
            Component vazio = Component.translatable("gui.aelysiummod.deuses.vazio");
            g.drawString(font, vazio, listaX() + (listaLargura() - font.width(vazio)) / 2, listaY() + LISTA_ALTURA / 2 - 4, 0xFF404040, false);
            return;
        }
        g.enableScissor(listaX() + 1, listaY() + 1, listaX() + listaLargura() - 1, listaY() + LISTA_ALTURA - 1);
        for (int i = 0; i < deuses.size(); i++) {
            int y = listaY() + 2 + i * LINHA - rolagem;
            if (y + LINHA < listaY() || y > listaY() + LISTA_ALTURA) continue;
            PacoteListaDeuses.Entrada deus = deuses.get(i);
            int x = listaX() + 2;
            int l = listaLargura() - 4;
            boolean selecionado = deus.id().equalsIgnoreCase(atual);
            boolean sobre = mouseX >= x && mouseX < x + l && mouseY >= Math.max(y, listaY() + 1) && mouseY < Math.min(y + LINHA - 2, listaY() + LISTA_ALTURA - 1);
            int fundo = sobre ? 0xFFA8A8A8 : 0xFF909090;
            g.fill(x, y, x + l, y + LINHA - 2, fundo);
            if (selecionado) {
                int borda = 0xFF000000 | deus.cor();
                g.fill(x, y, x + l, y + 2, borda);
                g.fill(x, y + LINHA - 4, x + l, y + LINHA - 2, borda);
                g.fill(x, y, x + 2, y + LINHA - 2, borda);
                g.fill(x + l - 2, y, x + l, y + LINHA - 2, borda);
            }
            int quadrado = y + (LINHA - 2 - 8) / 2;
            g.fill(x + 5, quadrado, x + 13, quadrado + 8, 0xFF000000);
            g.fill(x + 6, quadrado + 1, x + 12, quadrado + 7, 0xFF000000 | deus.cor());
            PainelGui.textoContorno(g, font, deus.nome(), x + 19, y + (LINHA - 2 - 8) / 2, deus.cor());
        }
        g.disableScissor();
    }

    private void desenharBarra(GuiGraphics g) {
        int max = rolagemMaxima();
        if (max <= 0) return;
        int bx = listaX() + listaLargura() + 2;
        int by = listaY();
        PainelGui.rebaixado(g, bx, by, BARRA_LARGURA, LISTA_ALTURA);
        int total = deuses.size() * LINHA + 4;
        int altura = Math.max(12, (int) ((long) (LISTA_ALTURA - 2) * LISTA_ALTURA / total));
        int topo = by + 1 + (int) ((long) (LISTA_ALTURA - 2 - altura) * rolagem / max);
        PainelGui.painel(g, bx + 1, topo, BARRA_LARGURA - 2, altura);
    }

    private int listaX() {
        return x0 + 10;
    }

    private int listaY() {
        return y0 + 22;
    }

    private int listaLargura() {
        return LARGURA - 20 - BARRA_LARGURA - 2;
    }

    private int rolagemMaxima() {
        return Math.max(0, deuses.size() * LINHA + 4 - LISTA_ALTURA);
    }

    private boolean naBarra(double mouseX, double mouseY) {
        int bx = listaX() + listaLargura() + 2;
        return rolagemMaxima() > 0 && mouseX >= bx && mouseX < bx + BARRA_LARGURA && mouseY >= listaY() && mouseY < listaY() + LISTA_ALTURA;
    }

    private void rolarPara(double mouseY) {
        int max = rolagemMaxima();
        double fracao = (mouseY - listaY()) / LISTA_ALTURA;
        rolagem = Mth.clamp((int) (fracao * (max + LISTA_ALTURA) - LISTA_ALTURA / 2.0), 0, max);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int botao) {
        if (botao == 0) {
            if (naBarra(mouseX, mouseY)) {
                arrastando = true;
                rolarPara(mouseY);
                return true;
            }
            if (mouseX >= listaX() + 2 && mouseX < listaX() + listaLargura() - 2 && mouseY >= listaY() + 1 && mouseY < listaY() + LISTA_ALTURA - 1) {
                int indice = (int) ((mouseY - listaY() - 2 + rolagem) / LINHA);
                if (indice >= 0 && indice < deuses.size()) {
                    PacoteListaDeuses.Entrada deus = deuses.get(indice);
                    if (!deus.id().equalsIgnoreCase(atual)) {
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        PacketDistributor.sendToServer(new PacoteEscolherDeus(deus.id()));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, botao);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int botao, double dx, double dy) {
        if (arrastando) {
            rolarPara(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, botao, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int botao) {
        arrastando = false;
        return super.mouseReleased(mouseX, mouseY, botao);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        rolagem = Mth.clamp(rolagem - (int) (scrollY * (LINHA / 2.0)), 0, rolagemMaxima());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
