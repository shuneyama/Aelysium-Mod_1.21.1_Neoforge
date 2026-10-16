package net.aelysium.aelysiummod.nucleo.jogador;

public record InsigniaPersonalizada(String id, String texto, int cor, int indice) {

    public static final int CODEPOINT_BASE = 0xE100;
    public static final int MAXIMO = 256;

    public static boolean ehCodepoint(int codigo) {
        return codigo >= CODEPOINT_BASE && codigo < CODEPOINT_BASE + MAXIMO;
    }

    public int codepoint() {
        return CODEPOINT_BASE + indice;
    }

    public int largura() {
        return GeradorInsignia.larguraTotal(GeradorInsignia.normalizar(texto));
    }

    public String glifos() {
        return new String(Character.toChars(codepoint())).repeat(largura());
    }

    public static String idPara(String textoNormalizado, int cor) {
        return textoNormalizado.toLowerCase(java.util.Locale.ROOT).replace(' ', '_') + "_" + String.format("%06x", cor & 0xFFFFFF);
    }
}
