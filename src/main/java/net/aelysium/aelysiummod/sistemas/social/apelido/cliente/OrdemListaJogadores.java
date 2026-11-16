package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import com.google.common.collect.MapMaker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.nucleo.cliente.VersaoFonteCliente;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.InsigniaPersonalizada;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class OrdemListaJogadores {

    private static final Set<TipoInsignia> PRIORITARIAS = EnumSet.of(
            TipoInsignia.ADMIN, TipoInsignia.DEUS, TipoInsignia.CIENTISTA, TipoInsignia.ROBO2);

    private static final TipoInsignia[] INSIGNIAS = TipoInsignia.values();
    private static final int CODIGO_MINIMO;
    private static final TipoInsignia[] POR_CODIGO;

    static {
        int minimo = Integer.MAX_VALUE;
        int maximo = Integer.MIN_VALUE;
        for (TipoInsignia b : INSIGNIAS) {
            minimo = Math.min(minimo, b.glyphCodePoint);
            maximo = Math.max(maximo, b.glyphCodePoint);
        }
        CODIGO_MINIMO = minimo;
        POR_CODIGO = new TipoInsignia[maximo - minimo + 1];
        for (int i = INSIGNIAS.length - 1; i >= 0; i--) {
            POR_CODIGO[INSIGNIAS[i].glyphCodePoint - minimo] = INSIGNIAS[i];
        }
    }

    private static final Map<PlayerInfo, ChaveOrdem> CHAVES = new MapMaker().weakKeys().makeMap();
    private static final Map<Component, NomePreparado> NOMES = new MapMaker().weakKeys().makeMap();
    private static final Map<FormattedCharSequence, LinhaPreparada> LINHAS = new MapMaker().weakKeys().makeMap();

    private static final class ChaveOrdem {
        Component origem;
        int codigo;
        int grupo;
        String chaveFixa;
        String nome;
    }

    private static final class NomePreparado {
        Component[] partes;
        int versaoFonte;
        boolean insignia;
        int codigo;
        int avanco;
        FormattedCharSequence insigniaTexto;
        FormattedCharSequence resto;
    }

    private static final int LUZ_TOTAL = 15728880;
    private static MultiBufferSource.BufferSource texto;

    private static final class Trecho {
        final FormattedCharSequence parte;
        final boolean insignia;
        final int primeiro;
        final int largura;

        Trecho(FormattedCharSequence parte, boolean insignia, int primeiro, int largura) {
            this.parte = parte;
            this.insignia = insignia;
            this.primeiro = primeiro;
            this.largura = largura;
        }
    }

    private static final class LinhaPreparada {
        final Trecho[] trechos;
        final int versaoFonte;

        LinhaPreparada(Trecho[] trechos, int versaoFonte) {
            this.trechos = trechos;
            this.versaoFonte = versaoFonte;
        }
    }

    private static final LinhaPreparada SEM_INSIGNIA = new LinhaPreparada(null, 0);

    private static final class DetectorInsignia implements FormattedCharSink {
        boolean encontrou;

        @Override
        public boolean accept(int indice, Style estilo, int codigo) {
            if (ehInsigniaCodigo(codigo)) {
                encontrou = true;
                return false;
            }
            return true;
        }
    }

    private static final DetectorInsignia DETECTOR = new DetectorInsignia();

    public static final Comparator<PlayerInfo> COMPARADOR = (a, b) -> {
        ChaveOrdem ca = chave(a);
        ChaveOrdem cb = chave(b);
        int resultado = Integer.compare(ca.grupo, cb.grupo);
        if (resultado != 0) return resultado;
        resultado = chaveInsignia(ca).compareTo(chaveInsignia(cb));
        if (resultado != 0) return resultado;
        return ca.nome.compareTo(cb.nome);
    };

    private OrdemListaJogadores() {}

    private static TipoInsignia insigniaFixa(int codigo) {
        int indice = codigo - CODIGO_MINIMO;
        return indice >= 0 && indice < POR_CODIGO.length ? POR_CODIGO[indice] : null;
    }

    private static boolean ehInsigniaCodigo(int codigo) {
        return InsigniaPersonalizada.ehCodepoint(codigo) || insigniaFixa(codigo) != null;
    }

    private static ChaveOrdem chave(PlayerInfo info) {
        Component exibicao = info.getTabListDisplayName();
        ChaveOrdem chave = CHAVES.get(info);
        if (chave != null && chave.origem == exibicao) return chave;
        if (chave == null) {
            chave = new ChaveOrdem();
            CHAVES.put(info, chave);
        }
        chave.origem = exibicao;

        String textoExibicao = exibicao != null ? exibicao.getString() : null;
        int codigo = textoExibicao == null || textoExibicao.isEmpty() ? -1 : textoExibicao.codePointAt(0);
        chave.codigo = codigo;

        TipoInsignia fixa = insigniaFixa(codigo);
        if (InsigniaPersonalizada.ehCodepoint(codigo)) chave.grupo = 1;
        else if (fixa == null) chave.grupo = 2;
        else chave.grupo = PRIORITARIAS.contains(fixa) ? 0 : 1;
        chave.chaveFixa = fixa == null ? "" : fixa.id;

        String texto = textoExibicao != null ? textoExibicao : info.getProfile().getName();
        if (!texto.isEmpty() && ehInsigniaCodigo(codigo)) {
            int i = 0;
            while (i < texto.length() && texto.codePointAt(i) == codigo) i += Character.charCount(codigo);
            texto = texto.substring(i).stripLeading();
        }
        chave.nome = texto.toLowerCase(Locale.ROOT);
        return chave;
    }

    private static String chaveInsignia(ChaveOrdem chave) {
        if (InsigniaPersonalizada.ehCodepoint(chave.codigo)) {
            InsigniasPersonalizadasCliente.Entrada e = InsigniasPersonalizadasCliente.porIndice(chave.codigo - InsigniaPersonalizada.CODEPOINT_BASE);
            return e == null ? "~" : e.dados().id();
        }
        return chave.chaveFixa;
    }

    private static MultiBufferSource.BufferSource bufferTexto() {
        MultiBufferSource.BufferSource atual = texto;
        if (atual == null) {
            atual = MultiBufferSource.immediate(new ByteBufferBuilder(262144));
            texto = atual;
        }
        return atual;
    }

    private static int escrever(GuiGraphics g, Font fonte, FormattedCharSequence sequencia, float x, float y, int cor, boolean sombra) {
        return fonte.drawInBatch(sequencia, x, y, cor, sombra, g.pose().last().pose(), bufferTexto(), Font.DisplayMode.NORMAL, 0, LUZ_TOTAL);
    }

    public static void descarregarTexto() {
        MultiBufferSource.BufferSource atual = texto;
        if (atual == null) return;
        RenderSystem.disableDepthTest();
        atual.endBatch();
        RenderSystem.enableDepthTest();
    }

    public static void descarregar(GuiGraphics g) {
        try {
            g.flush();
        } finally {
            descarregarTexto();
        }
    }

    private static void desenharPersonalizada(GuiGraphics g, int codigo, int x, int y, int cor) {
        InsigniasPersonalizadasCliente.Entrada e = InsigniasPersonalizadasCliente.porIndice(codigo - InsigniaPersonalizada.CODEPOINT_BASE);
        if (e == null) return;
        descarregar(g);
        float alfa = ((cor >>> 24) & 255) / 255f;
        if (alfa <= 0f) alfa = 1f;
        g.setColor(1f, 1f, 1f, alfa);
        g.blit(e.textura(), x, y - 2, 0, 0, e.largura(), GeradorInsignia.ALTURA, e.largura(), GeradorInsignia.ALTURA);
        g.setColor(1f, 1f, 1f, 1f);
    }

    public static int desenharNome(GuiGraphics g, Font fonte, Component nome, int x, int y, int cor) {
        try {
            return escreverNome(g, fonte, nome, x, y, cor);
        } finally {
            descarregarTexto();
        }
    }

    private static int escreverNome(GuiGraphics g, Font fonte, Component nome, int x, int y, int cor) {
        List<Component> partes = nome.getSiblings();
        if (partes.size() >= 3) {
            NomePreparado preparado = prepararNome(fonte, partes);
            if (preparado.insignia) {
                if (InsigniaPersonalizada.ehCodepoint(preparado.codigo)) desenharPersonalizada(g, preparado.codigo, x, y, cor);
                else escrever(g, fonte, preparado.insigniaTexto, x, y, cor, false);
                return escrever(g, fonte, preparado.resto, x + preparado.avanco, y, cor, true);
            }
        }
        return escrever(g, fonte, nome.getVisualOrderText(), x, y, cor, true);
    }

    private static NomePreparado prepararNome(Font fonte, List<Component> partes) {
        Component primeira = partes.get(0);
        int versaoFonte = VersaoFonteCliente.atual();
        NomePreparado preparado = NOMES.get(primeira);
        if (preparado != null && preparado.versaoFonte == versaoFonte && mesmasPartes(preparado.partes, partes)) {
            return preparado;
        }
        if (preparado == null) {
            preparado = new NomePreparado();
            NOMES.put(primeira, preparado);
        }
        preparado.partes = partes.toArray(new Component[0]);
        preparado.versaoFonte = versaoFonte;

        String textoInsignia = primeira.getString();
        int codigo = textoInsignia.isEmpty() ? -1 : textoInsignia.codePointAt(0);
        preparado.codigo = codigo;
        preparado.insignia = ehInsigniaCodigo(codigo);
        if (preparado.insignia) {
            preparado.avanco = fonte.width(primeira) + fonte.width(partes.get(1));
            MutableComponent resto = Component.empty();
            for (int i = 2; i < partes.size(); i++) resto.append(partes.get(i));
            preparado.insigniaTexto = primeira.getVisualOrderText();
            preparado.resto = resto.getVisualOrderText();
        } else {
            preparado.avanco = 0;
            preparado.insigniaTexto = null;
            preparado.resto = null;
        }
        return preparado;
    }

    private static boolean mesmasPartes(Component[] guardadas, List<Component> partes) {
        if (guardadas.length != partes.size()) return false;
        for (int i = 0; i < guardadas.length; i++) {
            if (guardadas[i] != partes.get(i)) return false;
        }
        return true;
    }

    public static int desenharLinhaSemSombraNaInsignia(GuiGraphics g, Font fonte, FormattedCharSequence linha, int x, int y, int cor) {
        LinhaPreparada preparada = prepararLinha(fonte, linha);
        Trecho[] trechos = preparada.trechos;
        if (trechos == null) return escrever(g, fonte, linha, x, y, cor, true);

        int cursor = x;
        for (Trecho trecho : trechos) {
            if (trecho.insignia && InsigniaPersonalizada.ehCodepoint(trecho.primeiro)) {
                desenharPersonalizada(g, trecho.primeiro, cursor, y, cor);
            } else {
                escrever(g, fonte, trecho.parte, cursor, y, cor, !trecho.insignia);
            }
            cursor += trecho.largura;
        }
        return cursor;
    }

    private static LinhaPreparada prepararLinha(Font fonte, FormattedCharSequence linha) {
        LinhaPreparada preparada = LINHAS.get(linha);
        if (preparada == SEM_INSIGNIA) return preparada;
        int versaoFonte = VersaoFonteCliente.atual();
        if (preparada != null && preparada.versaoFonte == versaoFonte) return preparada;

        if (preparada == null) {
            DETECTOR.encontrou = false;
            linha.accept(DETECTOR);
            if (!DETECTOR.encontrou) {
                LINHAS.put(linha, SEM_INSIGNIA);
                return SEM_INSIGNIA;
            }
        }

        List<Integer> codigos = new ArrayList<>();
        List<Style> estilos = new ArrayList<>();
        linha.accept((indice, estilo, codigo) -> {
            codigos.add(codigo);
            estilos.add(estilo);
            return true;
        });

        List<Trecho> trechos = new ArrayList<>();
        int inicio = 0;
        while (inicio < codigos.size()) {
            boolean insignia = ehInsigniaCodigo(codigos.get(inicio));
            int fim = inicio;
            while (fim < codigos.size() && ehInsigniaCodigo(codigos.get(fim)) == insignia) fim++;
            int tamanho = fim - inicio;
            int[] trechoCodigos = new int[tamanho];
            Style[] trechoEstilos = new Style[tamanho];
            for (int i = 0; i < tamanho; i++) {
                trechoCodigos[i] = codigos.get(inicio + i);
                trechoEstilos[i] = estilos.get(inicio + i);
            }
            FormattedCharSequence parte = sink -> {
                for (int i = 0; i < trechoCodigos.length; i++) {
                    if (!sink.accept(i, trechoEstilos[i], trechoCodigos[i])) return false;
                }
                return true;
            };
            trechos.add(new Trecho(parte, insignia, trechoCodigos[0], fonte.width(parte)));
            inicio = fim;
        }

        preparada = new LinhaPreparada(trechos.toArray(new Trecho[0]), versaoFonte);
        LINHAS.put(linha, preparada);
        return preparada;
    }
}
