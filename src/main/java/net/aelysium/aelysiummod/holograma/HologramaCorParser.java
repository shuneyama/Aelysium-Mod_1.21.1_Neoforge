package net.aelysium.aelysiummod.holograma;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//holograma criar teste &abem vindo
//holograma criar teste #FF5500texto laranja
//holograma criar teste <gradiente:#FF0000:#00FF00>texto degradê</gradiente>
//holograma criar teste <gradiente:#FF0000:#FFFF00:#00FF00>arco-iris</gradiente>

public class HologramaCorParser {

    private static final Pattern GRADIENTE_PATTERN =
            Pattern.compile("<gradiente:((?:#[0-9a-fA-F]{6}:?)+)>(.*?)</gradiente>");

    private static final Pattern HEX_PATTERN = Pattern.compile("#([0-9a-fA-F]{6})");

    private static final int[] CORES_MINECRAFT = {
            0x000000, // §0 - preto
            0x0000AA, // §1 - azul escuro
            0x00AA00, // §2 - verde escuro
            0x00AAAA, // §3 - ciano escuro
            0xAA0000, // §4 - vermelho escuro
            0xAA00AA, // §5 - roxo
            0xFFAA00, // §6 - dourado
            0xAAAAAA, // §7 - cinza
            0x555555, // §8 - cinza escuro
            0x5555FF, // §9 - azul
            0x55FF55, // §a - verde
            0x55FFFF, // §b - ciano
            0xFF5555, // §c - vermelho
            0xFF55FF, // §d - rosa
            0xFFFF55, // §e - amarelo
            0xFFFFFF  // §f - branco
    };

    public static Component parse(String texto) {
        texto = texto.replace("&", "§");

        texto = processarGradientes(texto);

        return montarComponent(texto);
    }

    private static String processarGradientes(String texto) {
        Matcher matcher = GRADIENTE_PATTERN.matcher(texto);
        StringBuilder resultado = new StringBuilder();

        while (matcher.find()) {
            String coresStr = matcher.group(1);
            String conteudo = matcher.group(2);

            List<Integer> cores = new ArrayList<>();
            Matcher hexMatcher = HEX_PATTERN.matcher(coresStr);
            while (hexMatcher.find()) {
                cores.add(Integer.parseInt(hexMatcher.group(1), 16));
            }

            if (cores.size() < 2) {
                matcher.appendReplacement(resultado, Matcher.quoteReplacement(conteudo));
                continue;
            }

            StringBuilder gradiente = new StringBuilder();
            String textoLimpo = conteudo.replaceAll("§.", "");
            int len = textoLimpo.length();

            if (len <= 1) {
                String hex = String.format("#%06X", cores.get(0));
                gradiente.append(hex).append(textoLimpo);
            } else {
                for (int i = 0; i < len; i++) {
                    float progresso = (float) i / (len - 1);
                    int cor = interpolarCores(cores, progresso);
                    String hex = String.format("#%06X", cor);
                    gradiente.append(hex).append(textoLimpo.charAt(i));
                }
            }

            matcher.appendReplacement(resultado, Matcher.quoteReplacement(gradiente.toString()));
        }
        matcher.appendTail(resultado);

        return resultado.toString();
    }

    private static int interpolarCores(List<Integer> cores, float progresso) {
        if (progresso <= 0) return cores.get(0);
        if (progresso >= 1) return cores.get(cores.size() - 1);

        int segmentos = cores.size() - 1;
        float posicao = progresso * segmentos;
        int segmento = (int) posicao;
        float fator = posicao - segmento;

        if (segmento >= segmentos) {
            return cores.get(cores.size() - 1);
        }

        return interpolarDuasCores(cores.get(segmento), cores.get(segmento + 1), fator);
    }

    private static int interpolarDuasCores(int cor1, int cor2, float fator) {
        int r1 = (cor1 >> 16) & 0xFF;
        int g1 = (cor1 >> 8) & 0xFF;
        int b1 = cor1 & 0xFF;

        int r2 = (cor2 >> 16) & 0xFF;
        int g2 = (cor2 >> 8) & 0xFF;
        int b2 = cor2 & 0xFF;

        int r = Math.round(r1 + (r2 - r1) * fator);
        int g = Math.round(g1 + (g2 - g1) * fator);
        int b = Math.round(b1 + (b2 - b1) * fator);

        return (r << 16) | (g << 8) | b;
    }

    private static Component montarComponent(String texto) {
        MutableComponent componente = Component.empty();
        Style estiloAtual = Style.EMPTY;
        StringBuilder buffer = new StringBuilder();

        int i = 0;
        while (i < texto.length()) {

            if (texto.charAt(i) == '#' && i + 6 < texto.length()) {
                String possibleHex = texto.substring(i + 1, i + 7);
                if (possibleHex.matches("[0-9a-fA-F]{6}")) {
                    if (!buffer.isEmpty()) {
                        componente.append(Component.literal(buffer.toString()).withStyle(estiloAtual));
                        buffer.setLength(0);
                    }
                    int cor = Integer.parseInt(possibleHex, 16);
                    estiloAtual = estiloAtual.withColor(TextColor.fromRgb(cor));
                    i += 7;
                    continue;
                }
            }

            if (texto.charAt(i) == '§' && i + 1 < texto.length()) {
                char code = Character.toLowerCase(texto.charAt(i + 1));

                if (!buffer.isEmpty()) {
                    componente.append(Component.literal(buffer.toString()).withStyle(estiloAtual));
                    buffer.setLength(0);
                }

                switch (code) {
                    case '0','1','2','3','4','5','6','7','8','9' -> {
                        int idx = code - '0';
                        estiloAtual = Style.EMPTY.withColor(TextColor.fromRgb(CORES_MINECRAFT[idx]));
                    }
                    case 'a','b','c','d','e','f' -> {
                        int idx = 10 + (code - 'a');
                        estiloAtual = Style.EMPTY.withColor(TextColor.fromRgb(CORES_MINECRAFT[idx]));
                    }
                    case 'k' -> estiloAtual = estiloAtual.withObfuscated(true);
                    case 'l' -> estiloAtual = estiloAtual.withBold(true);
                    case 'm' -> estiloAtual = estiloAtual.withStrikethrough(true);
                    case 'n' -> estiloAtual = estiloAtual.withUnderlined(true);
                    case 'o' -> estiloAtual = estiloAtual.withItalic(true);
                    case 'r' -> estiloAtual = Style.EMPTY;
                    default -> {
                        buffer.append('§').append(texto.charAt(i + 1));
                    }
                }
                i += 2;
                continue;
            }

            buffer.append(texto.charAt(i));
            i++;
        }

        if (!buffer.isEmpty()) {
            componente.append(Component.literal(buffer.toString()).withStyle(estiloAtual));
        }

        return componente;
    }
}