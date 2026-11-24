package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class TelaSeletorCorDupla extends Screen {
    private static final int RODA = 96;
    private static final int BARRA = 12;
    private static final int COLUNA = 130;
    private static final int COR_TEXTO = 0xFF404040;

    private final Screen pai;
    private final boolean comParticulas;
    private final int largura;
    private final int altura;
    private final String previa;
    private final BiConsumer<Integer, Integer> aoConfirmar;
    private final Coluna nome;
    private final Coluna particulas;
    private Coluna arrastando;
    private boolean arrastandoRoda;
    private int x0;
    private int y0;

    private final class Coluna {
        final String chave;
        final String idTextura;
        int cor;
        float matiz;
        float saturacao;
        float brilho;
        ResourceLocation textura;
        float brilhoDaTextura = -1f;
        boolean editando;
        boolean substituir;
        String digitos = "";
        long inicioCursor;
        int colunaX;
        int rodaX;
        int rodaY;
        int barraY;
        int hexX;
        int hexY;
        int hexL;

        Coluna(String chave, String idTextura, int cor) {
            this.chave = chave;
            this.idTextura = idTextura;
            definir(cor);
        }

        void definir(int rgb) {
            cor = rgb & 0xFFFFFF;
            float[] hsv = rgbParaHsv(cor);
            if (hsv[1] > 0f && hsv[2] > 0f) matiz = hsv[0];
            saturacao = hsv[1];
            brilho = hsv[2];
        }

        void posicionar(int colunaX) {
            this.colunaX = colunaX;
            rodaX = colunaX + (COLUNA - RODA) / 2;
            rodaY = y0 + 36;
            barraY = rodaY + RODA + 8;
        }

        void atualizarTextura() {
            if (textura != null && Math.abs(brilhoDaTextura - brilho) < 0.004f) return;
            brilhoDaTextura = brilho;
            InsigniasPersonalizadasCliente.liberar(textura);
            int[] pixels = new int[RODA * RODA];
            float raio = RODA / 2f;
            for (int y = 0; y < RODA; y++) {
                for (int x = 0; x < RODA; x++) {
                    float dx = x + 0.5f - raio;
                    float dy = y + 0.5f - raio;
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d > raio) continue;
                    float h = (float) (Math.atan2(dy, dx) / (Math.PI * 2.0));
                    if (h < 0f) h += 1f;
                    pixels[y * RODA + x] = 0xFF000000 | hsvParaRgb(h, Math.min(1f, d / raio), brilho);
                }
            }
            textura = InsigniasPersonalizadasCliente.registrar(idTextura, new GeradorInsignia.Imagem(RODA, RODA, pixels));
        }

        void desenhar(GuiGraphics g) {
            atualizarTextura();
            String antes = Component.translatable(chave).getString() + " (";
            String hex = String.format("#%06X", cor & 0xFFFFFF);
            hexL = font.width("#FFFFFF") + 2;
            int total = font.width(antes) + hexL + font.width(")");
            int x = colunaX + (COLUNA - total) / 2;
            hexY = y0 + 22;
            hexX = x + font.width(antes);
            g.drawString(font, antes, x, hexY, COR_TEXTO, false);
            String mostrado = editando ? "#" + digitos : hex;
            g.drawString(font, mostrado, hexX, hexY, COR_TEXTO, false);
            g.fill(hexX, hexY + 9, hexX + hexL, hexY + 10, COR_TEXTO);
            if (editando && ((System.currentTimeMillis() - inicioCursor) / 500) % 2 == 0) {
                int cx = hexX + font.width(mostrado);
                g.fill(cx, hexY - 1, cx + 1, hexY + 9, COR_TEXTO);
            }
            g.drawString(font, ")", hexX + hexL, hexY, COR_TEXTO, false);

            g.blit(textura, rodaX, rodaY, 0, 0, RODA, RODA, RODA, RODA);
            float raio = RODA / 2f;
            int mx = Math.round(rodaX + raio + (float) Math.cos(matiz * Math.PI * 2.0) * saturacao * raio);
            int my = Math.round(rodaY + raio + (float) Math.sin(matiz * Math.PI * 2.0) * saturacao * raio);
            g.renderOutline(mx - 3, my - 3, 7, 7, 0xFF000000);
            g.renderOutline(mx - 2, my - 2, 5, 5, 0xFFFFFFFF);

            g.renderOutline(rodaX - 1, barraY - 1, RODA + 2, BARRA + 2, 0xFF000000);
            for (int i = 0; i < RODA; i++) {
                g.fill(rodaX + i, barraY, rodaX + i + 1, barraY + BARRA, 0xFF000000 | hsvParaRgb(matiz, saturacao, i / (float) (RODA - 1)));
            }
            int mb = rodaX + Math.round(brilho * (RODA - 1));
            g.fill(mb - 1, barraY - 2, mb + 2, barraY + BARRA + 2, 0xFF000000);
            g.fill(mb, barraY - 1, mb + 1, barraY + BARRA + 1, 0xFFFFFFFF);

            g.drawString(font, previa, colunaX + (COLUNA - font.width(previa)) / 2, barraY + BARRA + 10, 0xFF000000 | cor, true);
        }

        boolean naRoda(double mx, double my) {
            float raio = RODA / 2f;
            double dx = mx - (rodaX + raio);
            double dy = my - (rodaY + raio);
            return dx * dx + dy * dy <= raio * raio;
        }

        boolean naBarra(double mx, double my) {
            return mx >= rodaX - 2 && mx < rodaX + RODA + 2 && my >= barraY - 2 && my < barraY + BARRA + 2;
        }

        boolean noHex(double mx, double my) {
            return mx >= hexX && mx < hexX + hexL && my >= hexY - 2 && my < hexY + 11;
        }

        void arrastarRoda(double mx, double my) {
            float raio = RODA / 2f;
            double dx = mx - (rodaX + raio);
            double dy = my - (rodaY + raio);
            double distancia = Math.min(raio, Math.sqrt(dx * dx + dy * dy));
            double angulo = Math.atan2(dy, dx) / (Math.PI * 2.0);
            if (angulo < 0) angulo += 1.0;
            matiz = (float) angulo;
            saturacao = (float) (distancia / raio);
            cor = hsvParaRgb(matiz, saturacao, brilho);
        }

        void arrastarBarra(double mx) {
            brilho = (float) Math.max(0.0, Math.min(1.0, (mx - rodaX) / (RODA - 1)));
            cor = hsvParaRgb(matiz, saturacao, brilho);
        }

        void abrirHex() {
            editando = true;
            substituir = true;
            inicioCursor = System.currentTimeMillis();
            digitos = String.format("%06X", cor & 0xFFFFFF);
        }

        void inserir(String texto) {
            StringBuilder limpo = new StringBuilder();
            for (char c : texto.toCharArray()) {
                if (Character.digit(c, 16) >= 0) limpo.append(Character.toUpperCase(c));
            }
            if (limpo.length() == 0) return;
            if (substituir) {
                digitos = "";
                substituir = false;
            }
            inicioCursor = System.currentTimeMillis();
            for (int i = 0; i < limpo.length() && digitos.length() < 6; i++) digitos += limpo.charAt(i);
            if (digitos.length() == 6) definir(Integer.parseInt(digitos, 16));
        }

        void liberar() {
            InsigniasPersonalizadasCliente.liberar(textura);
            textura = null;
        }
    }

    public TelaSeletorCorDupla(Screen pai, int corNome, int corParticulas, String previa, boolean comParticulas, BiConsumer<Integer, Integer> aoConfirmar) {
        super(Component.translatable("gui.aelysiummod.personagens.cor_titulo"));
        this.pai = pai;
        this.comParticulas = comParticulas;
        this.largura = comParticulas ? COLUNA * 2 + 20 : COLUNA + 20;
        this.altura = comParticulas ? 208 : 236;
        this.previa = previa == null || previa.isBlank() ? Component.translatable("gui.aelysiummod.deuses.previa").getString() : previa;
        this.aoConfirmar = aoConfirmar;
        this.nome = new Coluna("gui.aelysiummod.deuses.cor_nome", "deus_roda_nome", corNome);
        this.particulas = new Coluna("gui.aelysiummod.deuses.particulas", "deus_roda_particulas", corParticulas);
    }

    private Coluna[] colunas() {
        return comParticulas ? new Coluna[]{nome, particulas} : new Coluna[]{nome};
    }

    @Override
    protected void init() {
        x0 = (width - largura) / 2;
        y0 = (height - altura) / 2;
        nome.posicionar(x0 + 10);
        if (comParticulas) particulas.posicionar(x0 + 10 + COLUNA);
        Runnable confirmar = () -> {
            aoConfirmar.accept(nome.cor, particulas.cor);
            onClose();
        };
        if (comParticulas) {
            int larguraBotao = 100;
            int y = y0 + altura - 28;
            addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.personagens.cor_ok"), b -> confirmar.run())
                    .bounds(x0 + largura / 2 - larguraBotao - 2, y, larguraBotao, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                    .bounds(x0 + largura / 2 + 2, y, larguraBotao, 20).build());
        } else {
            int larguraBotao = largura - 20;
            addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.personagens.cor_ok"), b -> confirmar.run())
                    .bounds(x0 + 10, y0 + altura - 52, larguraBotao, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                    .bounds(x0 + 10, y0 + altura - 28, larguraBotao, 20).build());
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        pai.render(g, -1, -1, parcial);
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        g.fill(0, 0, width, height, 0x90000000);
        PainelGui.painel(g, x0, y0, largura, altura);
        g.drawString(font, title, x0 + (largura - font.width(title)) / 2, y0 + 8, COR_TEXTO, false);
        nome.desenhar(g);
        if (comParticulas) particulas.desenhar(g);
        super.render(g, mouseX, mouseY, parcial);
        g.pose().popPose();
    }

    private Coluna editando() {
        if (nome.editando) return nome;
        if (particulas.editando) return particulas;
        return null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        if (botao == 0) {
            for (Coluna c : colunas()) {
                if (c.noHex(mx, my)) {
                    nome.editando = false;
                    particulas.editando = false;
                    c.abrirHex();
                    return true;
                }
            }
            nome.editando = false;
            particulas.editando = false;
            for (Coluna c : colunas()) {
                if (c.naRoda(mx, my)) {
                    arrastando = c;
                    arrastandoRoda = true;
                    c.arrastarRoda(mx, my);
                    return true;
                }
                if (c.naBarra(mx, my)) {
                    arrastando = c;
                    arrastandoRoda = false;
                    c.arrastarBarra(mx);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, botao);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int botao, double dx, double dy) {
        if (arrastando != null) {
            if (arrastandoRoda) arrastando.arrastarRoda(mx, my);
            else arrastando.arrastarBarra(mx);
            return true;
        }
        return super.mouseDragged(mx, my, botao, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int botao) {
        arrastando = null;
        return super.mouseReleased(mx, my, botao);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        Coluna e = editando();
        if (e != null) {
            e.inserir(String.valueOf(c));
            return true;
        }
        return super.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int mods) {
        Coluna e = editando();
        if (e != null) {
            if (tecla == 257 || tecla == 335 || tecla == 256) {
                e.editando = false;
                return true;
            }
            if (tecla == 259) {
                if (e.substituir) {
                    e.digitos = "";
                    e.substituir = false;
                } else if (!e.digitos.isEmpty()) {
                    e.digitos = e.digitos.substring(0, e.digitos.length() - 1);
                }
                e.inicioCursor = System.currentTimeMillis();
                return true;
            }
            if (Screen.isPaste(tecla)) {
                e.inserir(minecraft.keyboardHandler.getClipboard());
                return true;
            }
            return true;
        }
        return super.keyPressed(tecla, scan, mods);
    }

    @Override
    public void onClose() {
        nome.liberar();
        particulas.liberar();
        minecraft.setScreen(pai);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int hsvParaRgb(float h, float s, float v) {
        float c = v * s;
        float hh = (h - (float) Math.floor(h)) * 6f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float m = v - c;
        float r, g, b;
        if (hh < 1f) { r = c; g = x; b = 0; }
        else if (hh < 2f) { r = x; g = c; b = 0; }
        else if (hh < 3f) { r = 0; g = c; b = x; }
        else if (hh < 4f) { r = 0; g = x; b = c; }
        else if (hh < 5f) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return (Math.round((r + m) * 255f) << 16) | (Math.round((g + m) * 255f) << 8) | Math.round((b + m) * 255f);
    }

    private static float[] rgbParaHsv(int rgb) {
        float r = ((rgb >> 16) & 255) / 255f;
        float g = ((rgb >> 8) & 255) / 255f;
        float b = (rgb & 255) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = 0f;
        if (d != 0f) {
            if (max == r) h = ((g - b) / d) % 6f;
            else if (max == g) h = (b - r) / d + 2f;
            else h = (r - g) / d + 4f;
            h /= 6f;
            if (h < 0f) h += 1f;
        }
        return new float[]{h, max == 0f ? 0f : d / max, max};
    }
}
