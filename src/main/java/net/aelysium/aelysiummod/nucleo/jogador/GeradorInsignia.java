package net.aelysium.aelysiummod.nucleo.jogador;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public final class GeradorInsignia {

    public static final int ALTURA = 10;
    public static final int MAX_CARACTERES = 10;
    private static final int PADDING = 4;
    private static final int LARGURA_ESPACO = 3;

    public record Imagem(int largura, int altura, int[] pixels) {}

    private static final String[] FUNDO = {
        ".HHH.HHHH.HHH.",
        "HHHH.HHHH.HHHH",
        "HHCC.CCCC.CCHH",
        "CCCC.CCCC.CCCC",
        "CCCC.CCCC.CCCC",
        "SSCC.CCCC.CCSS",
        "SSSS.SSSS.SSSS",
        ".SSS.SSSS.SSS."
    };

    private static final String[] LETRAS = {
        ".WW./WssW/WWWW/WssW/W..W/s..s",
        "WWW./WssW/WWWs/WssW/WWWs/sss.",
        ".WW./WssW/W..s/W..W/sWWs/.ss.",
        "WWW./WssW/W..W/W..W/WWWs/sss.",
        "WWWW/Wsss/WWW./Wss./WWWW/ssss",
        "WWWW/Wsss/WWW./Wss./W.../s...",
        ".WWW/Wsss/W.WW/W.sW/sWWW/.sss",
        "W..W/W..W/WWWW/WssW/W..W/s..s",
        "W/W/W/W/W/s",
        "WWWW/ssWs/..W./W.W./sWs./.s..",
        "W..W/W.Ws/WWs./WsW./W.sW/s..s",
        "W../W../W../W../WWW/sss",
        "W...W/WW.WW/WsWsW/W.s.W/W...W/s...s",
        "W..W/WW.W/WsWW/W.sW/W..W/s..s",
        ".WW./WssW/W..W/W..W/sWWs/.ss.",
        "WWW./WssW/WWWs/Wss./W.../s...",
        ".WW./WssW/W..W/W.Ws/sWsW/.s.s",
        "WWW./WssW/WWWs/WssW/W..W/s..s",
        ".WWW/Wsss/sWW./.ssW/WWWs/sss.",
        "WWW/sWs/.W./.W./.W./.s.",
        "W..W/W..W/W..W/W..W/sWWs/.ss.",
        "W.W/W.W/W.W/sWs/.W./.s.",
        "W.W.W/W.W.W/W.W.W/sWsWs/.W.W./.s.s.",
        "W.W/W.W/sWs/WsW/W.W/s.s",
        "W.W/W.W/sWs/.W./.W./.s.",
        "WWWW/sssW/.WWs/Wss./WWWW/ssss"
    };

    private GeradorInsignia() {}

    public static String normalizar(String bruto) {
        if (bruto == null) return "";
        String base = Normalizer.normalize(bruto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base.length() && sb.length() < MAX_CARACTERES; i++) {
            char c = base.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append(c);
            } else if (Character.isWhitespace(c) && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    private static int larguraLetra(char c) {
        if (c == ' ') return LARGURA_ESPACO;
        return LETRAS[c - 'A'].split("/")[0].length();
    }

    public static int larguraTexto(String texto) {
        if (texto.isEmpty()) return 0;
        int total = 0;
        for (int i = 0; i < texto.length(); i++) {
            total += larguraLetra(texto.charAt(i));
            if (i > 0) total += 1;
        }
        return total;
    }

    public static int larguraTotal(String texto) {
        return larguraTexto(texto) + PADDING * 2;
    }

    public static Imagem gerar(String textoBruto, int corRgb) {
        String texto = normalizar(textoBruto);
        int largura = larguraTotal(texto);
        int[] pixels = new int[largura * ALTURA];

        float[] hsl = rgbParaHsl((corRgb >> 16) & 255, (corRgb >> 8) & 255, corRgb & 255);
        int principal = 0xFF000000 | (corRgb & 0xFFFFFF);
        int brilho = hslParaArgb(hsl[0], hsl[1], clamp(hsl[2] + 0.08f));
        int sombra = hslParaArgb(hsl[0], hsl[1], clamp(hsl[2] - 0.06f));
        int sombraLetra = escurecer(corRgb, 0.65f);

        for (int linha = 0; linha < FUNDO.length; linha++) {
            String modelo = FUNDO[linha];
            int y = 2 + linha;
            for (int x = 0; x < largura; x++) {
                int coluna;
                if (x < 4) coluna = x;
                else if (x >= largura - 4) coluna = 10 + (x - (largura - 4));
                else coluna = 5;
                char c = modelo.charAt(coluna);
                int cor = switch (c) {
                    case 'H' -> brilho;
                    case 'C' -> principal;
                    case 'S' -> sombra;
                    default -> 0;
                };
                pixels[y * largura + x] = cor;
            }
        }

        int cursor = PADDING;
        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            if (i > 0) cursor += 1;
            if (letra == ' ') {
                cursor += LARGURA_ESPACO;
                continue;
            }
            String[] linhas = LETRAS[letra - 'A'].split("/");
            for (int ly = 0; ly < linhas.length; ly++) {
                String linhaGlifo = linhas[ly];
                for (int lx = 0; lx < linhaGlifo.length(); lx++) {
                    char p = linhaGlifo.charAt(lx);
                    int px = cursor + lx;
                    int py = 3 + ly;
                    if (p == 'W') pixels[py * largura + px] = 0xFFFFFFFF;
                    else if (p == 's') pixels[py * largura + px] = sombraLetra;
                }
            }
            cursor += linhas[0].length();
        }

        return new Imagem(largura, ALTURA, pixels);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int escurecer(int rgb, float fator) {
        int r = Math.round(((rgb >> 16) & 255) * fator);
        int g = Math.round(((rgb >> 8) & 255) * fator);
        int b = Math.round((rgb & 255) * fator);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static float[] rgbParaHsl(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float l = (max + min) / 2f;
        float h = 0f, s = 0f;
        if (max != min) {
            float d = max - min;
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == rf) h = (gf - bf) / d + (gf < bf ? 6f : 0f);
            else if (max == gf) h = (bf - rf) / d + 2f;
            else h = (rf - gf) / d + 4f;
            h /= 6f;
        }
        return new float[]{h, s, l};
    }

    private static float hueParaRgb(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    private static int hslParaArgb(float h, float s, float l) {
        float r, g, b;
        if (s == 0f) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
            float p = 2f * l - q;
            r = hueParaRgb(p, q, h + 1f / 3f);
            g = hueParaRgb(p, q, h);
            b = hueParaRgb(p, q, h - 1f / 3f);
        }
        return 0xFF000000 | (Math.round(r * 255f) << 16) | (Math.round(g * 255f) << 8) | Math.round(b * 255f);
    }

    public static byte[] png(Imagem imagem) throws IOException {
        ByteArrayOutputStream bruto = new ByteArrayOutputStream();
        for (int y = 0; y < imagem.altura(); y++) {
            bruto.write(0);
            for (int x = 0; x < imagem.largura(); x++) {
                int p = imagem.pixels()[y * imagem.largura() + x];
                bruto.write((p >> 16) & 255);
                bruto.write((p >> 8) & 255);
                bruto.write(p & 255);
                bruto.write((p >>> 24) & 255);
            }
        }

        Deflater compressor = new Deflater();
        compressor.setInput(bruto.toByteArray());
        compressor.finish();
        ByteArrayOutputStream comprimido = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!compressor.finished()) {
            int n = compressor.deflate(buffer);
            comprimido.write(buffer, 0, n);
        }
        compressor.end();

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        saida.write(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        ByteArrayOutputStream cabecalho = new ByteArrayOutputStream();
        DataOutputStream dc = new DataOutputStream(cabecalho);
        dc.writeInt(imagem.largura());
        dc.writeInt(imagem.altura());
        dc.writeByte(8);
        dc.writeByte(6);
        dc.writeByte(0);
        dc.writeByte(0);
        dc.writeByte(0);

        escreverBloco(saida, "IHDR", cabecalho.toByteArray());
        escreverBloco(saida, "IDAT", comprimido.toByteArray());
        escreverBloco(saida, "IEND", new byte[0]);
        return saida.toByteArray();
    }

    private static void escreverBloco(ByteArrayOutputStream saida, String tipo, byte[] dados) throws IOException {
        DataOutputStream d = new DataOutputStream(saida);
        d.writeInt(dados.length);
        byte[] tipoBytes = tipo.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        d.write(tipoBytes);
        d.write(dados);
        CRC32 crc = new CRC32();
        crc.update(tipoBytes);
        crc.update(dados);
        d.writeInt((int) crc.getValue());
    }
}
