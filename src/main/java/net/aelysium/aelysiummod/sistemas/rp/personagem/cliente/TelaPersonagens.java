package net.aelysium.aelysiummod.sistemas.rp.personagem.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.personagem.Personagem;
import net.aelysium.aelysiummod.sistemas.rp.personagem.rede.PacoteEscolherPersonagem;
import net.aelysium.aelysiummod.sistemas.rp.personagem.rede.PacoteExcluirPersonagem;
import net.aelysium.aelysiummod.sistemas.rp.personagem.rede.PacoteSolicitarPersonagens;
import net.aelysium.aelysiummod.sistemas.social.apelido.UtilitariosApelido;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class TelaPersonagens extends Screen {
    private static final int LARGURA = 196;
    private static final int ALTURA = 186;
    private static final int LINHA = 20;
    private static final int LINHAS_VISIVEIS = 6;
    private static final int LISTA_ALTURA = LINHAS_VISIVEIS * LINHA + 4;
    private static final int BARRA_LARGURA = 7;
    private static final int ICONE = 12;
    private static final String SEM_PERSONAGEM = "none";

    private List<Personagem> personagens = List.of();
    private String atual = SEM_PERSONAGEM;
    private int rolagem;
    private boolean arrastando;
    private int x0;
    private int y0;
    private Button botaoDesvincular;

    public TelaPersonagens() {
        super(Component.translatable("gui.aelysiummod.personagens.titulo"));
    }

    public static void receber(List<Personagem> personagens, String atual) {
        if (Minecraft.getInstance().screen instanceof TelaPersonagens tela) {
            tela.atualizar(personagens, atual);
        }
    }

    private void atualizar(List<Personagem> novos, String novoAtual) {
        this.personagens = novos;
        this.atual = novoAtual;
        this.rolagem = Mth.clamp(rolagem, 0, rolagemMaxima());
        if (botaoDesvincular != null) botaoDesvincular.active = !SEM_PERSONAGEM.equalsIgnoreCase(atual);
    }

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        int larguraBotao = (LARGURA - 20 - 4) / 2;
        int y = y0 + 22 + LISTA_ALTURA + 8;
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.personagens.criar"),
                        b -> minecraft.setScreen(new TelaEditarPersonagem(this, null)))
                .bounds(x0 + 10, y, larguraBotao, 20).build());
        botaoDesvincular = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.deuses.desabilitar"),
                        b -> PacketDistributor.sendToServer(new PacoteEscolherPersonagem(SEM_PERSONAGEM)))
                .bounds(x0 + 10 + larguraBotao + 4, y, larguraBotao, 20).build());
        botaoDesvincular.active = !SEM_PERSONAGEM.equalsIgnoreCase(atual);
        PacketDistributor.sendToServer(new PacoteSolicitarPersonagens());
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
        dicaIcone(g, mouseX, mouseY);
    }

    private int linhaY(int i) {
        return listaY() + 2 + i * LINHA - rolagem;
    }

    private int iconeEditarX() {
        return listaX() + listaLargura() - 2 - 2 * (ICONE + 2);
    }

    private int iconeExcluirX() {
        return listaX() + listaLargura() - 2 - (ICONE + 2);
    }

    private int iconeY(int i) {
        return linhaY(i) + (LINHA - 2 - ICONE) / 2;
    }

    private boolean dentro(double mx, double my, int x, int y, int l, int a) {
        return mx >= x && mx < x + l && my >= y && my < y + a;
    }

    private boolean naLista(double mx, double my) {
        return dentro(mx, my, listaX() + 1, listaY() + 1, listaLargura() - 2, LISTA_ALTURA - 2);
    }

    private void desenharLista(GuiGraphics g, int mouseX, int mouseY) {
        if (personagens.isEmpty()) {
            Component vazio = Component.translatable("gui.aelysiummod.personagens.vazio");
            g.drawString(font, vazio, listaX() + (listaLargura() - font.width(vazio)) / 2, listaY() + LISTA_ALTURA / 2 - 4, 0xFF404040, false);
            return;
        }
        g.enableScissor(listaX() + 1, listaY() + 1, listaX() + listaLargura() - 1, listaY() + LISTA_ALTURA - 1);
        for (int i = 0; i < personagens.size(); i++) {
            int y = linhaY(i);
            if (y + LINHA < listaY() || y > listaY() + LISTA_ALTURA) continue;
            Personagem p = personagens.get(i);
            int x = listaX() + 2;
            int l = listaLargura() - 4;
            boolean selecionado = p.id().equalsIgnoreCase(atual);
            boolean sobre = naLista(mouseX, mouseY) && mouseX >= x && mouseX < x + l && mouseY >= y && mouseY < y + LINHA - 2;
            g.fill(x, y, x + l, y + LINHA - 2, sobre ? 0xFFA8A8A8 : 0xFF909090);
            if (selecionado) {
                int borda = 0xFFFFFFFF;
                g.fill(x, y, x + l, y + 2, borda);
                g.fill(x, y + LINHA - 4, x + l, y + LINHA - 2, borda);
                g.fill(x, y, x + 2, y + LINHA - 2, borda);
                g.fill(x + l - 2, y, x + l, y + LINHA - 2, borda);
            }
            int ty = y + (LINHA - 2 - 8) / 2;
            Component nome = UtilitariosApelido.renderSingleColor(p.nome(), p.nickCor(), p.nickFormato());
            g.drawString(font, nome, x + 6, ty, 0xFFFFFFFF, true);
            TipoRaca raca = TipoRaca.fromId(p.raca());
            if (raca != TipoRaca.NONE) {
                int rx = iconeEditarX() - 4 - font.width(raca.displayName);
                if (rx > x + 8 + font.width(nome)) g.drawString(font, raca.displayName, rx, ty, 0xFF404040, false);
            }
            desenharIcone(g, iconeEditarX(), iconeY(i), "E", 0xFFFFFF, mouseX, mouseY);
            desenharIcone(g, iconeExcluirX(), iconeY(i), "X", 0xFF5555, mouseX, mouseY);
        }
        g.disableScissor();
    }

    private void desenharIcone(GuiGraphics g, int x, int y, String letra, int cor, int mouseX, int mouseY) {
        boolean sobre = naLista(mouseX, mouseY) && dentro(mouseX, mouseY, x, y, ICONE, ICONE);
        g.fill(x, y, x + ICONE, y + ICONE, 0xFF000000);
        g.fill(x + 1, y + 1, x + ICONE - 1, y + ICONE - 1, sobre ? 0xFF707070 : 0xFF505050);
        g.drawString(font, letra, x + (ICONE - font.width(letra)) / 2 + 1, y + 2, 0xFF000000 | cor, false);
    }

    private void dicaIcone(GuiGraphics g, int mouseX, int mouseY) {
        if (!naLista(mouseX, mouseY)) return;
        for (int i = 0; i < personagens.size(); i++) {
            if (dentro(mouseX, mouseY, iconeEditarX(), iconeY(i), ICONE, ICONE)) {
                g.renderTooltip(font, Component.translatable("gui.aelysiummod.personagens.editar"), mouseX, mouseY);
                return;
            }
            if (dentro(mouseX, mouseY, iconeExcluirX(), iconeY(i), ICONE, ICONE)) {
                g.renderTooltip(font, Component.translatable("gui.aelysiummod.personagens.excluir"), mouseX, mouseY);
                return;
            }
        }
    }

    private void desenharBarra(GuiGraphics g) {
        int max = rolagemMaxima();
        if (max <= 0) return;
        int bx = listaX() + listaLargura() + 2;
        int by = listaY();
        PainelGui.rebaixado(g, bx, by, BARRA_LARGURA, LISTA_ALTURA);
        int total = personagens.size() * LINHA + 4;
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
        return Math.max(0, personagens.size() * LINHA + 4 - LISTA_ALTURA);
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

    private void som() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void confirmarExclusao(Personagem p) {
        minecraft.setScreen(new ConfirmScreen(confirmado -> {
            if (confirmado) PacketDistributor.sendToServer(new PacoteExcluirPersonagem(p.id()));
            minecraft.setScreen(this);
        }, Component.translatable("gui.aelysiummod.personagens.excluir_titulo"),
                Component.translatable("gui.aelysiummod.personagens.excluir_mensagem", p.nome())));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int botao) {
        if (botao == 0) {
            if (naBarra(mouseX, mouseY)) {
                arrastando = true;
                rolarPara(mouseY);
                return true;
            }
            if (naLista(mouseX, mouseY)) {
                int indice = (int) ((mouseY - listaY() - 2 + rolagem) / LINHA);
                if (indice >= 0 && indice < personagens.size()) {
                    Personagem p = personagens.get(indice);
                    if (dentro(mouseX, mouseY, iconeEditarX(), iconeY(indice), ICONE, ICONE)) {
                        som();
                        minecraft.setScreen(new TelaEditarPersonagem(this, p));
                    } else if (dentro(mouseX, mouseY, iconeExcluirX(), iconeY(indice), ICONE, ICONE)) {
                        som();
                        confirmarExclusao(p);
                    } else if (!p.id().equalsIgnoreCase(atual)) {
                        som();
                        PacketDistributor.sendToServer(new PacoteEscolherPersonagem(p.id()));
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
    public void onClose() {
        if (minecraft.player != null) minecraft.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
        else super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
