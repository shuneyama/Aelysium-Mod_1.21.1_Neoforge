package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteEscolherRaca;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class TelaSelecaoRaca extends Screen {

    private static final List<TipoRaca> RACAS = List.of(
            TipoRaca.DRACONO, TipoRaca.ELVARIN, TipoRaca.FAUNARI, TipoRaca.HUMANO, TipoRaca.TIEFLING,
            TipoRaca.UNDYNE, TipoRaca.VALKYRIA);

    private static final java.util.Map<TipoRaca, Integer> COR_RACA = java.util.Map.of(
            TipoRaca.HUMANO, 0xE8761A,
            TipoRaca.ELVARIN, 0x22A902,
            TipoRaca.FAUNARI, 0xFF8FC8,
            TipoRaca.DRACONO, 0x9224BF,
            TipoRaca.TIEFLING, 0xB22222,
            TipoRaca.UNDYNE, 0x02A9A9,
            TipoRaca.VALKYRIA, 0xFFEE22,
            TipoRaca.ROBO, 0x7C8A99);

    private static final int INFO_L = 157;
    private static final int INFO_A = 92;
    private static final int ABA_L = 59;
    private static final int INFO_X = 10;
    private static final int INFO_Y = 10;
    private static final int DESC_X = 9;
    private static final int DESC_Y = 5;
    private static final int DESC_L = 73;
    private static final int DESC_A = 62;
    private static final int MENU_X0 = 7;
    private static final int MENU_L = 19;
    private static final int MENU_A = 8;
    private static final int MENU_Y = 7;
    private static final int MENU_ESP = 10;
    private static final int DIV_Y = 17;
    private static final int CONT_X = 7;
    private static final int CONT_Y = 21;
    private static final int CONT_A = 45;
    private static final ResourceLocation BOTAO_MENU = tex("painel/botao_menu");
    private static final int CONT_L = 77;
    private static final int ICONE = 24;
    private static final ResourceLocation MOLDURA_EFEITO = ResourceLocation.withDefaultNamespace("hud/effect_background");
    private static final int COR_DIVISOR = 0xFFAD84AD;
    private static final int[] COR_MENU = {0xFF5CD65C, 0xFFE05C5C, 0xFF5CB8E8};
    private int menuVerso;
    private static final int LER_X = 10, LER_Y = 49;
    private static final int CARD_X = 106;
    private static final int CARD_Y = 7;
    private static final int CONF_X = 106;
    private static final int CONF_Y = 73;

    private static final ResourceLocation PAINEL = tex("painel/moldura");
    private static final ResourceLocation FUNDO_INFO = tex("painel/fundo");
    private static final ResourceLocation ABA = tex("aba/aba");
    private static final ResourceLocation ELEM_INFO = tex("painel/informacoes");
    private static final ResourceLocation FUNDO_TEXTO = tex("painel/informacoes_fundo");
    private static final ResourceLocation ELEM_CARD = tex("painel/card");
    private static final ResourceLocation ELEM_CONFIRMAR = tex("painel/confirmar");
    private static final ResourceLocation ELEM_CONFIRMAR_SEL = tex("painel/confirmar_selecionado");
    private static final ResourceLocation LER_MAIS = tex("painel/ler_mais");
    private static final ResourceLocation LER_MAIS_SEL = tex("painel/ler_mais_hover");
    private static final ResourceLocation LER_MAIS_CLIQUE = tex("painel/ler_mais_clicado");
    private boolean lerMaisApertado;
    private static final ResourceLocation FUNDO_TELA = tex("tela/fundo");
    private static final ResourceLocation MOLDURA_TELA = tex("tela/moldura");
    private static final ResourceLocation MOLDURA_JOGADOR = tex("jogador/moldura");
    private static final ResourceLocation FUNDO_JOGADOR = tex("jogador/fundo");

    private final float[] hover = new float[RACAS.size()];
    private final long[] clique = new long[RACAS.size()];
    private long ultimoQuadro;
    private long inicioBamboleio = System.nanoTime();
    private int selecionada;
    private int larguraAba;
    private int painelLargura;
    private int tabOff;
    private float escArte;
    private int q = 1;
    private int infoXr;
    private int infoYr;
    private int playerXr;
    private int playerYr;
    private float escalaTela = 1.0F;
    private float pxPorUnidade = 1.0F;
    private int molK = 1;
    private int molOx;
    private int molOy;
    private boolean verMais;
    private float giro;
    private long ultimoGiro;
    private boolean sobreConfirmarAtual;
    private int vw;
    private int vh;

    public TelaSelecaoRaca(String atual) {
        super(Component.translatable("gui.aelysiummod.raca.titulo"));
        TipoRaca raca = TipoRaca.fromId(atual);
        int indice = RACAS.indexOf(raca);
        this.selecionada = Math.max(indice, 0);
    }

    private static ResourceLocation tex(String caminho) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/raca/" + caminho + ".png");
    }

    private static ResourceLocation banner(TipoRaca raca) {
        return tex("banner/" + raca.id);
    }

    private static ItemStack item(TipoRaca raca) {
        return switch (raca) {
            case HUMANO -> new ItemStack(Items.EXPERIENCE_BOTTLE);
            case ELVARIN -> new ItemStack(Items.ENCHANTED_BOOK);
            case FAUNARI -> new ItemStack(Items.RABBIT_FOOT);
            case DRACONO -> new ItemStack(Items.DRAGON_HEAD);
            case TIEFLING -> new ItemStack(Items.FIRE_CHARGE);
            case UNDYNE -> new ItemStack(Items.NAUTILUS_SHELL);
            case VALKYRIA -> new ItemStack(Items.ELYTRA);
            default -> new ItemStack(net.aelysium.aelysiummod.registro.ModItens.BATERIA.get());
        };
    }

    private TipoRaca racaAtual() {
        return RACAS.get(selecionada);
    }

    @Override
    protected void init() {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        pxPorUnidade = Math.max(1.0F, (float) Math.floor(guiScale * Math.min(width / 480.0F, height / 270.0F)));
        escalaTela = (float) (pxPorUnidade / guiScale);
        com.mojang.blaze3d.platform.Window janela = Minecraft.getInstance().getWindow();
        molK = Math.max(1, Math.min(janela.getWidth() / 127, janela.getHeight() / 71));
        molOx = (janela.getWidth() - 127 * molK) / 2;
        molOy = (janela.getHeight() - 71 * molK) / 2;
        vw = Math.round(width / escalaTela);
        vh = Math.round(height / escalaTela);
        Minecraft.getInstance().getTextureManager().getTexture(FUNDO_TELA).setFilter(true, false);
        prepararMascara();
        int innerW = 115 * molK;
        int innerL = molOx + 6 * molK;
        int s0 = Math.max(1, innerW / 243);
        escArte = Math.max(s0, (innerW - 60) / 222.0F);
        int gap = Math.round((innerW - 222 * escArte) / 3.0F);
        playerXr = innerL + gap;
        infoXr = playerXr + Math.round(65 * escArte) + gap;
        q = Math.max(1, (int) ((INFO_A - 10) * escArte / 166));
        int baseR = molOy + 63 * molK;
        playerYr = baseR - Math.round(99 * escArte);
        infoYr = baseR - Math.round(INFO_A * escArte);
        int desvioY = ((Math.round(5 * escArte) + q - 1) / q) * q;
        painelLargura = (int) (147 * escArte / q);
        larguraAba = (painelLargura - 16) / RACAS.size();
        tabOff = (painelLargura - 16 - larguraAba * RACAS.size()) / 2;

    }

    private void confirmar() {
        PacketDistributor.sendToServer(new PacoteEscolherRaca(racaAtual().id));
        onClose();
    }

    private void alternarVerso(boolean valor) {
        if (verMais == valor) return;
        verMais = valor;
        rolagem = 0.0F;
        inicioBamboleio = System.nanoTime();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.75F, 0.8F));
    }

    private void selecionar(int indice) {
        int novo = Math.floorMod(indice, RACAS.size());
        if (novo == selecionada) return;
        selecionada = novo;
        rolagem = 0.0F;
        verMais = false;
        inicioBamboleio = System.nanoTime();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.75F, 0.8F));
    }

    private void tingir(GuiGraphics g, int argb) {
        g.setColor(((argb >> 16) & 255) / 255.0F, ((argb >> 8) & 255) / 255.0F, (argb & 255) / 255.0F, 1.0F);
    }

    private static int cor(TipoRaca raca) {
        return 0xFF000000 | COR_RACA.getOrDefault(raca, 0xAAAAAA);
    }

    private static int claro(int argb) {
        return misturar(argb, 0xFFFFFFFF, 0.45F);
    }

    private static int escurecer(int argb, float f) {
        return misturar(argb, 0xFF000000, 1.0F - f);
    }

    private static int misturar(int a, int b, float f) {
        int r = (int) (((a >> 16) & 255) * (1 - f) + ((b >> 16) & 255) * f);
        int g = (int) (((a >> 8) & 255) * (1 - f) + ((b >> 8) & 255) * f);
        int bl = (int) ((a & 255) * (1 - f) + (b & 255) * f);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        mx /= escalaTela;
        my /= escalaTela;
        if (botao == 0) {
            if (verMais) {
                calcularBotoes();
                for (int i = 0; i < 3; i++) {
                    if (sobreArte(mx, my, botaoX[i], INFO_Y + MENU_Y, botaoL[i], MENU_A)) {
                        if (menuVerso != i) {
                            menuVerso = i;
                            rolagem = 0.0F;
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        }
                        return true;
                    }
                }
            }
            if (sobreLerMais(mx, my)) {
                lerMaisApertado = true;
                alternarVerso(!verMais);
                return true;
            }
            if (sobreConfirmar(mx, my)) {
                confirmar();
                return true;
            }
        }
        if (botao == 0) {
            for (int i = 0; i < RACAS.size(); i++) {
                if (sobreAba(i, (int) mx, (int) my)) {
                    clique[i] = System.nanoTime();
                    inicioBamboleio = clique[i];
                    selecionar(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, botao);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int botao) {
        lerMaisApertado = false;
        return super.mouseReleased(mx, my, botao);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        mx /= escalaTela;
        my /= escalaTela;
        if (dy != 0 && alturaTexto > alturaVisivel() && sobreArte(mx, my, INFO_X, INFO_Y, 91, 72)) {
            rolagem = Math.max(0.0F, Math.min(alturaTexto - alturaVisivel(), rolagem - (float) dy * 30.0F * escalaTexto));
            return true;
        }
        return super.mouseScrolled(mx, my, dx, dy);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int modificadores) {
        if (tecla == GLFW.GLFW_KEY_LEFT) {
            selecionar(selecionada - 1);
            return true;
        }
        if (tecla == GLFW.GLFW_KEY_RIGHT) {
            selecionar(selecionada + 1);
            return true;
        }
        return super.keyPressed(tecla, scan, modificadores);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float parcial) {
        g.pose().pushPose();
        g.pose().scale(escalaTela, escalaTela, 1.0F);
        mx = (int) (mx / escalaTela);
        my = (int) (my / escalaTela);
        super.render(g, mx, my, parcial);

        desenharJogador(g, mx, my);

        float su = escArte / pxPorUnidade;
        g.pose().pushPose();
        g.pose().translate(infoXr / pxPorUnidade, infoYr / pxPorUnidade, 0.0F);
        g.pose().scale(su, su, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(FUNDO_INFO, 5, 5, 0.0F, 0.0F, INFO_L - 10, INFO_A - 10, INFO_L - 10, INFO_A - 10);
        g.blit(PAINEL, 0, 0, 0.0F, 0.0F, INFO_L, INFO_A, INFO_L, INFO_A);
        g.blit(FUNDO_TEXTO, INFO_X, INFO_Y, 0.0F, 0.0F, 91, 72, 91, 72);
        g.blit(ELEM_INFO, INFO_X, INFO_Y, 0.0F, 0.0F, 91, 72, 91, 72);
        desenharBanner(g);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(ELEM_CARD, CARD_X, CARD_Y, 0.0F, 0.0F, 39, 63, 39, 63);
        boolean sobreLer = sobreLerMais(mx, my);
        boolean apertado = sobreLer && lerMaisApertado;
        g.pose().pushPose();
        g.pose().translate(CARD_X + 19.5F, CARD_Y + 31.5F, 0.0F);
        g.pose().scale(sxCard, 1.0F, 1.0F);
        g.pose().translate(LER_X - 19.5F, LER_Y - 31.5F, 0.0F);
        g.blit(apertado ? LER_MAIS_CLIQUE : sobreLer ? LER_MAIS_SEL : LER_MAIS, 0, 0, 0.0F, 0.0F, 19, 8, 19, 8);
        g.pose().popPose();
        sobreConfirmarAtual = sobreConfirmar(mx, my);
        g.blit(sobreConfirmarAtual ? ELEM_CONFIRMAR_SEL : ELEM_CONFIRMAR, CONF_X, CONF_Y, 0.0F, 0.0F, 39, 12, 39, 12);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int qe = escalaConteudo();
        float qc = qe / escArte;

        g.pose().pushPose();
        float qt = escalaTextoAjustada(qc);
        int areaX = verMais ? INFO_X + CONT_X : INFO_X + DESC_X;
        int areaY = verMais ? INFO_Y + CONT_Y : INFO_Y + DESC_Y;
        int areaL = verMais ? CONT_L : DESC_L;
        int areaA = alturaVisivel();
        boolean rolando = alturaTexto > areaA;
        if (rolando) {
            rolagem = Math.max(0.0F, Math.min(rolagem, alturaTexto - areaA));
            int rx1 = (int) Math.ceil(infoXr + areaX * escArte);
            int rx2 = (int) Math.floor(infoXr + (areaX + areaL) * escArte);
            int ry1 = (int) Math.ceil(infoYr + areaY * escArte);
            int ry2 = (int) Math.floor(infoYr + (areaY + areaA) * escArte);
            g.flush();
            com.mojang.blaze3d.systems.RenderSystem.enableScissor(rx1, minecraft.getWindow().getHeight() - ry2, Math.max(0, rx2 - rx1), Math.max(0, ry2 - ry1));
        }
        g.pose().translate(fixar(areaX), fixar(areaY + (rolando ? -rolagem : (areaA - alturaTexto) / 2.0F)), 0.0F);
        g.pose().scale(qt, qt, 1.0F);
        desenharTexto(g, (int) (areaL / qt));
        g.pose().popPose();
        if (rolando) {
            g.flush();
            com.mojang.blaze3d.systems.RenderSystem.disableScissor();
            float trilho = areaA;
            float alca = Math.max(6.0F, trilho * areaA / alturaTexto);
            int ay = areaY + Math.round((trilho - alca) * rolagem / (alturaTexto - areaA));
            int barraX = areaX + areaL + (verMais ? 1 : 2);
            g.fill(barraX, ay, barraX + 1, ay + Math.round(alca), COR_DIVISOR);
        }
        if (verMais) desenharMenus(g, mx, my, qt);

        g.pose().pushPose();
        g.pose().translate(fixar(CONF_X + 19.5F), fixar(CONF_Y + (sobreConfirmarAtual ? 6.0F : 5.0F)), 0.0F);
        g.pose().scale(qc, qc, 1.0F);
        Component rotulo = Component.translatable("gui.aelysiummod.raca.escolher");
        g.drawString(font, rotulo, -font.width(rotulo) / 2, -4, sobreConfirmarAtual ? 0xFFFFFFFF : 0xFFE9DDC6, true);
        g.pose().popPose();

        g.pose().pushPose();
        g.pose().translate(CARD_X + 19.5F, CARD_Y + 31.5F, 0.0F);
        g.pose().scale(sxCard, 1.0F, 1.0F);
        g.pose().translate(LER_X - 10.0F, LER_Y - 27.2F, 0.0F);
        g.pose().scale(qc, qc, 1.0F);
        Component rotuloLer = Component.translatable("gui.aelysiummod.raca.ver_mais");
        g.drawString(font, rotuloLer, -font.width(rotuloLer) / 2, -4, sobreLer && !apertado ? 0xFF000000 : 0xFFE9DDC6, !(sobreLer && !apertado));
        g.pose().popPose();
        g.pose().popPose();
        desenharAbas(g, mx, my);
        g.pose().popPose();
    }

    private void desenharJogador(GuiGraphics g, int mx, int my) {
        if (minecraft == null || minecraft.player == null) return;
        float su = escArte / pxPorUnidade;
        float px = playerXr / pxPorUnidade;
        float py = playerYr / pxPorUnidade;
        g.pose().pushPose();
        g.pose().translate(px, py, 0.0F);
        g.pose().scale(su, su, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(FUNDO_JOGADOR, 0, 0, 0.0F, 0.0F, 65, 99, 65, 99);
        g.blit(MOLDURA_JOGADOR, 0, 0, 0.0F, 0.0F, 65, 99, 65, 99);
        g.pose().popPose();
        int x1 = Math.round(px + 2 * su);
        int x2 = Math.round(px + 63 * su);
        int y1 = Math.round(py + 3 * su);
        int y2 = Math.round(py + 82 * su);
        float inverso = 1.0F / escalaTela;
        int gx1 = Math.round(x1 * escalaTela);
        int gx2 = Math.round(x2 * escalaTela);
        int gy1 = Math.round(y1 * escalaTela);
        int gy2 = Math.round(y2 * escalaTela);
        int escala = Math.max(1, (int) Math.min((gy2 - gy1) * 0.36F, (gx2 - gx1) * 0.62F));
        g.pose().pushPose();
        g.pose().scale(inverso, inverso, 1.0F);
        g.pose().translate(0.0F, 0.0F, 200.0F);
        desenharSentado(g, gx1, gy1, gx2, gy2, escala, Math.round(mx * escalaTela), Math.round(my * escalaTela));
        g.pose().popPose();

        Component nome = minecraft.player.getName();
        int largura = Math.max(1, font.width(nome));
        float faixaAltura = 13.0F * escArte;
        float faixaLargura = 55.0F * escArte;
        int qn = Math.max(1, (int) Math.min(faixaAltura * 0.36F / 8.0F, faixaLargura * 0.9F / largura));
        float centroX = playerXr + 32.5F * escArte;
        float centroY = playerYr + 89.5F * escArte;
        g.pose().pushPose();
        g.pose().scale(1.0F / pxPorUnidade, 1.0F / pxPorUnidade, 1.0F);
        g.pose().translate(Math.round(centroX - largura * qn / 2.0F), Math.round(centroY - 3.5F * qn), 0.0F);
        g.pose().scale(qn, qn, 1.0F);
        g.drawString(font, nome, 0, 0, 0xFFFFFFFF, true);
        g.pose().popPose();
    }

private void desenharSentado(GuiGraphics g, int gx1, int gy1, int gx2, int gy2, int escala, int rmx, int rmy) {
    net.minecraft.client.player.LocalPlayer jogador = minecraft.player;
    net.minecraft.client.renderer.entity.EntityRenderer<?> base = minecraft.getEntityRenderDispatcher().getRenderer(jogador);
    if (!(base instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer renderizador)) return;
    net.minecraft.client.model.PlayerModel<net.minecraft.client.player.AbstractClientPlayer> modelo = renderizador.getModel();
    modelo.setAllVisible(true);
    modelo.hat.visible = jogador.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.HAT);
    modelo.jacket.visible = jogador.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.JACKET);
    modelo.leftSleeve.visible = jogador.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE);
    modelo.rightSleeve.visible = jogador.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE);
    modelo.leftPants.visible = jogador.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.LEFT_PANTS_LEG);
    modelo.rightPants.visible = jogador.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.RIGHT_PANTS_LEG);
    modelo.young = false;
    modelo.crouching = false;
    modelo.riding = false;
    if (inicioPose == 0) inicioPose = System.nanoTime();
    float cx = (gx1 + gx2) / 2.0F;
    float cy = (gy1 + gy2) / 2.0F;
    double alvoGuinada = -Math.max(-1.0, Math.min(1.0, (rmx - cx) / ((gx2 - gx1) * 1.2))) * 40.0;
    double alvoArfagem = Math.max(-1.0, Math.min(1.0, (rmy - (cy - (gy2 - gy1) * 0.25)) / ((gy2 - gy1) * 0.8))) * 25.0;
    long agora = System.nanoTime();
    double dt = ultimoOlhar == 0 ? 0.0 : Math.min(0.1, (agora - ultimoOlhar) / 1.0E9);
    ultimoOlhar = agora;
    double k = 1.0 - Math.exp(-9.0 * dt);
    guinadaAtual += (alvoGuinada - guinadaAtual) * k;
    arfagemAtual += (alvoArfagem - arfagemAtual) * k;
    PoseSentada.aplicar(modelo, (agora - inicioPose) / 1.0E9, guinadaAtual, arfagemAtual);

    g.enableScissor(gx1, gy1, gx2, gy2);
    com.mojang.blaze3d.vertex.PoseStack ps = g.pose();
    ps.pushPose();
    ps.translate(cx, cy, 50.0F);
    ps.scale(escala, escala, -escala);
    ps.translate(0.0F, 1.0625F, 0.0F);
    ps.mulPose(Axis.ZP.rotationDegrees(180.0F));
    ps.mulPose(Axis.XP.rotationDegrees(INCLINACAO_CAMERA));
    ps.mulPose(Axis.YP.rotationDegrees(20.0F));
    ps.scale(-1.0F, -1.0F, 1.0F);
    ps.translate(0.0F, -1.501F, 0.0F);
    Lighting.setupForEntityInInventory();
    net.minecraft.client.renderer.MultiBufferSource.BufferSource buffersCpm = g.bufferSource();
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre(
            jogador, renderizador, 1.0F, ps, buffersCpm, 15728880));
    com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
    net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers = g.bufferSource();

    ps.pushPose();
    ps.translate(-0.3375, 1.5, 0.28125);
    ps.mulPose(Axis.XP.rotationDegrees(180.0F));
    ps.scale(0.675F, 0.675F, 0.675F);
    minecraft.getBlockRenderer().renderSingleBlock(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "perola_negra_dourada")).defaultBlockState(),
            ps, buffers, 15728880, OverlayTexture.NO_OVERLAY);
    ps.popPose();

    com.mojang.blaze3d.vertex.VertexConsumer vc = buffers.getBuffer(
            net.minecraft.client.renderer.RenderType.entityTranslucent(renderizador.getTextureLocation(jogador)));
    modelo.renderToBuffer(ps, vc, 15728880, OverlayTexture.NO_OVERLAY, -1);
    g.flush();
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.RenderPlayerEvent.Post(
            jogador, renderizador, 1.0F, ps, buffersCpm, 15728880));
    ps.popPose();
    Lighting.setupFor3DItems();
    g.disableScissor();
}

private static final float INCLINACAO_CAMERA = -15.0F;
private long inicioPose;
    private long ultimoOlhar;
    private double guinadaAtual;
    private double arfagemAtual;

private int escalaAba() {
        return Math.max(1, Math.round((infoYr - playerYr) / 21.0F));
    }

    private int abaEspaco() {
        return Math.max(1, Math.round(escArte * 4.0F / 3.0F));
    }

    private int abaW() {
        int n = RACAS.size();
        int total = Math.round((INFO_L - 12) * escArte);
        return (total - (n - 1) * abaEspaco()) / n;
    }

    private int abaH() {
        return infoYr - playerYr;
    }

    private int abaX(int i) {
        int inicio = infoXr + Math.round(6 * escArte);
        return inicio + i * (abaW() + abaEspaco());
    }

    private boolean sobreAba(int i, int mx, int my) {
        float rx = mx * pxPorUnidade;
        float ry = my * pxPorUnidade;
        int x = abaX(i);
        return rx >= x && rx < x + abaW() && ry >= infoYr - abaH() && ry < infoYr;
    }

    private void desenharAbas(GuiGraphics g, int mx, int my) {
        long agora = System.nanoTime();
        float dt = ultimoQuadro == 0 ? 0.0F : Math.min(0.1F, (agora - ultimoQuadro) / 1.0E9F);
        ultimoQuadro = agora;
        float k = 1.0F - (float) Math.exp(-dt * 16.0F);
        for (int i = 0; i < RACAS.size(); i++) {
            hover[i] += ((sobreAba(i, mx, my) ? 1.0F : 0.0F) - hover[i]) * k;
        }
        Integer[] ordem = new Integer[RACAS.size()];
        for (int i = 0; i < ordem.length; i++) ordem[i] = i;
        java.util.Arrays.sort(ordem, java.util.Comparator.comparingDouble(
                i -> hover[i] + (i == selecionada ? 10.0F : 0.0F) + (pulo(i, agora) > 0 ? 0.3F : 0.0F)));
        for (int i : ordem) desenharAba(g, i, mx, my, agora);
    }

    private float pulo(int i, long agora) {
        if (clique[i] == 0) return 0.0F;
        float t = (agora - clique[i]) / 1.0E9F;
        if (t > 1.0F) return 0.0F;
        return 0.16F * (float) (Math.exp(-6.5F * t) * Math.sin(22.0F * t));
    }

    private static final ResourceLocation FONTE_GLIFOS = ResourceLocation.withDefaultNamespace("alt");
    private static final String GLIFOS = "abcdefghijklmnopqrstuvwxyz";

    private static double aleatorio(long chave) {
        long h = chave * 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return (h & 0xFFFFFF) / (double) 0x1000000;
    }

private static java.util.List<int[]> mascara = new java.util.ArrayList<>();
private static int mascaraL = 255;
private static int mascaraA = 143;
    private static boolean mascaraPronta;
    private static int[] linhaE = new int[0];
    private static int[] linhaD = new int[0];

private void prepararMascaraTela() {
    if (mascaraPronta) return;
    mascaraPronta = true;
    try (java.io.InputStream in = minecraft.getResourceManager().getResource(MOLDURA_TELA).orElseThrow().open();
         com.mojang.blaze3d.platform.NativeImage img = com.mojang.blaze3d.platform.NativeImage.read(in)) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean[][] bloqueado = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getPixelRGBA(x, y) >>> 24) & 255) == 0) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int xx = x + dx;
                        int yy = y + dy;
                        if (xx >= 0 && yy >= 0 && xx < w && yy < h) bloqueado[yy][xx] = true;
                    }
                }
            }
        }
        boolean[][] dentro = new boolean[h][w];
        java.util.ArrayDeque<int[]> fila = new java.util.ArrayDeque<>();
        fila.add(new int[]{w / 2, h / 2});
        dentro[h / 2][w / 2] = true;
        int[][] vizinhos = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!fila.isEmpty()) {
            int[] p = fila.poll();
            for (int[] v : vizinhos) {
                int nx = p[0] + v[0];
                int ny = p[1] + v[1];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h || dentro[ny][nx] || bloqueado[ny][nx]) continue;
                dentro[ny][nx] = true;
                fila.add(new int[]{nx, ny});
            }
        }
           int[] le = new int[h];
            int[] ld = new int[h];
            for (int y = 0; y < h; y++) {
                le[y] = -1;
                ld[y] = -1;
                for (int x = 0; x < w; x++) {
                    if (dentro[y][x]) {
                        if (le[y] < 0) le[y] = x;
                        ld[y] = x;
                    }
                }
            }
            linhaE = le;
            linhaD = ld;
            java.util.List<int[]> faixas = new java.util.ArrayList<>();

        int[] atual = null;
        int minE = 0;
        int maxE = 0;
        int minD = 0;
        int maxD = 0;
        for (int y = 0; y < h; y++) {
            int e = -1;
            int d = -1;
            for (int x = 0; x < w; x++) {
                if (dentro[y][x]) {
                    if (e < 0) e = x;
                    d = x;
                }
            }
            if (e < 0) {
                atual = null;
                continue;
            }
            if (atual != null && Math.max(maxE, e) - Math.min(minE, e) <= 1 && Math.max(maxD, d) - Math.min(minD, d) <= 1) {
                atual[1] = y;
                minE = Math.min(minE, e);
                maxE = Math.max(maxE, e);
                minD = Math.min(minD, d);
                maxD = Math.max(maxD, d);
                atual[2] = minE;
                atual[3] = maxD;
            } else {
                atual = new int[]{y, y, e, d};
                faixas.add(atual);
                minE = e;
                maxE = e;
                minD = d;
                maxD = d;
            }
        }
        mascara = faixas;
        mascaraL = w;
        mascaraA = h;
    } catch (Exception e) {
        mascara = new java.util.ArrayList<>(java.util.List.of(new int[]{0, 142, 7, 247}));
    }
}

    private boolean dentroDaTela(double rx, double ry) {
    if (linhaE.length == 0) return true;
    int linha = (int) Math.floor((ry - molOy) / (molK * 71.0 / mascaraA));
    int coluna = (int) Math.floor((rx - molOx) / (molK * 127.0 / mascaraL));
    if (linha < 0 || linha >= linhaE.length || linhaE[linha] < 0) return false;
    return coluna >= linhaE[linha] && coluna <= linhaD[linha];
}

private double distanciaBorda(double cx, double cy, double max) {
    double menor = max;
    for (int d = 0; d < 8; d++) {
        double ang = d * Math.PI / 4.0;
        double dx = Math.cos(ang);
        double dy = Math.sin(ang);
        for (double k = 1.0; k < menor; k += 1.0) {
            if (!dentroDaTela((cx + dx * k) * pxPorUnidade, (cy + dy * k) * pxPorUnidade)) {
                menor = k;
                break;
            }
        }
    }
    return menor;
}

private void desenharGlifos(GuiGraphics g) {
    prepararMascaraTela();
        double t = System.nanoTime() / 1.0E9;
        double velocidade = 16.0;
        double percurso = vh / velocidade;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        for (int i = 0; i < 36; i++) {
            double duracao = 5.0 + aleatorio(i * 7919L + 1) * (percurso + 3.0);
            double fase = aleatorio(i * 104729L + 2) * duracao;
            long ciclo = (long) Math.floor((t + fase) / duracao);
            double idade = (t + fase) - ciclo * duracao;
            long semente = i * 1000003L + ciclo * 7777L;
            double x = 28.0 + aleatorio(semente + 3) * (vw - 56.0);
            double y0 = vh * (0.4 + 0.6 * aleatorio(semente + 4)) + 10.0;
            double y = y0 - velocidade * idade;
            char letra = GLIFOS.charAt((int) (aleatorio(semente + 5) * GLIFOS.length()));
            float escala = aleatorio(semente + 6) > 0.75 ? 2.0F : 1.0F;
            double entra = Math.min(1.0, idade / 0.8);
            double sai = Math.min(1.0, (duracao - idade) / 1.2);
            double brilho = 0.7 + 0.3 * Math.sin(t * 2.2 + i * 1.7);
            double alfa = Math.max(0.0, Math.min(entra, sai)) * brilho * 0.9;
            double centroX = x + 4.0 * escala;
double centroY = y + 4.5 * escala;
double folga = distanciaBorda(centroX, centroY, 5.0 * escala + 12.0) - 5.0 * escala;
double borda = Math.max(0.0, Math.min(1.0, folga / 12.0));
alfa *= borda * borda * (3.0 - 2.0 * borda);
int a = (int) (alfa * 255.0);
            if (a < 10) continue;
            int aGlow = (int) (alfa * 90.0);
            Component glifo = Component.literal(String.valueOf(letra)).withStyle(s -> s.withFont(FONTE_GLIFOS));
           double gs = minecraft.getWindow().getGuiScale();
            double gx1 = (x - 2.0) * pxPorUnidade;
            double gx2 = (x + 10.0 * escala) * pxPorUnidade;
            double gy1 = (y - 2.0) * pxPorUnidade;
            double gy2 = (y + 11.0 * escala) * pxPorUnidade;
            double fx = molK * 127.0 / mascaraL;
            double fy = molK * 71.0 / mascaraA;
            for (int[] faixa : mascara) {
                double ry1 = molOy + faixa[0] * fy;
                double ry2 = molOy + (faixa[1] + 1) * fy;
                if (ry2 <= gy1 || ry1 >= gy2) continue;
                double rx1 = molOx + faixa[2] * fx;
                double rx2 = molOx + (faixa[3] + 1) * fx;
                if (rx2 <= gx1 || rx1 >= gx2) continue;
                g.enableScissor((int) Math.floor(rx1 / gs), (int) Math.floor(ry1 / gs), (int) Math.ceil(rx2 / gs), (int) Math.ceil(ry2 / gs));
                g.pose().pushPose();
                g.pose().translate((float) x, (float) y, 0.0F);
                g.pose().scale(escala, escala, 1.0F);
            if (aGlow >= 10) {
                int corGlow = (aGlow << 24) | 0x8B4DFF;
                g.drawString(font, glifo, -1, 0, corGlow, false);
                g.drawString(font, glifo, 1, 0, corGlow, false);
                g.drawString(font, glifo, 0, -1, corGlow, false);
                g.drawString(font, glifo, 0, 1, corGlow, false);
            }
            g.drawString(font, glifo, 0, 0, (a << 24) | 0xC9A0FF, false);
                g.pose().popPose();
                g.disableScissor();
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float parcial) {
        g.fillGradient(0, 0, vw, vh, 0xFF1A1620, 0xFF0A090D);
        float s = Math.max(vw / 1672.0F, vh / 941.0F);
        g.pose().pushPose();
        g.pose().translate((vw - 1672.0F * s) / 2.0F, (vh - 941.0F * s) / 2.0F, 0.0F);
        g.pose().scale(s, s, 1.0F);
        g.blit(FUNDO_TELA, 0, 0, 0.0F, 0.0F, 1672, 941, 1672, 941);
        g.pose().popPose();
        desenharGlifos(g);
        float kv = molK / pxPorUnidade;
        g.pose().pushPose();
        g.pose().translate(molOx / pxPorUnidade, molOy / pxPorUnidade, 0.0F);
        g.pose().scale(kv, kv, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(MOLDURA_TELA, 0, 0, 0.0F, 0.0F, 127, 71, 127, 71);
        g.pose().popPose();
    }

    private static final ResourceLocation ABA_MASCARA =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "dinamico/aba_linhas");

    private void prepararMascara() {
        Minecraft mc = Minecraft.getInstance();
        try (java.io.InputStream in = mc.getResourceManager().open(ABA)) {
            com.mojang.blaze3d.platform.NativeImage origem = com.mojang.blaze3d.platform.NativeImage.read(in);
            com.mojang.blaze3d.platform.NativeImage mascara =
                    new com.mojang.blaze3d.platform.NativeImage(origem.getWidth(), origem.getHeight(), true);
            for (int y = 0; y < origem.getHeight(); y++) {
                for (int x = 0; x < origem.getWidth(); x++) {
                    int p = origem.getPixelRGBA(x, y);
                    boolean claro = (p >>> 24) > 0 && (p & 255) >= 0xC0 && ((p >> 8) & 255) >= 0xA0 && y >= origem.getHeight() / 2;
                    mascara.setPixelRGBA(x, y, claro ? p : 0);
                }
            }
            origem.close();
            mc.getTextureManager().register(ABA_MASCARA, new net.minecraft.client.renderer.texture.DynamicTexture(mascara));
        } catch (java.io.IOException ignorado) {
        }
    }

    private void desenharAba(GuiGraphics g, int i, int mx, int my, long agora) {
        boolean ativa = i == selecionada;
        boolean sobre = sobreAba(i, mx, my);
        int ts = escalaAba();
        int cresce = Math.max(-2, Math.round((hover[i] * 2.0F + pulo(i, agora) * 12.0F) * pxPorUnidade));
        int w = abaW() + cresce * 2;
        int h = abaH() + cresce;
        int x = abaX(i) - cresce;
        int y = infoYr - abaH() - cresce;
        TipoRaca raca = RACAS.get(i);
        int corRaca = cor(raca);
        int bl = 6 * ts;
        int bt = 5 * ts;
        int bb = 5 * ts;
        int meioH = h - bt - bb;
        float brilho = ativa ? 1.0F : (sobre ? 0.88F : 0.66F);
        int corLinha = ativa ? corRaca : (sobre ? escurecer(corRaca, 0.8F) : escurecer(corRaca, 0.55F));
        g.pose().pushPose();
        g.pose().scale(1.0F / pxPorUnidade, 1.0F / pxPorUnidade, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        int[] xs = {x, x + bl, x + w - bl};
        int[] ws = {bl, w - 2 * bl, bl};
        float[] us = {0.0F, 6.0F, (float) (ABA_L - 6)};
        int[] uws = {6, ABA_L - 12, 6};
        int[] ys = {y, y + bt, y + bt + meioH};
        int[] hs = {bt, meioH, bb};
        float[] vs = {0.0F, 5.0F, 16.0F};
        int[] vhs = {5, 11, 5};
        for (int passo = 0; passo < 2; passo++) {
            if (passo == 0) {
                g.setColor(brilho, brilho, brilho, 1.0F);
            } else {
                tingir(g, corLinha);
            }
            ResourceLocation textura = passo == 0 ? ABA : ABA_MASCARA;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    g.blit(textura, xs[c], ys[r], ws[c], hs[r], us[c], vs[r], uws[c], vhs[r], ABA_L, 21);
                }
            }
        }
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        Component nome = Component.literal(raca.displayName);
        int maior = 1;
        for (TipoRaca r : RACAS) maior = Math.max(maior, font.width(r.displayName));
        int qt = Math.max(1, (int) Math.min(abaH() * 0.5F / 8.0F, abaW() * 0.86F / maior));
        int largura = font.width(nome);
        int corTexto = ativa ? claro(corRaca) : (sobre ? 0xFFFFFFFF : 0xFFD7C6A5);
        int cx = x + w / 2 - (largura * qt) / 2;
        int cy = Math.round(y + (h - 4.0F * ts) / 2.0F + 1.0F * ts - 3.5F * qt);
        g.pose().translate(cx, cy, 0.0F);
        g.pose().scale(qt, qt, 1.0F);
        g.drawString(font, nome, 0, 0, corTexto, false);
        g.pose().popPose();
    }

   private String chaveTexto;
    private float escalaTexto;
    private float alturaTexto;
    private float rolagem;
    private float sxCard = 1.0F;

    private float escalaTextoAjustada(float qc) {
        String chave = racaAtual().id + verMais + menuVerso + qc + minecraft.getLanguageManager().getSelected();
        if (chave.equals(chaveTexto)) return escalaTexto;
        int k = Math.max(1, Math.round(qc * escArte));
        float s = k / escArte;
        chaveTexto = chave;
        escalaTexto = s;
        alturaTexto = desenharTexto(null, (int) ((verMais ? CONT_L : DESC_L) / s)) * s;
        return s;
    }

    private int alturaVisivel() {
        return verMais ? CONT_A : DESC_A;
    }

    private int desenharTexto(GuiGraphics g, int textoLargura) {
        TipoRaca raca = racaAtual();
        int cursor = 0;
        if (!verMais) {
            return escrever(g, Component.translatable("gui.aelysiummod.raca." + raca.id + ".descricao"), 0, cursor, textoLargura, 0xFFF0E4CF);
        }
        return desenharItens(g, textoLargura);
    }

    private List<String[]> itensMenu() {
        TipoRaca raca = racaAtual();
        List<String[]> itens = new ArrayList<>();
        String[] secoes = menuVerso == 0 ? new String[]{"buffs"} : menuVerso == 1 ? new String[]{"debuffs"} : new String[]{"conforto", "habilidade"};
        for (String secao : secoes) {
            String chave = "gui.aelysiummod.raca." + raca.id + "." + secao;
            if (!net.minecraft.client.resources.language.I18n.exists(chave)) continue;
            for (String linha : net.minecraft.locale.Language.getInstance().getOrDefault(chave).split("\n")) {
                String[] partes = linha.split("[|]", 3);
                if (partes.length == 3) itens.add(partes);
            }
        }
        return itens;
    }

    private static final int COR_VALOR = 0xFF8CF08C;
    private static final java.util.regex.Pattern DESTAQUE = java.util.regex.Pattern.compile(
            "[Rr]eduz(?:ido)? em \\d+(?:,\\d+)?%|[+-]?\\d+(?:,\\d+)?%(?: a mais| a menos| mais rápidos?)?|\\d+ (?:segundos|minutos|blocos?|corações?)|(?:Lentidão|Força|Velocidade|Regeneração|Sorte) \\d+|[Ii]munidade a \\p{L}+|imunes?|neve fofa|cacto|bagas|instantaneamente|sorte dupla|Velocidade de movimento|Visão Noturna|Wither|Veneno|Cegueira|Fortuna|Força|[Rr]educed by \\d+(?:\\.\\d+)?%|[+-]?\\d+(?:\\.\\d+)?%(?: more| less| faster| slower)?|\\d+ (?:seconds|minutes|blocks?|hearts?)|(?:Slowness|Strength|Speed|Regeneration|Luck) \\d+|[Ii]mmun(?:e|ity) to \\p{L}+|powder snow|cacti|berry bushes|instantly|double luck|Movement speed|Night Vision|Blindness|Poison|Fortune");

    private static Component destacar(String texto) {
        net.minecraft.network.chat.MutableComponent resultado = Component.empty();
        java.util.regex.Matcher m = DESTAQUE.matcher(texto);
        int fim = 0;
        while (m.find()) {
            if (m.start() > fim) resultado.append(Component.literal(texto.substring(fim, m.start())));
            resultado.append(Component.literal(m.group()).withColor(COR_VALOR));
            fim = m.end();
        }
        if (fim < texto.length()) resultado.append(Component.literal(texto.substring(fim)));
        return resultado;
    }

    private int desenharItens(GuiGraphics g, int larguraPx) {
        int corTitulo = claro(cor(racaAtual()));
        int textoX = ICONE + 5;
        int largura = Math.max(20, larguraPx - textoX);
        int y = 0;
        for (String[] item : itensMenu()) {
            if (g != null) {
                g.blitSprite(MOLDURA_EFEITO, 0, y, ICONE, ICONE);
                ItemStack pilha = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.parse(item[0])));
                g.renderItem(pilha, (ICONE - 16) / 2, y + (ICONE - 16) / 2);
            }
            int h = 0;
            for (FormattedCharSequence linha : font.split(Component.literal(item[1]), largura)) {
                if (g != null) g.drawString(font, linha, textoX, y + h, corTitulo, false);
                h += 10;
            }
            for (FormattedCharSequence linha : font.split(menuVerso == 0 ? destacar(item[2]) : Component.literal(item[2]), largura)) {
                if (g != null) g.drawString(font, linha, textoX, y + h, 0xFFE6CAE6, false);
                h += 10;
            }
            y += Math.max(h, ICONE) + 6;
        }
        return Math.max(0, y - 6);
    }

    private void desenharMenus(GuiGraphics g, int mx, int my, float qt) {
        calcularBotoes();
        g.fill(INFO_X + 7, INFO_Y + DIV_Y, INFO_X + 84, INFO_Y + DIV_Y + 1, COR_DIVISOR);
        for (int i = 0; i < 3; i++) {
            int x = botaoX[i];
            int w = botaoL[i];
            int y = INFO_Y + MENU_Y;
            boolean sel = i == menuVerso;
            boolean sobre = sobreArte(mx, my, x, y, w, MENU_A);
            int c = COR_MENU[i];
            float brilho = sel ? 1.0F : sobre ? 0.85F : 0.62F;
            g.setColor(brilho, brilho, brilho, 1.0F);
            g.blit(BOTAO_MENU, x, y, BOTAO_CAP, MENU_A, 0.0F, 0.0F, BOTAO_CAP, MENU_A, MENU_L, MENU_A);
            g.blit(BOTAO_MENU, x + BOTAO_CAP, y, w - 2 * BOTAO_CAP, MENU_A, (float) BOTAO_CAP, 0.0F, 1, MENU_A, MENU_L, MENU_A);
            g.blit(BOTAO_MENU, x + w - BOTAO_CAP, y, BOTAO_CAP, MENU_A, (float) (BOTAO_CAP + 1), 0.0F, BOTAO_CAP, MENU_A, MENU_L, MENU_A);
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            Component nome = Component.translatable(NOMES_MENU[i]);
            float ls = botaoEscala / escArte;
            g.pose().pushPose();
            g.pose().translate(fixar(x + w / 2.0F), fixar(y + MENU_A / 2.0F + 0.5F * ls), 0.0F);
            g.pose().scale(ls, ls, 1.0F);
            int corNome = i == 1 ? (sel ? 0xFFFF1A1A : 0xFFCC1111) : sel ? 0xFFFFFFFF : misturar(c, 0xFFFFFFFF, 0.55F);
            g.drawString(font, nome, -font.width(nome) / 2, -4, corNome, false);
            g.pose().popPose();
        }
    }

    private static final String[] NOMES_MENU = {"gui.aelysiummod.raca.buffs", "gui.aelysiummod.raca.debuffs", "gui.aelysiummod.raca.menu_habilidades"};
    private static final int BOTAO_CAP = MENU_L / 2;
    private static final int BOTAO_PAD = 4;
    private static final int MENU_FAIXA = 77;
    private final int[] botaoX = new int[3];
    private final int[] botaoL = new int[3];
    private int botaoEscala = 1;

    private void calcularBotoes() {
        int kl = Math.max(1, Math.round(escalaTexto * escArte));
        int soma;
        while (true) {
            soma = 0;
            for (int i = 0; i < 3; i++) {
                float tw = font.width(Component.translatable(NOMES_MENU[i])) * kl / escArte;
                botaoL[i] = Math.max(MENU_L, (int) Math.ceil(tw) + 2 * BOTAO_PAD);
                soma += botaoL[i];
            }
            if (soma + 2 <= MENU_FAIXA || kl == 1) break;
            kl--;
        }
        botaoEscala = kl;
        int folga = Math.max(0, Math.min(MENU_ESP, (MENU_FAIXA - soma) / 2));
        int x = INFO_X + MENU_X0 + Math.max(0, (MENU_FAIXA - soma - 2 * folga) / 2);
        for (int i = 0; i < 3; i++) {
            botaoX[i] = x;
            x += botaoL[i] + folga;
        }
    }

    private int escrever(GuiGraphics g, Component texto, int x, int y, int largura, int cor) {
        List<FormattedCharSequence> linhas = font.split(texto, largura);
        for (FormattedCharSequence linha : linhas) {
            if (g != null) g.drawString(font, linha, x, y, cor, false);
            y += 10;
        }
        return y;
    }

    private static boolean dentro(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

   private int escalaConteudo() {
        return Math.max(1, (int) (escArte * 0.4F));
    }

    private float fixar(float unidades) {
        return Math.round(unidades * escArte) / escArte;
    }

    private boolean sobreArte(double mx, double my, int x, int y, int w, int h) {
        double ax = (mx * pxPorUnidade - infoXr) / escArte;
        double ay = (my * pxPorUnidade - infoYr) / escArte;
        return dentro(ax, ay, x, y, w, h);
    }

    private boolean sobreLerMais(double mx, double my) {
        return sobreArte(mx, my, CARD_X + LER_X, CARD_Y + LER_Y, 19, 8);
    }

    private boolean sobreConfirmar(double mx, double my) {
        return sobreArte(mx, my, CONF_X, CONF_Y, 39, 12);
    }

    private static int coracoes(TipoRaca raca) {
        return switch (raca) {
            case HUMANO, VALKYRIA -> 13;
            case FAUNARI, ELVARIN, TIEFLING -> 15;
            case DRACONO, UNDYNE -> 17;
            default -> 10;
        };
    }

    private void desenharBanner(GuiGraphics g) {
        TipoRaca raca = racaAtual();
        long agora = System.nanoTime();
        float dt = ultimoGiro == 0 ? 0.0F : Math.min(0.1F, (agora - ultimoGiro) / 1.0E9F);
        ultimoGiro = agora;
        float alvo = verMais ? 1.0F : 0.0F;
        float passo = dt / 0.55F;
        giro = giro < alvo ? Math.min(alvo, giro + passo) : Math.max(alvo, giro - passo);
        float suave = giro * giro * (3.0F - 2.0F * giro);
        double angulo = suave * Math.PI;
        boolean verso = angulo > Math.PI / 2.0;
        float sx = (float) Math.max(0.03, Math.abs(Math.cos(angulo)));
        sxCard = sx;

       float qe = escalaConteudo();
        float qc = qe / escArte;
        g.pose().pushPose();
        g.pose().translate(CARD_X + 19.5F, CARD_Y + 31.5F, 0.0F);
        g.pose().scale(sx, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(banner(raca), -19, -31, 0.0F, 0.0F, 39, 63, 39, 63);
        g.pose().pushPose();
        g.pose().scale(qc, qc, 1.0F);
        int gw = (int) (31 * escArte / qe);
        int gh = (int) (55 * escArte / qe);
        glint(g, -gw / 2, -gh / 2, gw, gh, cor(raca));
        Component nomeRaca = Component.literal(raca.displayName);
        float escNome = Math.max(1, Math.min((int) (1.7F * qe), (int) (64.0F * qe / Math.max(1, font.width(nomeRaca))))) / qe;
        g.pose().pushPose();
        g.pose().translate(0.0F, -66.0F, 0.0F);
        g.pose().scale(escNome, escNome, 1.0F);
        g.drawCenteredString(font, nomeRaca, 0, 0, 0xFFFFFFFF);
        g.pose().popPose();

        if (verso) {
            g.pose().pushPose();
            g.pose().translate(-18, -26, 0.0F);
            g.pose().scale(4.0F, 4.0F, 1.0F);
            g.blitSprite(ResourceLocation.withDefaultNamespace("hud/heart/container"), 0, 0, 9, 9);
            g.blitSprite(ResourceLocation.withDefaultNamespace("hud/heart/full"), 0, 0, 9, 9);
            g.pose().popPose();
            g.pose().pushPose();
            g.pose().translate(0, 28, 0.0F);
            g.pose().scale(2.0F, 2.0F, 1.0F);
            Component qtd = Component.literal(coracoes(raca) + "x");
            g.drawString(font, qtd, -font.width(qtd) / 2, 0, 0xFFFFFFFF, true);
            g.pose().popPose();
        } else {
            desenharItem(g, item(raca), 0.0F, 4.0F);
        }
        g.pose().popPose();
        g.pose().popPose();
    }

    private static final ResourceLocation GLINT =
            ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_item.png");

    private void glint(GuiGraphics g, int x, int y, int w, int h, int argb) {
        double t = System.nanoTime() / 1.0E9;
        float r = ((argb >> 16) & 255) / 255.0F, gr = ((argb >> 8) & 255) / 255.0F, b = (argb & 255) / 255.0F;
        float mistura = 0.4F;
        r = r * (1 - mistura) + mistura;
        gr = gr * (1 - mistura) + mistura;
        b = b * (1 - mistura) + mistura;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_COLOR,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        g.setColor(r * 0.5F, gr * 0.5F, b * 0.5F, 1.0F);
        int uw = w, vh = h;
        g.blit(GLINT, x, y, w, h, (float) ((t * 9.0) % 256.0), (float) ((t * 4.0) % 256.0), uw, vh, 256, 256);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    }

    private void desenharItem(GuiGraphics g, ItemStack pilha, float cx, float cy) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel modelo = mc.getItemRenderer().getModel(pilha, mc.level, mc.player, 0);
        float seg = (System.nanoTime() - inicioBamboleio) / 1.0E9F;
        float forca = (float) Math.exp(-1.8F * seg);
        float inclinacao = 20.0F * forca * (float) Math.sin(seg * 8.0F);
        float giro = 28.0F * forca * (float) Math.sin(seg * 5.5F + 1.2F);
        g.pose().pushPose();
        double t = System.nanoTime() / 1.0E9;
        float flutuaX = (float) (3.0 * Math.sin(t * 1.3) + 1.6 * Math.sin(t * 2.7 + 1.0) + 0.8 * Math.sin(t * 4.1 + 2.3));
        float flutuaY = (float) (4.0 * Math.sin(t * 1.7 + 0.5) + 2.0 * Math.sin(t * 3.1 + 1.9) + 0.9 * Math.sin(t * 5.3));
        float balancoZ = (float) (7.0 * Math.sin(t * 1.9 + 0.3) + 3.0 * Math.sin(t * 3.7 + 2.0));
        float balancoY = (float) (14.0 * Math.sin(t * 1.1 + 1.0) + 6.0 * Math.sin(t * 2.3));
        float qpx = escalaConteudo();
        g.pose().translate(cx + Math.round(flutuaX * qpx) / qpx, cy + Math.round(flutuaY * qpx) / qpx, 150.0F);
        g.pose().scale(64.0F, -64.0F, 64.0F);
        g.pose().mulPose(Axis.ZP.rotationDegrees(inclinacao + balancoZ));
        g.pose().mulPose(Axis.YP.rotationDegrees(giro + balancoY));
        Lighting.setupFor3DItems();
        mc.getItemRenderer().render(pilha, ItemDisplayContext.GUI, false, g.pose(), g.bufferSource(),
                15728880, OverlayTexture.NO_OVERLAY, modelo);
        g.flush();
        g.pose().popPose();
    }
}
