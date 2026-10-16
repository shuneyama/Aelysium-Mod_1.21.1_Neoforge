package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import net.aelysium.aelysiummod.sistemas.social.apelido.PredefinicaoApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.Pronome;
import net.aelysium.aelysiummod.sistemas.social.apelido.UtilitariosApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedePredefinicoesApelido;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class TelaPredefinicoesApelido extends Screen {

    private static final int LINHA_ALTURA = 26;
    private static final int LARGURA = 280;
    private static final int BOTAO_EXCLUIR = 18;
    private static final int TOPO = 40;

    private final Screen pai;
    private final Consumer<PredefinicaoApelido> aoEscolher;
    private List<PredefinicaoApelido> lista;
    private int rolagem = 0;
    private int esq;

    public TelaPredefinicoesApelido(Screen pai, List<PredefinicaoApelido> lista, Consumer<PredefinicaoApelido> aoEscolher) {
        super(Component.translatable("gui.aelysiummod.apelido.predef_lista_titulo"));
        this.pai = pai;
        this.lista = new ArrayList<>(lista);
        this.aoEscolher = aoEscolher;
    }

    public void atualizar(List<PredefinicaoApelido> nova) {
        this.lista = new ArrayList<>(nova);
    }

    @Override
    protected void init() {
        esq = this.width / 2 - LARGURA / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.insignia.voltar"), b -> onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    private int visiveis() {
        return Math.max(1, (this.height - TOPO - 40) / LINHA_ALTURA);
    }

    private MutableComponent previa(PredefinicaoApelido p) {
        MutableComponent r = Component.empty();
        if (!p.prefixo().isEmpty()) {
            int c1 = p.prefixoCor1() < 0 ? 0xFFFFFF : p.prefixoCor1();
            r.append(UtilitariosApelido.renderDualColor(p.prefixo(), c1, p.prefixoCor2(), p.prefixoFormato()));
            r.append(Component.literal(" "));
        }
        r.append(UtilitariosApelido.renderSingleColor(p.nick().isEmpty() ? "Nick" : p.nick(), p.nickCor(), p.nickFormato()));
        if (!p.sufixo().isEmpty()) {
            int c1 = p.sufixoCor1() < 0 ? 0xFFFFFF : p.sufixoCor1();
            r.append(Component.literal(" "));
            r.append(UtilitariosApelido.renderDualColor(p.sufixo(), c1, p.sufixoCor2(), p.sufixoFormato()));
        }
        Pronome pronome = Pronome.deId(p.pronomes());
        if (pronome != null) {
            r.append(Component.literal(" "));
            r.append(Component.literal("(" + pronome.texto + ")").withStyle(e -> e.withColor(TextColor.fromRgb(pronome.cor))));
        }
        return r;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        if (lista.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("gui.aelysiummod.apelido.predef_vazia"), this.width / 2, TOPO + 10, 0xAAAAAA);
        }

        int visiveis = visiveis();
        rolagem = Math.max(0, Math.min(rolagem, Math.max(0, lista.size() - visiveis)));
        for (int i = 0; i < visiveis && rolagem + i < lista.size(); i++) {
            PredefinicaoApelido p = lista.get(rolagem + i);
            int y = TOPO + i * LINHA_ALTURA;
            boolean sobre = mouseX >= esq && mouseX < esq + LARGURA - BOTAO_EXCLUIR - 6 && mouseY >= y && mouseY < y + LINHA_ALTURA - 2;
            g.fill(esq, y, esq + LARGURA, y + LINHA_ALTURA - 2, sobre ? 0xE02E2E2E : 0xE0000000);
            g.drawString(this.font, p.nome(), esq + 6, y + 3, 0xFFFFFF);
            g.drawString(this.font, previa(p), esq + 6, y + 14, 0xFFFFFF);

            int bx = esq + LARGURA - BOTAO_EXCLUIR - 2;
            int by = y + 2;
            boolean sobreX = mouseX >= bx && mouseX < bx + BOTAO_EXCLUIR && mouseY >= by && mouseY < by + BOTAO_EXCLUIR;
            net.aelysium.aelysiummod.nucleo.cliente.BotaoExcluir.desenhar(g, bx, by, BOTAO_EXCLUIR, BOTAO_EXCLUIR, sobreX);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        if (botao == 0) {
            for (int i = 0; i < visiveis() && rolagem + i < lista.size(); i++) {
                PredefinicaoApelido p = lista.get(rolagem + i);
                int y = TOPO + i * LINHA_ALTURA;
                int bx = esq + LARGURA - BOTAO_EXCLUIR - 2;
                int by = y + 2;
                if (mx >= bx && mx < bx + BOTAO_EXCLUIR && my >= by && my < by + BOTAO_EXCLUIR) {
                    PacketDistributor.sendToServer(new RedePredefinicoesApelido.PacoteExcluirPredefinicao(p.nome()));
                    return true;
                }
                if (mx >= esq && mx < esq + LARGURA - BOTAO_EXCLUIR - 6 && my >= y && my < y + LINHA_ALTURA - 2) {
                    aoEscolher.accept(p);
                    this.minecraft.setScreen(pai);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, botao);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        rolagem -= (int) Math.signum(sy);
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(pai);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
