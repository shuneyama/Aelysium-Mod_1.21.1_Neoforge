package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class CamadasFilhotes {

    public static final ModelLayerLocation VACA_ADULTA = UtilFilhotes.camada("vaca_adulta");
    public static final ModelLayerLocation PORCO_ADULTO = UtilFilhotes.camada("porco_adulto");
    public static final ModelLayerLocation VACA_QUENTE = UtilFilhotes.camada("vaca_quente");
    public static final ModelLayerLocation VACA_FRIA = UtilFilhotes.camada("vaca_fria");
    public static final ModelLayerLocation PORCO_FRIO = UtilFilhotes.camada("porco_frio");
    public static final ModelLayerLocation GALINHA_FRIA = UtilFilhotes.camada("galinha_fria");

    public static final ModelLayerLocation VACA = UtilFilhotes.camada("vaca_filhote");
    public static final ModelLayerLocation PORCO = UtilFilhotes.camada("porco_filhote");
    public static final ModelLayerLocation GALINHA = UtilFilhotes.camada("galinha_filhote");
    public static final ModelLayerLocation OVELHA = UtilFilhotes.camada("ovelha_filhote");
    public static final ModelLayerLocation OVELHA_LA = UtilFilhotes.camada("ovelha_filhote_la");
    public static final ModelLayerLocation URSO_POLAR = UtilFilhotes.camada("urso_polar_filhote");
    public static final ModelLayerLocation TARTARUGA = UtilFilhotes.camada("tartaruga_filhote");
    public static final ModelLayerLocation CABRA = UtilFilhotes.camada("cabra_filhote");
    public static final ModelLayerLocation PANDA = UtilFilhotes.camada("panda_filhote");
    public static final ModelLayerLocation LOBO = UtilFilhotes.camada("lobo_filhote");
    public static final ModelLayerLocation FELINO = UtilFilhotes.camada("felino_filhote");
    public static final ModelLayerLocation FELINO_COLEIRA = UtilFilhotes.camada("felino_filhote_coleira");
    public static final ModelLayerLocation RAPOSA = UtilFilhotes.camada("raposa_filhote");
    public static final ModelLayerLocation COELHO = UtilFilhotes.camada("coelho_filhote");
    public static final ModelLayerLocation COELHO_ADULTO = UtilFilhotes.camada("coelho_adulto");
    public static final ModelLayerLocation ABELHA = UtilFilhotes.camada("abelha_filhote");
    public static final ModelLayerLocation TATU = UtilFilhotes.camada("tatu_filhote");
    public static final ModelLayerLocation CAMELO = UtilFilhotes.camada("camelo_filhote");
    public static final ModelLayerLocation AXOLOTE = UtilFilhotes.camada("axolote_filhote");
    public static final ModelLayerLocation LHAMA = UtilFilhotes.camada("lhama_filhote");
    public static final ModelLayerLocation LHAMA_DECORACAO = UtilFilhotes.camada("lhama_filhote_decoracao");
    public static final ModelLayerLocation CAVALO = UtilFilhotes.camada("cavalo_filhote");
    public static final ModelLayerLocation BURRO = UtilFilhotes.camada("burro_filhote");
    public static final ModelLayerLocation STRIDER = UtilFilhotes.camada("strider_filhote");
    public static final ModelLayerLocation SNIFFER = UtilFilhotes.camada("sniffer_filhote");
    public static final ModelLayerLocation LULA = UtilFilhotes.camada("lula_filhote");
    public static final ModelLayerLocation GOLFINHO = UtilFilhotes.camada("golfinho_filhote");

    private CamadasFilhotes() {
    }

    public static void registrar(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(VACA_ADULTA, ModelosAdultos::vaca);
        event.registerLayerDefinition(PORCO_ADULTO, ModelosAdultos::porco);
        event.registerLayerDefinition(VACA_QUENTE, ModelosAdultos::vacaQuente);
        event.registerLayerDefinition(VACA_FRIA, ModelosAdultos::vacaFria);
        event.registerLayerDefinition(PORCO_FRIO, ModelosAdultos::porcoFrio);
        event.registerLayerDefinition(GALINHA_FRIA, ModelosAdultos::galinhaFria);

        event.registerLayerDefinition(VACA, ModeloVacaFilhote::criarCamada);
        event.registerLayerDefinition(PORCO, ModeloPorcoFilhote::criarCamada);
        event.registerLayerDefinition(GALINHA, ModeloGalinhaFilhote::criarCamada);
        event.registerLayerDefinition(OVELHA, ModeloOvelhaFilhote::criarCamada);
        event.registerLayerDefinition(OVELHA_LA, ModeloOvelhaFilhote::criarCamada);
        event.registerLayerDefinition(URSO_POLAR, ModeloUrsoPolarFilhote::criarCamada);
        event.registerLayerDefinition(TARTARUGA, ModeloTartarugaFilhote::criarCamada);
        event.registerLayerDefinition(CABRA, ModeloCabraFilhote::criarCamada);
        event.registerLayerDefinition(PANDA, ModeloPandaFilhote::criarCamada);
        event.registerLayerDefinition(LOBO, ModeloLoboFilhote::criarCamada);
        event.registerLayerDefinition(FELINO, AnimacaoFelinoFilhote::criarCamada);
        event.registerLayerDefinition(FELINO_COLEIRA, AnimacaoFelinoFilhote::criarCamada);
        event.registerLayerDefinition(RAPOSA, ModeloRaposaFilhote::criarCamada);
        event.registerLayerDefinition(COELHO, ModeloCoelhoFilhote::criarCamada);
        event.registerLayerDefinition(COELHO_ADULTO, ModeloCoelhoAdulto::criarCamada);
        event.registerLayerDefinition(ABELHA, ModeloAbelhaFilhote::criarCamada);
        event.registerLayerDefinition(TATU, ModeloTatuFilhote::criarCamada);
        event.registerLayerDefinition(CAMELO, ModeloCameloFilhote::criarCamada);
        event.registerLayerDefinition(AXOLOTE, ModeloAxolotlFilhote::criarCamada);
        event.registerLayerDefinition(LHAMA, ModeloLhamaFilhote::criarCamada);
        event.registerLayerDefinition(LHAMA_DECORACAO, ModeloLhamaFilhote::criarCamadaDecoracao);
        event.registerLayerDefinition(CAVALO, ModeloCavaloFilhote::criarCamada);
        event.registerLayerDefinition(BURRO, ModeloBurroFilhote::criarCamada);
        event.registerLayerDefinition(STRIDER, ModeloStriderFilhote::criarCamada);
        event.registerLayerDefinition(SNIFFER, ModeloSnifferFilhote::criarCamada);
        event.registerLayerDefinition(LULA, ModelosAquaticos::lula);
        event.registerLayerDefinition(GOLFINHO, ModelosAquaticos::golfinho);
    }
}
