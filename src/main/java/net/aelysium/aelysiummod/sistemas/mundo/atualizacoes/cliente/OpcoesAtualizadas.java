package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class OpcoesAtualizadas {

    public enum FrequenciaMusica {
        PADRAO, FREQUENTE, CONSTANTE
    }

    public static final ModConfigSpec.Builder CONSTRUTOR = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue INVERTER_X = CONSTRUTOR.define("invertMouseX", false);
    public static final ModConfigSpec.IntValue JANELA_CORRIDA = CONSTRUTOR.defineInRange("sprintWindow", 7, 1, 20);
    public static final ModConfigSpec.EnumValue<FrequenciaMusica> FREQUENCIA = CONSTRUTOR.defineEnum("musicFrequency", FrequenciaMusica.PADRAO);
    public static final ModConfigSpec.BooleanValue AVISO_MUSICA = CONSTRUTOR.define("showNowPlayingToast", false);
    public static final ModConfigSpec.DoubleValue VOLUME_UI = CONSTRUTOR.defineInRange("uiVolume", 1.0D, 0.0D, 1.0D);
    public static final ModConfigSpec.BooleanValue ALTERNAR_ATAQUE = CONSTRUTOR.define("toggleAttack", false);
    public static final ModConfigSpec.BooleanValue ALTERNAR_USAR = CONSTRUTOR.define("toggleUse", false);

    public static final ModConfigSpec.BooleanValue MIPMAP_TODOS = CONSTRUTOR.define("mipmapAllBlocks", true);
    public static final ModConfigSpec.BooleanValue FOLHAS_TRANSPARENTES = CONSTRUTOR.define("seeThroughLeaves", true);

    public static final ModConfigSpec.BooleanValue VINHETA = CONSTRUTOR.define("vignette", true);
    public static final ModConfigSpec.BooleanValue TELA_CHEIA_EXCLUSIVA = CONSTRUTOR.define("exclusiveFullscreen", false);
    public static final ModConfigSpec.IntValue RAIO_CLIMA = CONSTRUTOR.defineInRange("weatherRadius", 5, 3, 10);
    public static final ModConfigSpec.IntValue DENSIDADE_CHUVA = CONSTRUTOR.defineInRange("rainDensityPercent", 50, 10, 100);
    public static final ModConfigSpec.IntValue DENSIDADE_NEVE = CONSTRUTOR.defineInRange("snowDensityPercent", 50, 10, 100);

    public static final ModConfigSpec.BooleanValue ESCONDER_SODIUM_F3 = CONSTRUTOR.define("hideSodiumDebugInfo", true);

    public static final ModConfigSpec SPEC = CONSTRUTOR.build();

    private OpcoesAtualizadas() {
    }
}
