package net.aelysium.aelysiummod.sistemas.rp.personagem;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

public record Personagem(
        String id, String nome,
        String prefixo, int prefixoCor1, int prefixoCor2, int prefixoFormato,
        int nickCor, int nickFormato,
        String sufixo, int sufixoCor1, int sufixoCor2, int sufixoFormato,
        String pronomes, String badge, String raca
) {

    public static final int NOME_MAXIMO = 20;
    public static final int AFIXO_MAXIMO = 20;

    public Personagem comId(String novoId) {
        return new Personagem(novoId, nome, prefixo, prefixoCor1, prefixoCor2, prefixoFormato, nickCor, nickFormato,
                sufixo, sufixoCor1, sufixoCor2, sufixoFormato, pronomes, badge, raca);
    }

    public Personagem saneado() {
        return new Personagem(id == null ? "" : id,
                limitar(nome, NOME_MAXIMO),
                limitar(prefixo, AFIXO_MAXIMO), prefixoCor1 & 0xFFFFFF, prefixoCor2 < 0 ? -1 : prefixoCor2 & 0xFFFFFF, prefixoFormato & 31,
                nickCor & 0xFFFFFF, nickFormato & 31,
                limitar(sufixo, AFIXO_MAXIMO), sufixoCor1 & 0xFFFFFF, sufixoCor2 < 0 ? -1 : sufixoCor2 & 0xFFFFFF, sufixoFormato & 31,
                pronomes == null ? "" : pronomes, badge == null ? "" : limitar(badge, 64), raca == null ? "none" : raca);
    }

    private static String limitar(String texto, int maximo) {
        if (texto == null) return "";
        String limpo = texto.replaceAll("\\s+", "");
        return limpo.length() > maximo ? limpo.substring(0, maximo) : limpo;
    }

    public void escrever(FriendlyByteBuf buf) {
        buf.writeUtf(id, 64);
        buf.writeUtf(nome, 64);
        buf.writeUtf(prefixo, 64);
        buf.writeInt(prefixoCor1);
        buf.writeInt(prefixoCor2);
        buf.writeInt(prefixoFormato);
        buf.writeInt(nickCor);
        buf.writeInt(nickFormato);
        buf.writeUtf(sufixo, 64);
        buf.writeInt(sufixoCor1);
        buf.writeInt(sufixoCor2);
        buf.writeInt(sufixoFormato);
        buf.writeUtf(pronomes, 32);
        buf.writeUtf(badge, 64);
        buf.writeUtf(raca, 32);
    }

    public static Personagem ler(FriendlyByteBuf buf) {
        return new Personagem(buf.readUtf(64), buf.readUtf(64),
                buf.readUtf(64), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readInt(), buf.readInt(),
                buf.readUtf(64), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readUtf(32), buf.readUtf(64), buf.readUtf(32));
    }

    public JsonObject paraJson() {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("nome", nome);
        o.addProperty("prefixo", prefixo);
        o.addProperty("prefixoCor1", prefixoCor1);
        o.addProperty("prefixoCor2", prefixoCor2);
        o.addProperty("prefixoFormato", prefixoFormato);
        o.addProperty("nickCor", nickCor);
        o.addProperty("nickFormato", nickFormato);
        o.addProperty("sufixo", sufixo);
        o.addProperty("sufixoCor1", sufixoCor1);
        o.addProperty("sufixoCor2", sufixoCor2);
        o.addProperty("sufixoFormato", sufixoFormato);
        o.addProperty("pronomes", pronomes);
        o.addProperty("badge", badge);
        o.addProperty("raca", raca);
        return o;
    }

    public static Personagem deJson(JsonObject o) {
        return new Personagem(
                o.get("id").getAsString(), o.get("nome").getAsString(),
                texto(o, "prefixo"), inteiro(o, "prefixoCor1", 0xFFFFFF), inteiro(o, "prefixoCor2", -1), inteiro(o, "prefixoFormato", 0),
                inteiro(o, "nickCor", 0xFFFFFF), inteiro(o, "nickFormato", 0),
                texto(o, "sufixo"), inteiro(o, "sufixoCor1", 0xFFFFFF), inteiro(o, "sufixoCor2", -1), inteiro(o, "sufixoFormato", 0),
                texto(o, "pronomes"), texto(o, "badge"), o.has("raca") ? o.get("raca").getAsString() : "none").saneado();
    }

    private static String texto(JsonObject o, String chave) {
        return o.has(chave) ? o.get(chave).getAsString() : "";
    }

    private static int inteiro(JsonObject o, String chave, int padrao) {
        return o.has(chave) ? o.get(chave).getAsInt() : padrao;
    }
}
