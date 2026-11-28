package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.configuracao.AelysiumDiretorioConfiguracao;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfiguracaoChefes {
    public static final String FLOR = "flor_do_vazio";
    public static final String FLOR_ESPINHOS = FLOR + ".espinhos";
    public static final String FLOR_ONDA = FLOR + ".onda_de_espinhos";
    public static final String FLOR_ESPOROS = FLOR + ".esporos";
    public static final String FLOR_LAMINAS = FLOR + ".laminas_de_petala";
    public static final String FLOR_FLORESCER = FLOR + ".florescer";
    public static final String FLOR_CONTATO = FLOR + ".espinhos_do_corpo";

    public static final String FROSTMAW = "frostmaw";
    public static final String FROSTMAW_GARRA = FROSTMAW + ".garrada";
    public static final String FROSTMAW_GARRA_DUPLA = FROSTMAW + ".garrada_dupla";
    public static final String FROSTMAW_ESMAGAR = FROSTMAW + ".esmagar";
    public static final String FROSTMAW_SOPRO = FROSTMAW + ".sopro_de_gelo";
    public static final String FROSTMAW_BOLA = FROSTMAW + ".bola_de_gelo";

    public static final String QUEBRA = "quebra_de_armadura";
    public static final String VOO = "voo";

    private static final String ARQUIVO = "chefes.toml";
    private static final String CHAVE_VERSAO = "versao";
    private static final int VERSAO = 3;
    private static final Map<Integer, Map<String, Object>> PADROES_ANTIGOS = Map.of(
            1, Map.of(
                    QUEBRA + ".pontos_por_golpe", 1,
                    FLOR_ESPOROS + ".dano", 12.0
            ),
            2, Map.of(
                    FROSTMAW + ".dano_extra_por_fase", 1.0
            )
    );
    private static final String SEGUNDOS = "Tempo em segundos.";

    private record Entrada(String chave, Object padrao, String comentario) {}

    private static final Map<String, Entrada> ENTRADAS = new LinkedHashMap<>();
    private static volatile Map<String, Object> valores = new HashMap<>();
    private static volatile Map<String, AtaqueChefe> ataques = new HashMap<>();
    private static volatile Map<String, StatusChefe> status = new HashMap<>();

    static {
        entrada(QUEBRA + ".pontos_maximos", 6, "Máximo de pontos de armadura que um jogador pode perder ao mesmo tempo para os chefes.");
        entrada(QUEBRA + ".pontos_por_golpe", 2, "Pontos de armadura removidos por golpe que tem remove_armadura ligado.");
        entrada(QUEBRA + ".duracao_segundos", 15, "Quanto tempo cada ponto removido demora para voltar. " + SEGUNDOS);
        entrada(QUEBRA + ".intervalo_minimo_segundos", 2.5, "Tempo mínimo entre duas remoções no mesmo jogador, para não acontecer em sequência. " + SEGUNDOS);

        entrada(VOO + ".bloqueio_segundos", 2.0, "Por quanto tempo o voo (Valkyrias, elytra, voo de criativo liberado) fica desligado ao ser atingido por um golpe com derruba_voo. " + SEGUNDOS);

        status(FLOR, 750.0, 4.0, 0.5, 0);
        entrada(FLOR + ".recarga_inicial_ticks", 80, "Espera antes do primeiro ataque e depois de um ataque cancelado, em ticks (20 ticks = 1 segundo).");
        entrada(FLOR + ".musgo_fofo.vida_abaixo_de", 0.5, "A partir de qual fração da vida a Flor do Vazio começa a contar ataques para o musgo fofo (0.5 = abaixo da metade).");
        entrada(FLOR + ".musgo_fofo.a_cada_ataques", 7, "A cada quantos ataques o chão embaixo do jogador vira musgo fofo (afunda como neve fofa e envenena). 0 desativa.");
        entrada(FLOR + ".musgo_fofo.raio", 2.0, "Raio da área de musgo fofo em blocos, a partir dos pés do jogador (2.0 = área de 4x4 com cantos arredondados). A camada tem 1 bloco de altura.");
        entrada(FLOR + ".musgo_fofo.alvos", 1, "Em quantos jogadores diferentes o musgo fofo aparece ao mesmo tempo.");
        entrada(FLOR + ".musgo_fofo.duracao_segundos", 10.0, "Depois de quanto tempo o musgo fofo volta a ser o bloco original. Se alguém estiver dentro nessa hora, o bloco espera a pessoa sair. " + SEGUNDOS);
        entrada(FLOR + ".musgo_fofo.nivel_veneno", 1, "Nível do veneno de quem está dentro do musgo fofo (1 = Veneno I). 0 desativa o veneno.");
        entrada(FLOR + ".musgo_fofo.duracao_veneno_segundos", 3.0, "Quanto o veneno dura depois de sair do musgo fofo. " + SEGUNDOS);
        ataque(FLOR_ESPINHOS, "Espinhos que brotam do chão embaixo dos jogadores.", 13.0, 150, "peso", 1.0, 4, "", 1, 0, true);
        ataque(FLOR_ONDA, "Ondas de espinhos em anéis ao redor da flor. Acerta todos na área.", 13.0, 120, "peso", 1.0, -1, "minecraft:slowness", 2, 4, false);
        ataque(FLOR_ESPOROS, "Bola de esporos que explode em uma área venenosa. O dano ignora armadura, resistência da armadura (toughness) e encantamentos de proteção.", 8.0, 100, "peso", 1.0, 3, "minecraft:poison", 1, 7, false);
        ataque(FLOR_LAMINAS, "Rajadas de lâminas de pétala. Aparece abaixo de 50% da vida, ou a qualquer momento se houver alguém voando.", 12.0, 120, "peso", 1.0, 2, "minecraft:slowness", 1, 3, true);
        voo(FLOR_LAMINAS, true, true);
        ataque(FLOR_FLORESCER, "Cria flores curativas e muros de vinhas ao perder cada quarto da vida. Não causa dano.", -1, 120, null, 0, -1, null, 0, 0, false);
        ataque(FLOR_CONTATO, "Dano dos espinhos do corpo em quem encosta ou bate de perto.", 12.0, -1, null, 0, -1, "", 1, 0, false);

        status(FROSTMAW, 750.0, 0.0, 0.3, 120);
        entrada(FROSTMAW + ".dano_extra_por_fase", 0.5, "Dano extra dos ataques (menos o esmagar) a cada quarto de vida perdido, em fração do dano base (0.5 = +50% por fase: x1.5, x2, x2.5). 0 desativa.");
        entrada(FROSTMAW + ".neve_fofa.vida_abaixo_de", 0.5, "A partir de qual fração da vida o Frostmaw começa a contar ataques para a neve fofa (0.5 = abaixo da metade).");
        entrada(FROSTMAW + ".neve_fofa.a_cada_ataques", 7, "A cada quantos ataques o chão embaixo do jogador vira neve fofa. 0 desativa.");
        entrada(FROSTMAW + ".neve_fofa.raio", 2.0, "Raio da área de neve fofa em blocos, a partir dos pés do jogador (2.0 = área de 4x4 com cantos arredondados). A camada tem 1 bloco de altura.");
        entrada(FROSTMAW + ".neve_fofa.alvos", 1, "Em quantos jogadores diferentes a neve fofa aparece ao mesmo tempo.");
        entrada(FROSTMAW + ".neve_fofa.duracao_segundos", 10.0, "Depois de quanto tempo a neve fofa volta a ser o bloco original. Se alguém estiver dentro da neve nessa hora, o bloco espera a pessoa sair. " + SEGUNDOS);
        entrada(FROSTMAW + ".avanco.fase_minima", 1, "A partir de quantos quartos de vida perdidos o Frostmaw começa a dar botes nos jogadores (0 = desde o início, 1 = abaixo de 75%).");
        entrada(FROSTMAW + ".avanco.recarga_ticks", 100, "Tempo entre dois botes, em ticks (20 ticks = 1 segundo). Também acelera com aceleracao_por_fase.");
        entrada(FROSTMAW + ".avanco.distancia_minima", 7.0, "Distância mínima do alvo para dar o bote, em blocos.");
        entrada(FROSTMAW + ".avanco.distancia_maxima", 20.0, "Distância máxima do alvo para dar o bote, em blocos.");
        ataque(FROSTMAW_GARRA, "Golpe de garra corpo a corpo em arco. Acerta todos na frente.", 11.0, -1, null, 0, -1, "", 1, 0, false);
        ataque(FROSTMAW_GARRA_DUPLA, "Dois golpes de garra seguidos que quebram escudos.", 10.0, -1, "chance", 0.25, -1, "", 1, 0, true);
        ataque(FROSTMAW_ESMAGAR, "Salto que esmaga o chão. Só aparece abaixo de 60% da vida.", 40.0, 500, null, 0, -1, "minecraft:slowness", 2, 5, true);
        ataque(FROSTMAW_SOPRO, "Sopro de gelo contínuo em cone (dano por acerto). O dano ignora armadura e resistência da armadura (toughness).", 2.0, 260, null, 0, -1, "", 1, 0, false);
        voo(FROSTMAW_SOPRO, null, true);
        ataque(FROSTMAW_BOLA, "Bola de gelo atirada em quem está longe ou voando. O dano ignora armadura e resistência da armadura (toughness).", 6.0, 200, null, 0, 2, "minecraft:slowness", 2, 4, false);
        voo(FROSTMAW_BOLA, true, true);
    }

    private ConfiguracaoChefes() {
    }

    private static void entrada(String chave, Object padrao, String comentario) {
        ENTRADAS.put(chave, new Entrada(chave, padrao, comentario));
    }

    private static void status(String chefe, double vida, double armadura, double cura, int trocaAlvo) {
        entrada(chefe + ".vida", vida, "Vida máxima do chefe.");
        entrada(chefe + ".armadura", armadura, "Pontos de armadura do chefe.");
        entrada(chefe + ".dano_maximo_recebido", 15.0, "Maior dano que o chefe pode receber de um único golpe, já depois da armadura. 0 desativa o limite.");
        entrada(chefe + ".cura_por_tick", cura, "Vida recuperada por tick quando está fora de combate. 0 desativa.");
        entrada(chefe + ".cura_espera_segundos", 10, "Tempo sem levar dano antes de começar a se curar. " + SEGUNDOS);
        entrada(chefe + ".aceleracao_por_fase", 0.5, "Quanto os ataques aceleram a cada quarto de vida perdido (0.5 = +50% por fase, acumulando: +50%, +100%, +150%). Reduz o tempo de recarga entre ataques. 0 desativa.");
        if (trocaAlvo > 0) entrada(chefe + ".troca_de_alvo_ticks", trocaAlvo, "De quanto em quanto tempo reavalia qual jogador focar, em ticks. A Flor do Vazio troca a cada ataque.");
    }

    private static void ataque(String chave, String descricao, double dano, int recarga, String nomePeso, double peso, int alvos, String efeito, int nivel, int duracao, boolean armadura) {
        if (dano >= 0) entrada(chave + ".dano", dano, descricao + " Dano em dificuldade Normal.");
        if (recarga >= 0) entrada(chave + ".recarga_ticks", recarga, (dano < 0 ? descricao + " " : "") + "Tempo até o chefe poder atacar de novo depois deste ataque, em ticks (20 ticks = 1 segundo).");
        if ("peso".equals(nomePeso)) entrada(chave + ".peso", peso, "Peso no sorteio do próximo ataque. Maior = mais frequente. 0 desativa o ataque.");
        if ("chance".equals(nomePeso)) entrada(chave + ".chance", peso, "Chance de usar este ataque quando ele está disponível (0.25 = 25%). 0 desativa o ataque.");
        if (alvos >= 0) entrada(chave + ".alvos", alvos, "Quantos jogadores diferentes este ataque mira ao mesmo tempo.");
        if (efeito != null) {
            entrada(chave + ".efeito", efeito, "Efeito aplicado em quem é atingido, pelo id (ex.: \"minecraft:slowness\"). Deixe \"\" para nenhum.");
            entrada(chave + ".nivel_efeito", nivel, "Nível do efeito (1 = nível I).");
            entrada(chave + ".duracao_efeito_segundos", duracao, "Duração do efeito. " + SEGUNDOS);
            entrada(chave + ".remove_armadura", armadura, "Se este golpe remove pontos de armadura (veja a seção quebra_de_armadura).");
        }
    }

    private static void voo(String chave, Boolean mira, Boolean derruba) {
        if (mira != null) entrada(chave + ".mira_voadores", mira, "Se este ataque prioriza jogadores voando entre os seus alvos.");
        if (derruba != null) entrada(chave + ".derruba_voo", derruba, "Se acertar alguém voando, desliga o voo dessa pessoa (veja a seção voo).");
    }

    public static synchronized void carregar() {
        Path caminho = AelysiumDiretorioConfiguracao.file(ARQUIVO);
        Map<String, Object> lidos = new HashMap<>();
        try {
            Map<String, Object> existentes = lerExistentes(caminho);
            int versaoArquivo = Files.exists(caminho) ? versaoLida(caminho) : VERSAO;
            CommentedConfig saida = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
            saida.set(CHAVE_VERSAO, VERSAO);
            saida.setComment(CHAVE_VERSAO, " Versão do arquivo, usada para atualizar padrões antigos. Não altere.");
            for (Entrada entrada : ENTRADAS.values()) {
                Object atual = existentes.get(entrada.chave());
                if (atual == null || !compativel(atual, entrada.padrao())) atual = entrada.padrao();
                if (igualPadraoAntigo(versaoArquivo, entrada.chave(), atual)) atual = entrada.padrao();
                saida.set(entrada.chave(), atual);
                saida.setComment(entrada.chave(), " " + entrada.comentario());
                lidos.put(entrada.chave(), atual);
            }
            try (Writer escritor = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8)) {
                TomlFormat.instance().createWriter().write(saida, escritor);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar " + ARQUIVO + ": ", e);
        }
        valores = lidos;
        montar();
    }

    private static boolean igualPadraoAntigo(int versaoArquivo, String chave, Object valor) {
        if (!(valor instanceof Number v)) return false;
        for (int versao = versaoArquivo; versao < VERSAO; versao++) {
            Map<String, Object> padroes = PADROES_ANTIGOS.get(versao);
            if (padroes != null && padroes.get(chave) instanceof Number n && n.doubleValue() == v.doubleValue()) return true;
        }
        return false;
    }

    private static int versaoLida(Path caminho) {
        try (CommentedFileConfig cfg = CommentedFileConfig.builder(caminho).charset(StandardCharsets.UTF_8).build()) {
            cfg.load();
            Object valor = cfg.get(CHAVE_VERSAO);
            return valor instanceof Number n ? n.intValue() : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private static boolean compativel(Object valor, Object padrao) {
        if (padrao instanceof Number) return valor instanceof Number;
        return padrao.getClass().isInstance(valor);
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

    private static void montar() {
        Map<String, AtaqueChefe> novosAtaques = new HashMap<>();
        for (String chave : new String[]{FLOR_ESPINHOS, FLOR_ONDA, FLOR_ESPOROS, FLOR_LAMINAS, FLOR_FLORESCER, FLOR_CONTATO, FROSTMAW_GARRA, FROSTMAW_GARRA_DUPLA, FROSTMAW_ESMAGAR, FROSTMAW_SOPRO, FROSTMAW_BOLA}) {
            double peso = ENTRADAS.containsKey(chave + ".peso") ? numero(chave + ".peso") : numero(chave + ".chance");
            novosAtaques.put(chave, new AtaqueChefe(
                    (float) Math.max(numero(chave + ".dano"), 0),
                    Math.max(inteiro(chave + ".recarga_ticks"), 1),
                    Math.max(peso, 0),
                    Math.max(inteiro(chave + ".alvos"), 1),
                    efeito(texto(chave + ".efeito")),
                    Math.max(inteiro(chave + ".nivel_efeito"), 1),
                    Math.max(inteiro(chave + ".duracao_efeito_segundos"), 0) * 20,
                    booleano(chave + ".remove_armadura"),
                    booleano(chave + ".mira_voadores"),
                    booleano(chave + ".derruba_voo")));
        }
        Map<String, StatusChefe> novosStatus = new HashMap<>();
        for (String chefe : new String[]{FLOR, FROSTMAW}) {
            novosStatus.put(chefe, new StatusChefe(
                    Math.max(numero(chefe + ".vida"), 1),
                    Math.max(numero(chefe + ".armadura"), 0),
                    (float) Math.max(numero(chefe + ".dano_maximo_recebido"), 0),
                    (float) Math.max(numero(chefe + ".cura_por_tick"), 0),
                    Math.max(inteiro(chefe + ".cura_espera_segundos"), 0) * 20,
                    Math.max(inteiro(chefe + ".troca_de_alvo_ticks"), 20)));
        }
        ataques = novosAtaques;
        status = novosStatus;
    }

    private static Holder<MobEffect> efeito(String id) {
        if (id == null || id.isBlank()) return null;
        ResourceLocation local = ResourceLocation.tryParse(id.trim());
        if (local == null) return null;
        Holder<MobEffect> efeito = BuiltInRegistries.MOB_EFFECT.getHolder(local).orElse(null);
        if (efeito == null) LoggerFactory.getLogger(AelysiumMod.MOD_ID).warn("Efeito desconhecido em " + ARQUIVO + ": " + id);
        return efeito;
    }

    public static AtaqueChefe ataque(String chave) {
        AtaqueChefe ataque = ataques.get(chave);
        if (ataque == null) {
            montar();
            ataque = ataques.get(chave);
        }
        return ataque;
    }

    public static StatusChefe status(String chefe) {
        StatusChefe atual = status.get(chefe);
        if (atual == null) {
            montar();
            atual = status.get(chefe);
        }
        return atual;
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

    private static String texto(String chave) {
        Object valor = valores.get(chave);
        if (valor instanceof String texto) return texto;
        Entrada entrada = ENTRADAS.get(chave);
        return entrada != null && entrada.padrao() instanceof String padrao ? padrao : null;
    }
}
