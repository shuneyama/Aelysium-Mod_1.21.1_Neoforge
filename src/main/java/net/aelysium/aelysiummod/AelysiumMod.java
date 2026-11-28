package net.aelysium.aelysiummod;

import com.natamus.collective_common_neoforge.check.ShouldLoadCheck;
import net.aelysium.aelysiummod.sistemas.admin.discord.ConfiguracaoDiscord;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoAnotacao;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosAnotacao;
import net.aelysium.aelysiummod.sistemas.admin.historico.DadosHistorico;
import net.aelysium.aelysiummod.sistemas.midia.holograma.GerenciadorHologramas;
import net.aelysium.aelysiummod.sistemas.midia.tocar.GerenciadorSom;
import net.aelysium.aelysiummod.sistemas.midia.holograma.TemporizadorHolograma;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.sistemas.social.chat.DadosChatJogador;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.OuvinteFiltroReceitas;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.registro.ModModificadoresLoot;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoBanimentoReceitas;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.CompatibilidadeCurios;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ManipuladorTickInventario;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.EventosBanimentoItem;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.RedeListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.DadosBanimentoCliente;
import net.aelysium.aelysiummod.nucleo.comando.AelysiumComandos;
import net.aelysium.aelysiummod.sistemas.rp.deus.FormaDivina;
import net.aelysium.aelysiummod.sistemas.rp.deus.TemporizadorVanish;
import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.aelysium.aelysiummod.registro.ModItens;
import net.aelysium.aelysiummod.registro.ModMenus;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.EventosLootModsDesativados;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.EventosTrocaModsDesativados;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.registro.ModParticulas;
import net.aelysium.aelysiummod.sistemas.admin.protecao.evento.ManipuladorEventosMovimento;
import net.aelysium.aelysiummod.sistemas.admin.protecao.evento.ManipuladorEventosProtecao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.evento.VarredorMobsRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.RegiaoCacheCliente;
import net.aelysium.aelysiummod.sistemas.rp.raca.ManipuladorLoginRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.TemporizadorRaca;
import net.aelysium.aelysiummod.sistemas.mundo.teleporte.TemporizadorTeleporte;
import net.aelysium.aelysiummod.registro.AbaCriativo;
import net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoMod;
import net.aelysium.aelysiummod.sistemas.admin.listabranca.GerenciadorListaBrancaOffline;
import net.aelysium.aelysiummod.registro.RegistroNpc;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.sistemas.rp.deus.ProvedorConsultaDeus;
import net.aelysium.aelysiummod.sistemas.rp.habilidade.ProvedorConsultaHabilidades;
import net.aelysium.aelysiummod.sistemas.rp.raca.ProvedorConsultaRaca;
import net.aelysium.aelysiummod.sistemas.rp.vidas.ProvedorConsultaVidas;
import net.aelysium.aelysiummod.sistemas.social.apelido.ProvedorConsultaApelido;
import net.aelysium.aelysiummod.sistemas.social.insignia.ProvedorConsultaInsignia;
import net.aelysium.aelysiummod.compatibilidade.voicechat.ConfiguracaoVoz;
import net.aelysium.aelysiummod.compatibilidade.waystones.IntegracaoWaystones;
import net.aelysium.aelysiummod.sistemas.social.maozinha.ManipuladorEventosMaozinha;
import net.aelysium.aelysiummod.sistemas.social.maozinha.TemporizadorMaozinha;
import net.aelysium.aelysiummod.sistemas.admin.protecao.ProvedorConsultaVoo;
import net.aelysium.aelysiummod.nucleo.servico.RegistroServicos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ProvedorListaNegraMods;
import net.aelysium.aelysiummod.sistemas.admin.protecao.evento.ManipuladorEventosAmbienteProtecao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.evento.ManipuladorEventosCombateProtecao;

@Mod(AelysiumMod.MOD_ID)
public class AelysiumMod {
    public static final String MOD_ID = "aelysiummod";

    public AelysiumMod(IEventBus modEventBus, ModContainer modContainer) {
        if (!ShouldLoadCheck.shouldLoad(MOD_ID)) return;

        net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.RegrasAtualizadas.registrar();
        net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.mundo.flora.ModFlora.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.ModJardimPalido.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.mundo.lithynor.ModLithynor.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.chefes.ModChefes.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.AtributosNovos.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.mundo.flora.ModItensNovos.registrar(modEventBus);
        net.aelysium.aelysiummod.sistemas.mundo.mochila.ReceitasMochila.registrar(modEventBus);
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas.SPEC, "aelysiummod-atualizacoes-client.toml");
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        AbaCriativo.register(modEventBus);
        net.aelysium.aelysiummod.registro.ModComponentesDados.register(modEventBus);
        ModItens.register(modEventBus);
        ModBlocos.register(modEventBus);
        net.aelysium.aelysiummod.registro.ModBlocosDecoracao.registrar(modEventBus);
        net.aelysium.aelysiummod.registro.ModEntidadesBloco.register(modEventBus);
        net.aelysium.aelysiummod.registro.ModEntidadesBlocoCofre.register(modEventBus);
        ModParticulas.register(modEventBus);
        ModEfeitos.register(modEventBus);
        RegistroNpc.register(modEventBus);
        ModMenus.register(modEventBus);
        ModModificadoresLoot.register(modEventBus);
        net.aelysium.aelysiummod.registro.ModEntidades.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        PerfilJogador.registrarRaca(new ProvedorConsultaRaca());
        PerfilJogador.registrarDeus(new ProvedorConsultaDeus());
        PerfilJogador.registrarHabilidades(new ProvedorConsultaHabilidades());
        PerfilJogador.registrarApelido(new ProvedorConsultaApelido());
        PerfilJogador.registrarInsignia(new ProvedorConsultaInsignia());
        PerfilJogador.registrarVidas(new ProvedorConsultaVidas());
        PerfilJogador.registrarVoo(new ProvedorConsultaVoo());
        RegistroServicos.registrarListaNegraMods(new ProvedorListaNegraMods());

        NeoForge.EVENT_BUS.register(new TemporizadorRaca());
        NeoForge.EVENT_BUS.register(new TemporizadorVanish());
        NeoForge.EVENT_BUS.register(new ManipuladorLoginRaca());
        NeoForge.EVENT_BUS.register(new FormaDivina());
        NeoForge.EVENT_BUS.register(new ManipuladorEventosProtecao());
        NeoForge.EVENT_BUS.register(new ManipuladorEventosCombateProtecao());
        NeoForge.EVENT_BUS.register(new ManipuladorEventosAmbienteProtecao());
        NeoForge.EVENT_BUS.register(new ManipuladorEventosMovimento());
        NeoForge.EVENT_BUS.register(new VarredorMobsRegiao());
        NeoForge.EVENT_BUS.register(new TemporizadorTeleporte());
        NeoForge.EVENT_BUS.register(new TemporizadorHolograma());
        NeoForge.EVENT_BUS.register(new TemporizadorMaozinha());
        NeoForge.EVENT_BUS.register(new ConfiguracaoVoz());
        NeoForge.EVENT_BUS.register(new ManipuladorEventosMaozinha());
        NeoForge.EVENT_BUS.register(new net.aelysium.aelysiummod.sistemas.rp.morte.TemporizadorMorte());
        NeoForge.EVENT_BUS.register(new net.aelysium.aelysiummod.sistemas.rp.morte.OuvinteMorte());
        NeoForge.EVENT_BUS.register(new net.aelysium.aelysiummod.sistemas.rp.morte.LimiteTotens());
        NeoForge.EVENT_BUS.register(new net.aelysium.aelysiummod.conteudo.efeito.ManipuladorChoqueRestricao());
        NeoForge.EVENT_BUS.register(new net.aelysium.aelysiummod.sistemas.mundo.idade.TravaIdade());


        NeoForge.EVENT_BUS.register(new EventosBanimentoItem());
        NeoForge.EVENT_BUS.register(new net.aelysium.aelysiummod.sistemas.admin.banimentos.EventosBanimentoEstacaoCriacao());
        NeoForge.EVENT_BUS.register(new EventosTrocaModsDesativados());
        NeoForge.EVENT_BUS.register(new EventosLootModsDesativados());
        NeoForge.EVENT_BUS.register(new ManipuladorTickInventario());
        if (CompatibilidadeCurios.isCuriosLoaded()) {
            NeoForge.EVENT_BUS.register(new CompatibilidadeCurios());
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(new AelysiumComandos());
        IntegracaoWaystones.registrar();
        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocos.DAMA_DA_NOITE.getId(), ModBlocos.POTTED_DAMA_DA_NOITE);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocos.DENTE_DE_LEAO_DOURADO.getId(), ModBlocos.POTTED_DENTE_DE_LEAO_DOURADO);
            net.aelysium.aelysiummod.nucleo.comando.AelysiumOpcoesSeletor.registrar();
            net.aelysium.aelysiummod.sistemas.mundo.jardimpalido.ModJardimPalido.configuracaoComum();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        AelysiumGuarda.verificar(event.getServer());
        if (!AelysiumGuarda.isAutorizado()) return;

        net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium.inicializar(event.getServer());
        DadosChatJogador.setDataFolder(
                event.getServer().getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("baloes"),
                event.getServer().getWorldPath(LevelResource.ROOT).resolve("aelysium_baloes")
        );
        GerenciadorSom.inicializar();
        DadosHistorico.inicializar(event.getServer());
        ConfiguracaoAnotacao.load();
        DadosAnotacao.inicializar(DadosHistorico.getPastaAnotacoes());
        GerenciadorListaBrancaOffline.inicializar(event.getServer());
        GerenciadorRegioes.getInstance().setServidor(event.getServer());
        GerenciadorHologramas.getInstance().setServidor(event.getServer());
        net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods.inicializar(event.getServer());
        net.aelysium.aelysiummod.nucleo.configuracao.ConfiguracaoChat.carregar(event.getServer());
        net.aelysium.aelysiummod.sistemas.rp.deus.RegistroDeuses.carregar(event.getServer());
        net.aelysium.aelysiummod.sistemas.rp.personagem.RegistroPersonagens.carregar(event.getServer());
        net.aelysium.aelysiummod.sistemas.rp.skin.RegistroSkins.carregar(event.getServer());
        net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem.carregar(event.getServer());
        ConfiguracaoListaBanimentos.load(event.getServer());
        ConfiguracaoDiscord.load();
        ConfiguracaoModsDesativados.load();
        ConfiguracaoBanimentoReceitas.load();
        net.aelysium.aelysiummod.sistemas.mundo.renascimento.ConfiguracaoDimensaoRenascimento.load(event.getServer());
        net.aelysium.aelysiummod.sistemas.mundo.renascimento.SpawnsIndividuais.load(event.getServer());
        net.aelysium.aelysiummod.ferramentas.AmostradorMundo.iniciar(event.getServer());
        GerenciadorFiltroReceitas.setServer(event.getServer());
        GerenciadorFiltroReceitas.reapplyFilter(event.getServer());
        net.aelysium.aelysiummod.sistemas.admin.banimentos.AplicadorNerfs.aplicarConquistas(event.getServer());
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new OuvinteFiltroReceitas(
            event.getServerResources().getRecipeManager(),
            event.getRegistryAccess()
        ));
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            DadosHistorico.registrarLogin(player, DadosHistorico.obterIpJogador(player));
            RedeListaBanimentos.syncToPlayer(player);
            GerenciadorFiltroReceitas.syncToPlayer(player);
            GerenciadorRegioes.getInstance().sincronizarJogador(player);
            net.aelysium.aelysiummod.nucleo.guarda.GuardaRede.syncToPlayer(player);
            if (net.aelysium.aelysiummod.sistemas.rp.morte.ConfiguracaoMorte.isHabilitada()) {
                net.aelysium.aelysiummod.sistemas.rp.morte.UtilitariosEfeitoMorte.aplicar(player);
            } else if (player.hasEffect(net.aelysium.aelysiummod.registro.ModEfeitos.MORTE_CANONICA_EVENTO)) {
                net.aelysium.aelysiummod.sistemas.rp.morte.UtilitariosEfeitoMorte.remover(player);
            }
            net.aelysium.aelysiummod.sistemas.rp.vidas.DadosVidas.get(player.getServer()).marcarOnline(player.getUUID());
            net.aelysium.aelysiummod.sistemas.social.apelido.comando.ComandoApelido.refreshTabList(player);
            net.aelysium.aelysiummod.sistemas.mundo.lua.rede.RedeLua.sincronizarLuaParaJogador(player, net.aelysium.aelysiummod.sistemas.mundo.lua.GerenciadorLua.luaAtual);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide()) {
            DadosHistorico.registrarLogout(player);
            net.aelysium.aelysiummod.sistemas.admin.banimentos.ManipuladorListaNegraModsServidor.limpar(player.getUUID());
            net.aelysium.aelysiummod.sistemas.rp.vidas.DadosVidas.get(player.getServer()).marcarOffline(player.getUUID());
        }
        if (event.getEntity().level().isClientSide()) {
            DadosBanimentoCliente.clear();
            RegiaoCacheCliente.clear();
        }
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class EventosModComum {
        @SubscribeEvent
        public static void registerEntityAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) {
            event.put(RegistroNpc.CUSTOM_NPC.get(),
                    EntidadeNpcPersonalizado.createAttributes().build());
        }

        @SubscribeEvent
        public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
            if (!CompatibilidadeCurios.isCuriosLoaded()) return;
            event.registerItem(top.theillusivec4.curios.api.CuriosCapability.ITEM,
                    (stack, ctx) -> new net.aelysium.aelysiummod.conteudo.item.cigarro.CigarroCurio(stack),
                    ModItens.CIGARRO.get());
        }
    }
}
