package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class DecorSimples extends BlocoDecorativoBase {
    public DecorSimples(Properties propriedades, String id, int apoio, boolean semColisao) {
        super(propriedades, id, apoio, semColisao);
    }

    @Override
    protected DirectionProperty direcao() {
        return null;
    }
}
