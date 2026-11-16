package net.aelysium.aelysiummod.conteudo.bloco.decoracao;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public enum MaterialDecoracao {
    PEROLA(2.0F, 10.0F, SoundType.STONE, Ferramenta.PICARETA, true, false),
    MARMORE(2.0F, 10.0F, SoundType.CALCITE, Ferramenta.PICARETA, true, false),
    MADEIRA(2.0F, 3.0F, SoundType.WOOD, Ferramenta.MACHADO, false, false),
    VIDRO(0.3F, 0.3F, SoundType.GLASS, Ferramenta.NENHUMA, false, false),
    CRISTAL(2.0F, 10.0F, SoundType.GLASS, Ferramenta.PICARETA, true, false),
    FOLHAGEM(0.2F, 0.2F, SoundType.GRASS, Ferramenta.ENXADA, false, false),
    LAMPADA(2.0F, 1.0F, SoundType.LANTERN, Ferramenta.PICARETA, false, false),
    HASTE(1.0F, 10.0F, SoundType.BONE_BLOCK, Ferramenta.PICARETA, false, false),
    CORAL(0.2F, 0.2F, SoundType.CORAL_BLOCK, Ferramenta.NENHUMA, false, true),
    TECIDO(0.0F, 1.0F, SoundType.WOOL, Ferramenta.NENHUMA, false, false),
    CONCHA(1.0F, 10.0F, SoundType.METAL, Ferramenta.PICARETA, false, false),
    PETALAS(0.2F, 0.2F, SoundType.GRASS, Ferramenta.NENHUMA, false, true);

    public enum Ferramenta {
        NENHUMA, PICARETA, MACHADO, ENXADA
    }

    private final float dureza;
    private final float resistencia;
    private final SoundType som;
    private final Ferramenta ferramenta;
    private final boolean exigeFerramenta;
    private final boolean semColisao;

    MaterialDecoracao(float dureza, float resistencia, SoundType som, Ferramenta ferramenta, boolean exigeFerramenta, boolean semColisao) {
        this.dureza = dureza;
        this.resistencia = resistencia;
        this.som = som;
        this.ferramenta = ferramenta;
        this.exigeFerramenta = exigeFerramenta;
        this.semColisao = semColisao;
    }

    public Ferramenta ferramenta() {
        return ferramenta;
    }

    public boolean semColisao() {
        return semColisao;
    }

    public BlockBehaviour.Properties propriedades() {
        BlockBehaviour.Properties propriedades = BlockBehaviour.Properties.of().strength(dureza, resistencia).sound(som);
        if (exigeFerramenta) {
            propriedades.requiresCorrectToolForDrops();
        }
        if (semColisao) {
            propriedades.noCollission();
        }
        return propriedades;
    }
}
