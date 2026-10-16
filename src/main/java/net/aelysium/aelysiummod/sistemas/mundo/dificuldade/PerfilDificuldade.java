package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

public class PerfilDificuldade {

    private static double n(String chave) {
        return ConfiguracaoDificuldade.numero(chave);
    }

    private static int i(String chave) {
        return ConfiguracaoDificuldade.inteiro(chave);
    }

    public static boolean bloquearDropEquipamento() {
        return ConfiguracaoDificuldade.booleano("geral.bloquear_drop_equipamento");
    }

    public static boolean armaduraParaHumanoidesGenericos() {
        return ConfiguracaoDificuldade.booleano("geral.armadura_para_humanoides_genericos");
    }

    public static double distanciaSeguraBruxa() {
        return n("bruxa.distancia_segura");
    }

    public static double distanciaMaximaAtaqueBruxa() {
        return n("bruxa.distancia_maxima_ataque");
    }

    public static int duracaoMaximaFugaBruxa() {
        return i("bruxa.fuga_duracao_maxima_ticks");
    }

    public static int descansoAposFugaBruxa() {
        return i("bruxa.fuga_descanso_ticks");
    }

    public static double velocidadeFugaBruxa() {
        return n("bruxa.velocidade_fuga");
    }

    public static int intervaloPocaoBruxa(int nivel) {
        return Math.max(i("bruxa.intervalo_pocao_minimo_ticks"),
                i("bruxa.intervalo_pocao_base_ticks") - i("bruxa.intervalo_pocao_reducao_por_nivel_ticks") * nivel);
    }

    public static double chanceTickExtraAtaque(int nivel) {
        return Math.min(0.95, n("ataque_acelerado.extra_por_nivel") * nivel);
    }

    private static int amplificador(int nivel, String nivel2, String nivel3) {
        if (nivel >= i(nivel3)) return 2;
        if (nivel >= i(nivel2)) return 1;
        return 0;
    }

    public static int amplificadorWitherEsqueleto(int nivel) {
        return amplificador(nivel, "efeito_mob.wither_nivel2", "efeito_mob.wither_nivel3");
    }

    public static int duracaoWitherEsqueleto() {
        return i("efeito_mob.wither_duracao_ticks");
    }

    public static int amplificadorFomeHusk(int nivel) {
        return amplificador(nivel, "efeito_mob.fome_nivel2", "efeito_mob.fome_nivel3");
    }

    public static int duracaoFomeHusk() {
        return i("efeito_mob.fome_duracao_ticks");
    }

    public static int amplificadorLentidaoStray(int nivel) {
        return amplificador(nivel, "efeito_mob.lentidao_nivel2", "efeito_mob.lentidao_nivel3");
    }

    public static int duracaoLentidaoStray() {
        return i("efeito_mob.lentidao_duracao_ticks");
    }

    public static double bonusDanoProjetil(int nivel) {
        return n("geral.dano_projetil_por_nivel") * nivel;
    }

    public static double raioMembrosHorda() {
        return n("horda.raio_membros_blocos");
    }

    public static double raioLimiteHorda() {
        return n("horda.raio_verificacao_limite_blocos");
    }

    public static double aceleracaoVex(int nivel) {
        return n("vex.aceleracao_por_nivel") * nivel;
    }

    public static int amplificadorVenenoAranha(int nivel) {
        return amplificador(nivel, "efeito_mob.veneno_aranha_nivel2", "efeito_mob.veneno_aranha_nivel3");
    }

    public static int duracaoVenenoAranha() {
        return i("efeito_mob.veneno_aranha_duracao_ticks");
    }

    public static int amplificadorVenenoBogged(int nivel) {
        return amplificador(nivel, "efeito_mob.veneno_nivel2", "efeito_mob.veneno_nivel3");
    }

    public static int duracaoVenenoBogged() {
        return i("efeito_mob.veneno_duracao_ticks");
    }

    public static double bonusVida(int nivel) {
        return n("geral.vida_por_nivel") * nivel;
    }

    public static double bonusDano(int nivel) {
        return n("geral.dano_por_nivel") * nivel;
    }

    public static double bonusVelocidade(int nivel) {
        return n("geral.velocidade_por_nivel") * nivel;
    }

    public static double bonusAlcanceVisao(int nivel) {
        return n("geral.visao_blocos_por_nivel") * nivel;
    }

    public static double bonusResistenciaEmpurrao(int nivel) {
        return n("geral.resistencia_empurrao_por_nivel") * nivel;
    }

    public static double bonusArmadura(int nivel) {
        return n("geral.armadura_por_nivel") * nivel;
    }

    public static double bonusReforcosZumbi(int nivel) {
        return n("zumbi.reforcos_por_nivel") * nivel;
    }

    public static double penetracaoArmadura(int nivel) {
        return Math.min(1.0, n("geral.penetracao_armadura_por_nivel") * nivel);
    }

    public static double chanceGolpeEspecial(int nivel) {
        return n("golpe_especial.chance_base") + n("golpe_especial.chance_por_nivel") * nivel;
    }

    public static int recargaGolpeEspecialTicks(int nivel) {
        return Math.max(i("golpe_especial.recarga_minima_ticks"),
                i("golpe_especial.recarga_base_ticks") - i("golpe_especial.recarga_reducao_por_nivel_ticks") * nivel);
    }

    public static int pesoCritico() {
        return Math.max(0, i("golpe_especial.peso_critico"));
    }

    public static int pesoEfeito() {
        return Math.max(0, i("golpe_especial.peso_efeito"));
    }

    public static int pesoCorrosao() {
        return Math.max(0, i("golpe_especial.peso_corrosao"));
    }

    public static int pesoDesarme() {
        return Math.max(0, i("golpe_especial.peso_desarme"));
    }

    public static int nivelMinimoCorrosao() {
        return i("golpe_especial.nivel_minimo_corrosao");
    }

    public static int nivelMinimoDesarme() {
        return i("golpe_especial.nivel_minimo_desarme");
    }

    public static float multiplicadorGolpeCritico(int nivel) {
        return (float) (n("golpe_critico.multiplicador_base") + n("golpe_critico.multiplicador_por_nivel") * nivel);
    }

    public static int duracaoEfeitoTicks(int nivel) {
        return (int) ((n("efeito_negativo.duracao_base_segundos") + n("efeito_negativo.duracao_por_nivel_segundos") * nivel) * 20);
    }

    public static int duracaoMaximaEfeitoPerigosoTicks() {
        return i("efeito_negativo.duracao_maxima_efeitos_perigosos_segundos") * 20;
    }

    public static int amplificadorEfeito(int nivel) {
        return nivel >= i("efeito_negativo.nivel_amplificador_2") ? 1 : 0;
    }

    public static int nivelMinimoFraqueza() {
        return i("efeito_negativo.nivel_minimo_fraqueza");
    }

    public static int nivelMinimoLentidao() {
        return i("efeito_negativo.nivel_minimo_lentidao");
    }

    public static int nivelMinimoFadiga() {
        return i("efeito_negativo.nivel_minimo_fadiga");
    }

    public static int nivelMinimoFome() {
        return i("efeito_negativo.nivel_minimo_fome");
    }

    public static int nivelMinimoPoison() {
        return i("efeito_negativo.nivel_minimo_poison");
    }

    public static int nivelMinimoEscuridao() {
        return i("efeito_negativo.nivel_minimo_escuridao");
    }

    public static int nivelMinimoCegueira() {
        return i("efeito_negativo.nivel_minimo_cegueira");
    }

    public static int nivelMinimoWither() {
        return i("efeito_negativo.nivel_minimo_wither");
    }

    public static double reducaoArmaduraJogador(int nivel) {
        return n("corrosao.armadura_base") + n("corrosao.armadura_por_nivel") * nivel;
    }

    public static double reducaoDurezaJogador(int nivel) {
        return n("corrosao.dureza_por_nivel") * nivel;
    }

    public static int duracaoCorrosaoTicks(int nivel) {
        return (int) ((n("corrosao.duracao_base_segundos") + n("corrosao.duracao_por_nivel_segundos") * nivel) * 20);
    }

    public static int tempoPickupAposDesarmarTicks() {
        return i("desarme.pickup_delay_ticks");
    }

    public static double chanceCreeperEletrico(int nivel) {
        return n("creeper.chance_eletrico_por_nivel") * nivel;
    }

    public static double escalaAranha(int nivel) {
        return n("aranha.escala_por_nivel") * nivel;
    }

    public static double bonusVelocidadeAranha(int nivel) {
        return n("aranha.velocidade_por_nivel") * nivel;
    }

    public static double chanceTeiaPorSegundo(int nivel) {
        return n("aranha.teia_chance_base") + n("aranha.teia_chance_por_nivel") * nivel;
    }

    public static int duracaoTeiaTicks() {
        return i("aranha.teia_duracao_ticks");
    }

    public static double distanciaTeiaAlvo() {
        return n("aranha.teia_distancia_alvo");
    }


    public static double bonusVelocidadeSlime(int nivel) {
        return n("slime.velocidade_por_nivel") * nivel;
    }

    public static int intervaloMinimoArco(int nivel) {
        return Math.max(i("esqueleto.intervalo_arco_minimo_ticks"),
                Math.round((float) (n("esqueleto.intervalo_arco_base_ticks") - n("esqueleto.intervalo_arco_reducao_por_nivel") * nivel)));
    }

    public static double chanceArmaduraPeca(int nivel) {
        return Math.min(1.0, n("zumbi.armadura_chance_peca_base") + n("zumbi.armadura_chance_peca_por_nivel") * nivel);
    }

    public static int tierMaximoArmadura(int nivel) {
        return Math.max(0, Math.min(2, (int) (nivel * n("zumbi.armadura_tier_por_nivel"))));
    }

    public static double chanceDiamanteArmadura(int nivel) {
        return nivel >= i("zumbi.armadura_nivel_minimo_diamante") ? n("zumbi.armadura_chance_diamante") : 0.0;
    }

    public static double chanceHorda(int nivel) {
        return n("horda.chance_base") + n("horda.chance_por_nivel") * nivel;
    }

    public static int extrasMaximosHorda(int nivel) {
        return 1 + nivel / Math.max(1, i("horda.niveis_por_extra"));
    }

    public static int limiteMonstrosPorHorda() {
        return i("horda.limite_monstros_na_area");
    }

    public static int nivelMinimoFantasma() {
        return i("fantasma.nivel_minimo");
    }

    public static double chanceFantasma(int nivel) {
        return n("fantasma.chance_por_nivel_acima_do_minimo") * (nivel - nivelMinimoFantasma() + 1);
    }

    public static int recargaFantasmaTicks(int nivel) {
        return Math.max(i("fantasma.recarga_minima_ticks"),
                i("fantasma.recarga_base_ticks") - i("fantasma.recarga_reducao_por_nivel_ticks") * nivel);
    }

    public static int limiteFantasmas(int nivel) {
        return nivel >= i("fantasma.nivel_para_dois") ? 2 : 1;
    }

    public static double alturaMinimaFantasma() {
        return n("fantasma.altura_minima_blocos");
    }

    public static double chanceCancelarVoo() {
        return n("fantasma.chance_cancelar_voo");
    }

    public static int duracaoCancelamentoVooTicks() {
        return i("fantasma.duracao_cancelar_voo_ticks");
    }
}
