package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfiguracaoDificuldade {

    private record Entrada(String chave, Object padrao, String comentario) {}

    private static final Map<String, Entrada> ENTRADAS = new LinkedHashMap<>();
    private static final String CHAVE_ENTIDADES_EXCLUIDAS = "geral.entidades_excluidas";
    private static final String CHAVE_HUMANOIDES = "geral.mobs_humanoides";
    private static final String CHAVE_ATAQUE_ACELERADO = "ataque_acelerado.entidades";
    private static final String CHAVE_VEX = "vex.entidades";
    private static final String CHAVE_BRUXAS = "bruxa.entidades";
    private static final String CHAVE_CAVALEIROS = "fantasma.cavaleiros";
    private static final String CHAVE_HORDA_PERMITIDOS = "horda.entidades_permitidas";
    private static final String CHAVE_HORDA_EXCLUIDOS = "horda.entidades_excluidas";

    private static volatile Map<String, Object> valores = new HashMap<>();
    private static volatile Map<String, List<String>> listas = new HashMap<>();
    private static volatile Map<String, Map<ResourceLocation, Boolean>> cacheListas = new ConcurrentHashMap<>();

    static {
        entrada("geral.bloquear_drop_equipamento", true, "Impede que mobs afetados dropem armaduras, armas e ferramentas.");
        entrada("geral.armadura_para_humanoides_genericos", true, "Mobs hostis sem tratamento próprio (inclusive de outros mods) que andam no chão e têm tamanho de humanoide recebem armadura como os zumbis.");
        entrada(CHAVE_HUMANOIDES, new ArrayList<String>(), "Mobs tratados como zumbi (armadura), mesmo fora do critério de tamanho. Aceita o id (modid:mob) ou o mod inteiro (modid). Exemplo: [\"born_in_chaos:zombie_personalizado\", \"born_in_chaos\"].");
        entrada(CHAVE_ENTIDADES_EXCLUIDAS, new ArrayList<String>(), "Mobs que nunca são afetados. Aceita o id (modid:mob) ou o mod inteiro (modid). Exemplo: [\"minecraft:slime\", \"minecraft\"].");
        entrada("geral.vida_por_nivel", 0.04, "Bônus de vida por nível, em porcentagem (0.04 = +4% por nível).");
        entrada("geral.dano_por_nivel", 0.05, "Bônus de dano corpo a corpo por nível, em porcentagem (0.05 = +5% por nível).");
        entrada("geral.dano_projetil_por_nivel", 0.05, "Bônus de dano de projéteis (flechas, bolas de fogo, poções, tridentes) por nível, em porcentagem (0.05 = +5% por nível). 0 desativa.");
        entrada("geral.velocidade_por_nivel", 0.02, "Bônus de velocidade de movimento por nível, em porcentagem (0.02 = +2% por nível).");
        entrada("geral.resistencia_empurrao_por_nivel", 0.04, "Resistência a empurrão por nível, em porcentagem (0.04 = +4% por nível, 1.0 = imune).");
        entrada("geral.penetracao_armadura_por_nivel", 0.04, "Porcentagem da proteção da armadura do jogador que é ignorada, por nível (0.04 = 4% por nível).");
        entrada("geral.visao_blocos_por_nivel", 4.0, "Blocos adicionais de alcance de visão por nível.");
        entrada("geral.armadura_por_nivel", 0.3, "Pontos de armadura extras por nível.");

        entrada("golpe_especial.nivel_minimo_corrosao", 3, "Nível mínimo para a corrosão de armadura entrar no sorteio.");
        entrada("golpe_especial.nivel_minimo_desarme", 4, "Nível mínimo para o desarme entrar no sorteio.");
        entrada("golpe_especial.chance_base", 0.03, "Chance base de um ataque do mob virar ataque especial, em porcentagem (0.03 = 3%).");
        entrada("golpe_especial.chance_por_nivel", 0.012, "Chance adicional por nível, em porcentagem (0.012 = +1,2% por nível).");
        entrada("golpe_especial.peso_critico", 3, "Peso do ataque crítico no sorteio de ataques especiais (0 desativa).");
        entrada("golpe_especial.peso_efeito", 3, "Peso dos efeitos negativos no sorteio de ataques especiais (0 desativa).");
        entrada("golpe_especial.peso_corrosao", 2, "Peso da corrosão de armadura no sorteio de ataques especiais (0 desativa).");
        entrada("golpe_especial.peso_desarme", 1, "Peso do desarme no sorteio de ataques especiais (0 desativa).");
        entrada("golpe_especial.recarga_base_ticks", 160, "Recarga entre ataques especiais no mesmo jogador, em ticks (20 ticks = 1 segundo).");
        entrada("golpe_especial.recarga_reducao_por_nivel_ticks", 12, "Quanto a recarga diminui por nível, em ticks.");
        entrada("golpe_especial.recarga_minima_ticks", 20, "Menor recarga possível, em ticks.");

        entrada("golpe_critico.multiplicador_base", 1.5, "Multiplicador de dano do ataque crítico no nível 0 (1.5 = 150% do dano).");
        entrada("golpe_critico.multiplicador_por_nivel", 0.1, "Multiplicador adicional por nível (0.1 = +10% do dano por nível).");

        entrada("efeito_negativo.nivel_minimo_fraqueza", 1, "Nível mínimo para Fraqueza entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_lentidao", 1, "Nível mínimo para Lentidão entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_fadiga", 2, "Nível mínimo para Fadiga entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_fome", 2, "Nível mínimo para Fome entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_poison", 4, "Nível mínimo para Veneno entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_escuridao", 5, "Nível mínimo para Escuridão entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_cegueira", 6, "Nível mínimo para Cegueira entrar no sorteio.");
        entrada("efeito_negativo.nivel_minimo_wither", 8, "Nível mínimo para Wither entrar no sorteio.");
        entrada("efeito_negativo.nivel_amplificador_2", 7, "Nível a partir do qual Fraqueza, Lentidão e Fadiga passam ao nível II.");
        entrada("efeito_negativo.duracao_base_segundos", 3, "Duração base dos efeitos negativos, em segundos.");
        entrada("efeito_negativo.duracao_por_nivel_segundos", 1, "Segundos adicionais de duração por nível.");
        entrada("efeito_negativo.duracao_maxima_efeitos_perigosos_segundos", 4, "Limite de duração para Veneno, Cegueira e Wither, em segundos.");

        entrada("corrosao.armadura_base", 1.0, "Pontos de armadura removidos do jogador no nível 0.");
        entrada("corrosao.armadura_por_nivel", 0.6, "Pontos de armadura removidos a mais por nível.");
        entrada("corrosao.dureza_por_nivel", 0.3, "Pontos de resistência da armadura (toughness) removidos por nível.");
        entrada("corrosao.duracao_base_segundos", 8, "Duração base da corrosão, em segundos.");
        entrada("corrosao.duracao_por_nivel_segundos", 1, "Segundos adicionais de duração por nível.");

        entrada("desarme.pickup_delay_ticks", 60, "Tempo até o jogador poder pegar de volta o item largado, em ticks (20 ticks = 1 segundo).");

        entrada("horda.niveis_por_extra", 5, "A cada quantos níveis o máximo de membros extras da horda aumenta em 1.");
        entrada(CHAVE_HORDA_PERMITIDOS, new ArrayList<String>(), "Mobs que podem formar hordas. Aceita o id (modid:mob) ou o mod inteiro (modid). Vazio = todos os mobs afetados.");
        entrada(CHAVE_HORDA_EXCLUIDOS, new ArrayList<String>(List.of("minecraft:slime", "minecraft:magma_cube")), "Mobs que nunca formam hordas. Aceita o id (modid:mob) ou o mod inteiro (modid).");
        entrada("horda.chance_base", 0.1, "Chance base de um spawn natural virar horda, em porcentagem (0.1 = 10%). Precisa de hordas ligado.");
        entrada("horda.chance_por_nivel", 0.02, "Chance adicional por nível, em porcentagem (0.02 = +2% por nível).");
        entrada("horda.limite_monstros_na_area", 14, "Não cria horda se já houver tantos monstros na área de verificação.");
        entrada("horda.raio_verificacao_limite_blocos", 12.0, "Raio, em blocos, da área em que os monstros são contados para o limite.");
        entrada("horda.raio_membros_blocos", 2.5, "Distância máxima, em blocos, entre o mob original e os membros extras da horda.");

        entrada("fantasma.nivel_minimo", 5, "Nível mínimo para os fantasmas cavalgados aparecerem.");
        entrada("fantasma.nivel_para_dois", 8, "Nível a partir do qual podem existir 2 fantasmas por jogador.");
        entrada(CHAVE_CAVALEIROS, new ArrayList<String>(List.of("minecraft:zombie", "minecraft:husk", "minecraft:drowned", "minecraft:zombie_villager", "minecraft:skeleton", "minecraft:stray", "minecraft:bogged", "minecraft:wither_skeleton")), "Mobs que podem montar um fantasma para atacar jogadores no alto. Aceita o id (modid:mob) ou o mod inteiro (modid).");
        entrada("fantasma.chance_por_nivel_acima_do_minimo", 0.1, "Chance a cada checagem (2 segundos), em porcentagem, por nível acima do mínimo menos 1 (0.1 = 10%).");
        entrada("fantasma.chance_cancelar_voo", 0.30, "Chance de um golpe do fantasma cancelar o voo do jogador, em porcentagem (0.30 = 30%).");
        entrada("fantasma.duracao_cancelar_voo_ticks", 300, "Duração do voo cancelado, em ticks (20 ticks = 1 segundo).");
        entrada("fantasma.recarga_base_ticks", 1200, "Recarga por jogador no nível 0, em ticks.");
        entrada("fantasma.recarga_reducao_por_nivel_ticks", 80, "Quanto a recarga diminui por nível, em ticks.");
        entrada("fantasma.recarga_minima_ticks", 200, "Menor recarga possível, em ticks.");
        entrada("fantasma.altura_minima_blocos", 8.0, "Altura acima do solo para o jogador contar como estando no alto, em blocos.");

        entrada("ataque_acelerado.extra_por_nivel", 0.06, "Chance por tick, por nível, de a entidade de ataque à distância rodar um ataque extra, em porcentagem (0.06 = +6% por nível; no nível 10, ataques até 60% mais rápidos).");
        entrada(CHAVE_ATAQUE_ACELERADO, new ArrayList<String>(List.of("minecraft:blaze", "minecraft:pillager", "minecraft:drowned", "minecraft:breeze")), "Mobs com ataque à distância acelerado. Aceita o id (modid:mob) ou o mod inteiro (modid).");

        entrada("zumbi.armadura_nivel_minimo_diamante", 9, "Nível mínimo para peças de diamante aparecerem.");
        entrada("zumbi.armadura_chance_peca_base", 0.2, "Chance base de cada peça de armadura aparecer, em porcentagem (0.2 = 20%).");
        entrada("zumbi.armadura_chance_peca_por_nivel", 0.08, "Chance adicional por nível, em porcentagem (0.08 = +8% por nível).");
        entrada("zumbi.armadura_chance_diamante", 0.3, "Chance de uma peça ser de diamante quando o nível permite, em porcentagem (0.3 = 30%).");
        entrada("zumbi.reforcos_por_nivel", 0.03, "Chance extra de chamar reforços por nível, em porcentagem (0.03 = +3% por nível).");
        entrada("zumbi.armadura_tier_por_nivel", 0.35, "Tier máximo da armadura = parte inteira de (nível x este valor). 0 = couro, 1 = malha, 2 = ferro.");

        entrada("esqueleto.intervalo_arco_base_ticks", 20, "Intervalo entre disparos no nível 0, em ticks (20 ticks = 1 segundo).");
        entrada("esqueleto.intervalo_arco_reducao_por_nivel", 1.5, "Quanto o intervalo diminui por nível, em ticks.");
        entrada("esqueleto.intervalo_arco_minimo_ticks", 5, "Menor intervalo possível, em ticks.");

        entrada("aranha.teia_chance_base", 0.05, "Chance base por segundo de colocar teia, em porcentagem (0.05 = 5%). Precisa de griefing ligado.");
        entrada("aranha.teia_chance_por_nivel", 0.02, "Chance adicional por nível, em porcentagem (0.02 = +2% por nível).");
        entrada("aranha.escala_por_nivel", -0.04, "Variação de tamanho por nível, em porcentagem (-0.04 = 4% menor por nível).");
        entrada("aranha.velocidade_por_nivel", 0.03, "Bônus extra de velocidade por nível, em porcentagem (0.03 = +3% por nível).");
        entrada("aranha.teia_distancia_alvo", 12.0, "Distância máxima do alvo para colocar teia, em blocos.");
        entrada("aranha.teia_duracao_ticks", 400, "Tempo até a teia sumir sozinha, em ticks (1 segundo = 20 ticks).");

        entrada("creeper.chance_eletrico_por_nivel", 0.1, "Chance de nascer elétrico por nível, em porcentagem (0.1 = +10% por nível).");

        entrada("slime.velocidade_por_nivel", 0.04, "Bônus extra de velocidade por nível, em porcentagem (0.04 = +4% por nível). Deixa os pulos mais longos.");

        entrada(CHAVE_BRUXAS, new ArrayList<String>(List.of("minecraft:witch")), "Mobs que usam o comportamento tático da bruxa (fugir e jogar poções andando). Aceita o id (modid:mob) ou o mod inteiro (modid). O mob precisa ter ataque à distância.");
        entrada("bruxa.distancia_segura", 8.0, "A bruxa foge quando um jogador está mais perto que isso, em blocos.");
        entrada("bruxa.distancia_maxima_ataque", 13.0, "A bruxa se aproxima do alvo quando ele está mais longe que isso, em blocos.");
        entrada("bruxa.velocidade_fuga", 1.1, "Multiplicador de velocidade ao fugir (1.1 = 110% da velocidade normal).");
        entrada("bruxa.fuga_duracao_maxima_ticks", 100, "Tempo máximo que a bruxa foge sem parar, em ticks (20 ticks = 1 segundo). Depois disso ela se cansa.");
        entrada("bruxa.fuga_descanso_ticks", 100, "Tempo que a bruxa fica parada, sem fugir, depois de se cansar, em ticks. É a janela para o jogador alcançá-la.");
        entrada("bruxa.intervalo_pocao_base_ticks", 60, "Intervalo entre poções no nível 0, em ticks.");
        entrada("bruxa.intervalo_pocao_reducao_por_nivel_ticks", 3, "Quanto o intervalo diminui por nível, em ticks.");
        entrada("bruxa.intervalo_pocao_minimo_ticks", 25, "Menor intervalo possível, em ticks.");

        entrada(CHAVE_VEX, new ArrayList<String>(List.of("minecraft:vex")), "Mobs voadores que recebem a aceleração extra em direção ao alvo. Aceita o id (modid:mob) ou o mod inteiro (modid).");
        entrada("vex.aceleracao_por_nivel", 0.002, "Aceleração extra por tick, por nível, dos vex em direção ao alvo (0.002 por nível = cerca de +40% de velocidade no nível 10).");

        entrada("efeito_mob.wither_nivel2", 5, "Nível da dificuldade a partir do qual o Wither do esqueleto wither passa ao nível II.");
        entrada("efeito_mob.wither_nivel3", 10, "Nível da dificuldade a partir do qual o Wither do esqueleto wither passa ao nível III.");
        entrada("efeito_mob.wither_duracao_ticks", 100, "Duração do Wither aprimorado do esqueleto wither, em ticks.");
        entrada("efeito_mob.fome_nivel2", 5, "Nível da dificuldade a partir do qual a Fome do husk passa ao nível II.");
        entrada("efeito_mob.fome_nivel3", 9, "Nível da dificuldade a partir do qual a Fome do husk passa ao nível III.");
        entrada("efeito_mob.fome_duracao_ticks", 200, "Duração da Fome aprimorada do husk, em ticks.");
        entrada("efeito_mob.lentidao_nivel2", 4, "Nível da dificuldade a partir do qual a Lentidão do stray passa ao nível II.");
        entrada("efeito_mob.lentidao_nivel3", 9, "Nível da dificuldade a partir do qual a Lentidão do stray passa ao nível III.");
        entrada("efeito_mob.lentidao_duracao_ticks", 160, "Duração da Lentidão aprimorada do stray, em ticks.");
        entrada("efeito_mob.veneno_nivel2", 4, "Nível da dificuldade a partir do qual o Veneno do bogged passa ao nível II.");
        entrada("efeito_mob.veneno_nivel3", 9, "Nível da dificuldade a partir do qual o Veneno do bogged passa ao nível III.");
        entrada("efeito_mob.veneno_duracao_ticks", 120, "Duração do Veneno aprimorado do bogged, em ticks.");
        entrada("efeito_mob.veneno_aranha_nivel2", 4, "Nível da dificuldade a partir do qual o Veneno da aranha das cavernas passa ao nível II.");
        entrada("efeito_mob.veneno_aranha_nivel3", 9, "Nível da dificuldade a partir do qual o Veneno da aranha das cavernas passa ao nível III.");
        entrada("efeito_mob.veneno_aranha_duracao_ticks", 100, "Duração do Veneno aprimorado da aranha das cavernas, em ticks.");
    }

    private static void entrada(String chave, Object padrao, String comentario) {
        ENTRADAS.put(chave, new Entrada(chave, padrao, comentario));
    }

    public static Path pasta(MinecraftServer servidor) {
        Path pasta = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("dificuldade");
        try {
            Files.createDirectories(pasta);
        } catch (IOException ignored) {}
        return pasta;
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        Path caminho = pasta(servidor).resolve("dificuldade.toml");
        Map<String, Object> lidos = new HashMap<>();
        Map<String, List<String>> listasLidas = new HashMap<>();

        try {
            Map<String, Object> existentes = lerExistentes(caminho);

            CommentedConfig saida = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
            for (Entrada entrada : ENTRADAS.values()) {
                Object atual = existentes.get(entrada.chave());
                if (atual == null) atual = entrada.padrao();
                saida.set(entrada.chave(), atual);
                saida.setComment(entrada.chave(), " " + entrada.comentario());
                lidos.put(entrada.chave(), atual);
            }

            for (Entrada entrada : ENTRADAS.values()) {
                if (!(entrada.padrao() instanceof List<?>)) continue;
                List<String> textos = new ArrayList<>();
                if (lidos.get(entrada.chave()) instanceof List<?> itens) {
                    for (Object item : itens) textos.add(String.valueOf(item).trim());
                }
                listasLidas.put(entrada.chave(), textos);
            }

            try (Writer escritor = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8)) {
                TomlFormat.instance().createWriter().write(saida, escritor);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar dificuldade.toml: ", e);
        }

        valores = lidos;
        listas = listasLidas;
        cacheListas = new ConcurrentHashMap<>();
    }

    private static Map<String, Object> lerExistentes(Path caminho) {
        Map<String, Object> existentes = new HashMap<>();
        if (!Files.exists(caminho)) return existentes;
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(caminho).charset(StandardCharsets.UTF_8).build()) {
            cfg.load();
            for (Entrada entrada : ENTRADAS.values()) {
                Object valor = cfg.get(entrada.chave());
                if (valor != null) existentes.put(entrada.chave(), valor);
            }
        }
        return existentes;
    }

    public static double numero(String chave) {
        Object valor = valores.get(chave);
        if (valor instanceof Number numero) return numero.doubleValue();
        Entrada entrada = ENTRADAS.get(chave);
        return entrada != null && entrada.padrao() instanceof Number padrao ? padrao.doubleValue() : 0.0;
    }

    public static int inteiro(String chave) {
        return (int) Math.round(numero(chave));
    }

    public static boolean booleano(String chave) {
        Object valor = valores.get(chave);
        if (valor instanceof Boolean booleano) return booleano;
        Entrada entrada = ENTRADAS.get(chave);
        return entrada != null && entrada.padrao() instanceof Boolean padrao && padrao;
    }

    public static boolean listaContem(String chave, ResourceLocation id) {
        Map<String, Map<ResourceLocation, Boolean>> caches = cacheListas;
        Map<ResourceLocation, Boolean> cache = caches.get(chave);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            Map<ResourceLocation, Boolean> anterior = caches.putIfAbsent(chave, cache);
            if (anterior != null) cache = anterior;
        }
        Boolean resultado = cache.get(id);
        if (resultado == null) {
            resultado = calcularListaContem(chave, id);
            cache.put(id, resultado);
        }
        return resultado;
    }

    private static boolean calcularListaContem(String chave, ResourceLocation id) {
        List<String> itens = listas.get(chave);
        if (itens == null) return false;
        String completo = id.toString();
        for (String item : itens) {
            if (item.equals(completo) || item.equals(id.getNamespace())) return true;
        }
        return false;
    }

    public static boolean entidadeExcluida(ResourceLocation id) {
        return listaContem(CHAVE_ENTIDADES_EXCLUIDAS, id);
    }

    public static boolean humanoideConfigurado(ResourceLocation id) {
        return listaContem(CHAVE_HUMANOIDES, id);
    }

    public static boolean vexConfigurado(ResourceLocation id) {
        return listaContem(CHAVE_VEX, id);
    }

    public static boolean bruxaConfigurada(ResourceLocation id) {
        return listaContem(CHAVE_BRUXAS, id);
    }

    public static boolean cavaleiroDeFantasma(ResourceLocation id) {
        return listaContem(CHAVE_CAVALEIROS, id);
    }

    public static boolean podeFormarHorda(ResourceLocation id) {
        if (listaContem(CHAVE_HORDA_EXCLUIDOS, id)) return false;
        List<String> permitidos = listas.get(CHAVE_HORDA_PERMITIDOS);
        return permitidos == null || permitidos.isEmpty() || listaContem(CHAVE_HORDA_PERMITIDOS, id);
    }

    public static boolean ataqueAcelerado(ResourceLocation id) {
        return listaContem(CHAVE_ATAQUE_ACELERADO, id);
    }
}
