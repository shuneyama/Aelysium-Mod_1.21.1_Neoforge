package net.aelysium.aelysiummod.protecao.regiao;

public enum FlagRegiao {
    QUEBRAR_BLOCOS("Quebrar Blocos", "Permite quebrar blocos na região"),
    COLOCAR_BLOCOS("Colocar Blocos", "Permite colocar blocos na região"),
    PVP("PvP", "Permite combate entre jogadores"),
    USAR_ITENS("Usar Itens", "Permite usar itens na região"),
    INTERAGIR_BLOCOS("Interagir com Blocos", "Permite interagir com blocos (baús, portas, etc)"),
    DANO_ANIMAIS("Dano em Animais", "Permite dar dano em animais"),
    DANO_MONSTROS("Dano em Monstros", "Permite dar dano em monstros"),
    EXPLOSAO_DANO("Explosão (Dano)", "Permite receber dano de explosões"),
    EXPLOSAO_DESTRUIR("Explosão (Destruir)", "Permite explosões destruírem blocos"),
    SPAWN_ANIMAIS("Spawn de Animais", "Permite spawn de animais"),
    SPAWN_MONSTROS("Spawn de Monstros", "Permite spawn de monstros"),
    FOGO_ESPALHAR("Fogo Espalhar", "Permite fogo se espalhar"),
    DANO_FOGO("Dano de Fogo", "Permite receber dano de fogo"),
    DANO_QUEDA("Dano de Queda", "Permite receber dano de queda"),
    TELEPORTE("Teleporte", "Permite teleportar na região"),
    SAIR_REGIAO("Sair da Região", "Permite sair da região"),
    ENTRAR_REGIAO("Entrar na Região", "Permite entrar na região"),
    AVISAR_DONO_ENTRADA("Avisar Dono (Entrada)", "Avisa donos quando alguém entra"),
    AVISAR_DONO_SAIDA("Avisar Dono (Saída)", "Avisa donos quando alguém sai");

    private final String nome;
    private final String descricao;

    FlagRegiao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
}
