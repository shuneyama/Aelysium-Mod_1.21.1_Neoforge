package net.aelysium.aelysiummod.sistemas.mundo.mochila.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.ConteudoMochila;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.DicaMochila;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

import java.util.ArrayList;
import java.util.List;

public class ClienteDicaMochila implements ClientTooltipComponent {

    private static final ResourceLocation BORDA = sprite("bundle_progressbar_border");
    private static final ResourceLocation PREENCHIMENTO = sprite("bundle_progressbar_fill");
    private static final ResourceLocation CHEIA_BARRA = sprite("bundle_progressbar_full");
    private static final ResourceLocation DESTAQUE_TRAS = sprite("slot_highlight_back");
    private static final ResourceLocation DESTAQUE_FRENTE = sprite("slot_highlight_front");
    private static final ResourceLocation FUNDO_SLOT = sprite("slot_background");
    private static final int TAMANHO_SLOT = 24;
    private static final int LARGURA_GRADE = 96;
    private static final int ALTURA_BARRA = 13;
    private static final int PREENCHIMENTO_MAX = 94;
    private static final int MARGEM_Y = 4;
    private static final Component TEXTO_CHEIA = Component.translatable("item.aelysiummod.mochila.full");
    private static final Component TEXTO_VAZIA = Component.translatable("item.aelysiummod.mochila.empty");
    private static final Component DESCRICAO_VAZIA = Component.translatable("item.aelysiummod.mochila.empty.description");

    private final BundleContents conteudo;
    private final int selecionado;

    public ClienteDicaMochila(DicaMochila dica) {
        this.conteudo = dica.conteudo();
        this.selecionado = dica.selecionado();
    }

    private static ResourceLocation sprite(String nome) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "container/mochila/" + nome);
    }

    @Override
    public int getHeight() {
        Font fonte = Minecraft.getInstance().font;
        return conteudo.isEmpty() ? alturaDescricao(fonte) + ALTURA_BARRA + 8 : alturaGrade() + ALTURA_BARRA + 8;
    }

    @Override
    public int getWidth(Font fonte) {
        return LARGURA_GRADE;
    }

    private int linhasGrade() {
        return Mth.positiveCeilDiv(Math.min(ConteudoMochila.GRADE_MAX, conteudo.size()), ConteudoMochila.GRADE_X);
    }

    private int alturaGrade() {
        return linhasGrade() * TAMANHO_SLOT;
    }

    private static int alturaDescricao(Font fonte) {
        return fonte.split(DESCRICAO_VAZIA, LARGURA_GRADE).size() * 9;
    }

    @Override
    public void renderImage(Font fonte, int x, int y, GuiGraphics graficos) {
        Fraction peso = conteudo.weight();
        if (conteudo.isEmpty()) {
            graficos.drawWordWrap(fonte, DESCRICAO_VAZIA, x, y, LARGURA_GRADE, 0xFFAAAAAA);
            barra(x, y + alturaDescricao(fonte) + MARGEM_Y, fonte, graficos, Fraction.ZERO);
            return;
        }
        int total = conteudo.size();
        boolean excedeu = total > ConteudoMochila.GRADE_MAX;
        int mostrar = ConteudoMochila.quantidadeMostrar(total);
        List<ItemStack> itens = new ArrayList<>();
        for (int i = 0; i < mostrar; i++) itens.add(conteudo.getItemUnsafe(i));
        int inicioX = x + LARGURA_GRADE;
        int inicioY = y + alturaGrade();
        int numero = 1;
        for (int linha = 1; linha <= linhasGrade(); linha++) {
            for (int coluna = 1; coluna <= ConteudoMochila.GRADE_X; coluna++) {
                int desenharX = inicioX - coluna * TAMANHO_SLOT;
                int desenharY = inicioY - linha * TAMANHO_SLOT;
                if (excedeu && coluna * linha == 1) {
                    int ocultos = 0;
                    for (int i = mostrar; i < total; i++) ocultos += conteudo.getItemUnsafe(i).getCount();
                    graficos.drawCenteredString(fonte, "+" + ocultos, desenharX + 12, desenharY + 10, -1);
                } else if (itens.size() >= numero) {
                    slot(numero, desenharX, desenharY, itens, fonte, graficos);
                    numero++;
                }
            }
        }
        nomeSelecionado(fonte, graficos, x, y);
        barra(x, y + alturaGrade() + MARGEM_Y, fonte, graficos, peso);
    }

    private void slot(int numero, int x, int y, List<ItemStack> itens, Font fonte, GuiGraphics graficos) {
        int indice = itens.size() - numero;
        boolean destaque = indice == selecionado;
        ItemStack pilha = itens.get(indice);
        graficos.blitSprite(destaque ? DESTAQUE_TRAS : FUNDO_SLOT, x, y, TAMANHO_SLOT, TAMANHO_SLOT);
        graficos.renderItem(pilha, x + 4, y + 4, numero);
        graficos.renderItemDecorations(fonte, pilha, x + 4, y + 4);
        if (destaque) {
            graficos.blitSprite(DESTAQUE_FRENTE, x, y, 0, TAMANHO_SLOT, TAMANHO_SLOT);
        }
    }

    private void nomeSelecionado(Font fonte, GuiGraphics graficos, int x, int y) {
        if (selecionado < 0 || selecionado >= conteudo.size()) return;
        ItemStack pilha = conteudo.getItemUnsafe(selecionado);
        Component nome = pilha.getHoverName();
        int largura = fonte.width(nome.getVisualOrderText());
        int centro = x + LARGURA_GRADE / 2 - 12;
        graficos.renderTooltip(fonte, nome, centro - largura / 2 - 12, y - 15 + 12);
    }

    private static void barra(int x, int y, Font fonte, GuiGraphics graficos, Fraction peso) {
        int preenchimento = Mth.clamp(Mth.mulAndTruncate(peso, PREENCHIMENTO_MAX), 0, PREENCHIMENTO_MAX);
        ResourceLocation textura = peso.compareTo(Fraction.ONE) >= 0 ? CHEIA_BARRA : PREENCHIMENTO;
        graficos.blitSprite(textura, x + 1, y, preenchimento, ALTURA_BARRA);
        graficos.blitSprite(BORDA, x, y, LARGURA_GRADE, ALTURA_BARRA);
        Component texto = null;
        if (peso.compareTo(Fraction.ZERO) == 0) {
            texto = TEXTO_VAZIA;
        } else if (peso.compareTo(Fraction.ONE) >= 0) {
            texto = TEXTO_CHEIA;
        }
        if (texto != null) {
            graficos.drawCenteredString(fonte, texto, x + 48, y + 3, -1);
        }
    }
}
