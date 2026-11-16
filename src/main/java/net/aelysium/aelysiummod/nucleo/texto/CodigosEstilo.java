package net.aelysium.aelysiummod.nucleo.texto;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class CodigosEstilo {

    private static final String CODIGOS_NOVOS = "ghijpqstuvwz";
    private static final String CODIGOS_VANILLA = "0123456789abcdefklmnor";
    private static final String[] FONTES = {
            "efeito/rainbow", "efeito/glitch", "efeito/wave", "efeito/shake", "efeito/pulse",
            "efeito/neon", "efeito/bounce", "efeito/glow", "efeito/vibrate", "efeito/wiggle",
            "cinzel", "almendra"
    };
    private static final ResourceLocation[] LOCAIS = new ResourceLocation[FONTES.length];

    static {
        for (int i = 0; i < FONTES.length; i++) {
            LOCAIS[i] = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, FONTES[i]);
        }
    }

    private CodigosEstilo() {}

    public static boolean ehCodigoEstilo(char codigo) {
        return CODIGOS_NOVOS.indexOf(Character.toLowerCase(codigo)) >= 0;
    }

    public static ResourceLocation fonte(char codigo) {
        return LOCAIS[CODIGOS_NOVOS.indexOf(Character.toLowerCase(codigo))];
    }

    public static int indiceEfeito(ResourceLocation fonte) {
        for (int i = 0; i < 10; i++) {
            if (LOCAIS[i].equals(fonte)) return i;
        }
        return -1;
    }

    public static Style recomecar(Style atual) {
        return Style.EMPTY.withFont(atual.getFont());
    }

    public static MutableComponent componente(String texto) {
        MutableComponent raiz = Component.empty();
        if (texto == null || texto.isEmpty()) return raiz;
        StringBuilder atual = new StringBuilder();
        StringBuilder estado = new StringBuilder();
        ResourceLocation fonteAtual = null;
        int tamanho = texto.length();
        for (int i = 0; i < tamanho; i++) {
            char c = texto.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < tamanho) {
                char codigo = Character.toLowerCase(texto.charAt(i + 1));
                if (CODIGOS_VANILLA.indexOf(codigo) >= 0) {
                    i++;
                    if (codigo == 'r') {
                        anexar(raiz, atual, fonteAtual);
                        estado.setLength(0);
                        fonteAtual = null;
                    } else {
                        atual.append('§').append(codigo);
                        estado.append('§').append(codigo);
                    }
                    continue;
                }
                if (c == '&' && ehCodigoEstilo(codigo)) {
                    anexar(raiz, atual, fonteAtual);
                    atual.append(estado);
                    fonteAtual = fonte(codigo);
                    i++;
                    continue;
                }
            }
            atual.append(c);
        }
        anexar(raiz, atual, fonteAtual);
        if (raiz.getSiblings().size() == 1) return raiz.getSiblings().get(0).copy();
        return raiz;
    }

    private static boolean ehSequencia(String texto, int i) {
        char c = texto.charAt(i);
        if ((c != '&' && c != '§') || i + 1 >= texto.length()) return false;
        char codigo = Character.toLowerCase(texto.charAt(i + 1));
        return CODIGOS_VANILLA.indexOf(codigo) >= 0 || (c == '&' && ehCodigoEstilo(codigo));
    }

    public static String semCodigos(String texto) {
        return prefixoVisivel(texto, Integer.MAX_VALUE, true);
    }

    public static String prefixoVisivel(String texto, int visiveis) {
        return prefixoVisivel(texto, visiveis, false);
    }

    private static String prefixoVisivel(String texto, int visiveis, boolean removerCodigos) {
        StringBuilder saida = new StringBuilder(texto.length());
        int contados = 0;
        for (int i = 0; i < texto.length(); i++) {
            if (ehSequencia(texto, i)) {
                if (!removerCodigos) saida.append(texto, i, i + 2);
                i++;
                continue;
            }
            if (contados >= visiveis) break;
            saida.append(texto.charAt(i));
            contados++;
        }
        return saida.toString();
    }

    public static Component converter(Component texto) {
        if (texto == null || !contem(texto)) return texto;
        return transformar(texto);
    }

    private static boolean contem(Component texto) {
        if (texto.getContents() instanceof PlainTextContents.LiteralContents literal && literal.text().indexOf('&') >= 0) return true;
        for (Component irmao : texto.getSiblings()) {
            if (contem(irmao)) return true;
        }
        return false;
    }

    private static MutableComponent transformar(Component texto) {
        MutableComponent novo;
        if (texto.getContents() instanceof PlainTextContents.LiteralContents literal && literal.text().indexOf('&') >= 0) {
            novo = Component.empty().append(componente(literal.text()));
        } else {
            novo = MutableComponent.create(texto.getContents());
        }
        novo.setStyle(texto.getStyle());
        for (Component irmao : texto.getSiblings()) {
            novo.append(transformar(irmao));
        }
        return novo;
    }

    public static Packet<?> converterPacote(Packet<?> pacote) {
        if (SemConversao.ativo()) return pacote;
        if (pacote instanceof ClientboundSystemChatPacket p) {
            Component novo = converter(p.content());
            return novo == p.content() ? pacote : new ClientboundSystemChatPacket(novo, p.overlay());
        }
        if (pacote instanceof ClientboundSetTitleTextPacket p) {
            Component novo = converter(p.text());
            return novo == p.text() ? pacote : new ClientboundSetTitleTextPacket(novo);
        }
        if (pacote instanceof ClientboundSetSubtitleTextPacket p) {
            Component novo = converter(p.text());
            return novo == p.text() ? pacote : new ClientboundSetSubtitleTextPacket(novo);
        }
        if (pacote instanceof ClientboundSetActionBarTextPacket p) {
            Component novo = converter(p.text());
            return novo == p.text() ? pacote : new ClientboundSetActionBarTextPacket(novo);
        }
        if (pacote instanceof ClientboundPlayerChatPacket p) {
            Component base = p.unsignedContent() != null ? p.unsignedContent() : Component.literal(p.body().content());
            Component novo = converter(base);
            if (novo == base) return pacote;
            return new ClientboundPlayerChatPacket(p.sender(), p.index(), p.signature(), p.body(), novo, p.filterMask(), p.chatType());
        }
        return pacote;
    }

    private static void anexar(MutableComponent raiz, StringBuilder atual, ResourceLocation fonte) {
        String texto = atual.toString();
        atual.setLength(0);
        if (texto.isEmpty()) return;
        MutableComponent parte = Component.literal(texto);
        if (fonte != null) parte.setStyle(Style.EMPTY.withFont(fonte));
        raiz.append(parte);
    }
}
