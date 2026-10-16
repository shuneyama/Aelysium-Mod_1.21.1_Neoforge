package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.InsigniaPersonalizada;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public final class OrdemListaJogadores {

    private static final Set<TipoInsignia> PRIORITARIAS = EnumSet.of(
            TipoInsignia.ADMIN, TipoInsignia.DEUS, TipoInsignia.CIENTISTA, TipoInsignia.ROBO2);

    public static final Comparator<PlayerInfo> COMPARADOR = Comparator
            .<PlayerInfo>comparingInt(OrdemListaJogadores::grupo)
            .thenComparing(OrdemListaJogadores::chaveInsignia)
            .thenComparing(OrdemListaJogadores::nome);

    private OrdemListaJogadores() {}

    private static int primeiroCodigo(PlayerInfo info) {
        Component exibicao = info.getTabListDisplayName();
        if (exibicao == null) return -1;
        String texto = exibicao.getString();
        return texto.isEmpty() ? -1 : texto.codePointAt(0);
    }

    private static TipoInsignia insigniaFixa(int codigo) {
        for (TipoInsignia b : TipoInsignia.values()) {
            if (b.glyphCodePoint == codigo) return b;
        }
        return null;
    }

    private static boolean ehInsigniaCodigo(int codigo) {
        return InsigniaPersonalizada.ehCodepoint(codigo) || insigniaFixa(codigo) != null;
    }

    private static int grupo(PlayerInfo info) {
        int codigo = primeiroCodigo(info);
        if (InsigniaPersonalizada.ehCodepoint(codigo)) return 1;
        TipoInsignia fixa = insigniaFixa(codigo);
        if (fixa == null) return 2;
        return PRIORITARIAS.contains(fixa) ? 0 : 1;
    }

    private static String chaveInsignia(PlayerInfo info) {
        int codigo = primeiroCodigo(info);
        if (InsigniaPersonalizada.ehCodepoint(codigo)) {
            InsigniasPersonalizadasCliente.Entrada e = InsigniasPersonalizadasCliente.porIndice(codigo - InsigniaPersonalizada.CODEPOINT_BASE);
            return e == null ? "~" : e.dados().id();
        }
        TipoInsignia fixa = insigniaFixa(codigo);
        return fixa == null ? "" : fixa.id;
    }

    private static String nome(PlayerInfo info) {
        Component exibicao = info.getTabListDisplayName();
        String texto = exibicao != null ? exibicao.getString() : info.getProfile().getName();
        int codigo = primeiroCodigo(info);
        if (!texto.isEmpty() && ehInsigniaCodigo(codigo)) {
            int i = 0;
            while (i < texto.length() && texto.codePointAt(i) == codigo) i += Character.charCount(codigo);
            texto = texto.substring(i).stripLeading();
        }
        return texto.toLowerCase(Locale.ROOT);
    }

    private static void desenharPersonalizada(GuiGraphics g, int codigo, int x, int y, int cor) {
        InsigniasPersonalizadasCliente.Entrada e = InsigniasPersonalizadasCliente.porIndice(codigo - InsigniaPersonalizada.CODEPOINT_BASE);
        if (e == null) return;
        float alfa = ((cor >>> 24) & 255) / 255f;
        if (alfa <= 0f) alfa = 1f;
        g.setColor(1f, 1f, 1f, alfa);
        g.blit(e.textura(), x, y - 2, 0, 0, e.largura(), GeradorInsignia.ALTURA, e.largura(), GeradorInsignia.ALTURA);
        g.setColor(1f, 1f, 1f, 1f);
    }

    public static int desenharNome(GuiGraphics g, Font fonte, Component nome, int x, int y, int cor) {
        List<Component> partes = nome.getSiblings();
        if (partes.size() >= 3) {
            String textoInsignia = partes.get(0).getString();
            int codigo = textoInsignia.isEmpty() ? -1 : textoInsignia.codePointAt(0);
            if (ehInsigniaCodigo(codigo)) {
                Component insignia = partes.get(0);
                int avanco = fonte.width(insignia) + fonte.width(partes.get(1));
                MutableComponent resto = Component.empty();
                for (int i = 2; i < partes.size(); i++) resto.append(partes.get(i));
                if (InsigniaPersonalizada.ehCodepoint(codigo)) desenharPersonalizada(g, codigo, x, y, cor);
                else g.drawString(fonte, insignia, x, y, cor, false);
                return g.drawString(fonte, resto, x + avanco, y, cor);
            }
        }
        return g.drawString(fonte, nome, x, y, cor);
    }

    private record Caractere(int codigo, Style estilo) {}

    public static int desenharLinhaSemSombraNaInsignia(GuiGraphics g, Font fonte, FormattedCharSequence linha, int x, int y, int cor) {
        List<Caractere> caracteres = new ArrayList<>();
        linha.accept((indice, estilo, codigo) -> {
            caracteres.add(new Caractere(codigo, estilo));
            return true;
        });
        boolean temInsignia = false;
        for (Caractere ch : caracteres) {
            if (ehInsigniaCodigo(ch.codigo())) {
                temInsignia = true;
                break;
            }
        }
        if (!temInsignia) return g.drawString(fonte, linha, x, y, cor);

        int cursor = x;
        int inicio = 0;
        while (inicio < caracteres.size()) {
            boolean insignia = ehInsigniaCodigo(caracteres.get(inicio).codigo());
            int fim = inicio;
            while (fim < caracteres.size() && ehInsigniaCodigo(caracteres.get(fim).codigo()) == insignia) fim++;
            List<Caractere> trecho = caracteres.subList(inicio, fim);
            FormattedCharSequence parte = sink -> {
                for (int i = 0; i < trecho.size(); i++) {
                    if (!sink.accept(i, trecho.get(i).estilo(), trecho.get(i).codigo())) return false;
                }
                return true;
            };
            int primeiro = trecho.get(0).codigo();
            if (insignia && InsigniaPersonalizada.ehCodepoint(primeiro)) {
                desenharPersonalizada(g, primeiro, cursor, y, cor);
            } else {
                g.drawString(fonte, parte, cursor, y, cor, !insignia);
            }
            cursor += fonte.width(parte);
            inicio = fim;
        }
        return cursor;
    }
}
