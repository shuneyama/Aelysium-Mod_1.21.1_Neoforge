package net.aelysium.aelysiummod.sistemas.social.apelido;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;

public class FormatoApelido implements DadosEmArquivos.Formato {

    private static final String[] NOMES_FORMATO = {"negrito", "italico", "sublinhado", "riscado", "ofuscado"};

    @Override
    public JsonElement paraArquivo(JsonElement bruto) {
        JsonObject e = bruto.getAsJsonObject();
        JsonObject saida = new JsonObject();
        saida.add("nick", parte(e, "nick", "nickCor", null, "nickFormat"));
        saida.add("prefixo", parte(e, "prefix", "prefixCor1", "prefixCor2", "prefixFormat"));
        saida.add("sufixo", parte(e, "suffix", "suffixCor1", "suffixCor2", "suffixFormat"));
        return saida;
    }

    @Override
    public JsonElement doArquivo(JsonElement legivel) {
        JsonObject l = legivel.getAsJsonObject();
        if (l.has("nick") && l.get("nick").isJsonPrimitive()) return legivel;
        JsonObject bruto = new JsonObject();
        leitura(bruto, l.getAsJsonObject("nick"), "nick", "nickCor", null, "nickFormat");
        leitura(bruto, l.getAsJsonObject("prefixo"), "prefix", "prefixCor1", "prefixCor2", "prefixFormat");
        leitura(bruto, l.getAsJsonObject("sufixo"), "suffix", "suffixCor1", "suffixCor2", "suffixFormat");
        return bruto;
    }

    private static JsonObject parte(JsonObject e, String texto, String cor1, String cor2, String formato) {
        JsonObject p = new JsonObject();
        p.addProperty("texto", e.has(texto) ? e.get(texto).getAsString() : "");
        p.addProperty("cor", UtilitariosApelido.toHex(inteiro(e, cor1, 0xFFFFFF)));
        if (cor2 != null && inteiro(e, cor2, -1) >= 0) p.addProperty("corFinal", UtilitariosApelido.toHex(inteiro(e, cor2, -1)));
        JsonArray nomes = new JsonArray();
        int flags = inteiro(e, formato, 0);
        for (int i = 0; i < NOMES_FORMATO.length; i++) {
            if ((flags & (1 << i)) != 0) nomes.add(NOMES_FORMATO[i]);
        }
        p.add("formato", nomes);
        return p;
    }

    private static void leitura(JsonObject bruto, JsonObject p, String texto, String cor1, String cor2, String formato) {
        if (p == null) p = new JsonObject();
        bruto.addProperty(texto, p.has("texto") ? p.get("texto").getAsString() : "");
        bruto.addProperty(cor1, p.has("cor") ? UtilitariosApelido.parseHex(p.get("cor").getAsString()) : 0xFFFFFF);
        if (cor2 != null) {
            bruto.addProperty(cor2, p.has("corFinal") ? UtilitariosApelido.parseHex(p.get("corFinal").getAsString()) : -1);
        }
        int flags = 0;
        if (p.has("formato")) {
            for (JsonElement el : p.getAsJsonArray("formato")) {
                for (int i = 0; i < NOMES_FORMATO.length; i++) {
                    if (NOMES_FORMATO[i].equalsIgnoreCase(el.getAsString())) flags |= 1 << i;
                }
            }
        }
        bruto.addProperty(formato, flags);
    }

    private static int inteiro(JsonObject e, String chave, int padrao) {
        return e.has(chave) ? e.get(chave).getAsInt() : padrao;
    }
}
