package net.aelysium.aelysiummod.sistemas.rp.escola;

public enum ElementoMagico {

    FOGO("Fogo", "§c"),
    GELO("Gelo", "§b"),
    NATUREZA("Natureza", "§a"),
    RAIO("Raio", "§e"),
    SACRA("Sacra", "§6"),
    SANGUE("Sangue", "§4"),
    VAZIO("Vazio", "§5"),
    NENHUMA("Nenhuma", "§7");

    public final String nome;
    public final String cor;

    ElementoMagico(String nome, String cor) {
        this.nome = nome;
        this.cor = cor;
    }

    public String colorido() {
        return cor + nome + "§r";
    }
}
