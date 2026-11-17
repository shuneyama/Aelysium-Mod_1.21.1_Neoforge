package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.arma.ArmaAeon;
import net.aelysium.aelysiummod.conteudo.item.arma.ArmaKlaus;
import net.aelysium.aelysiummod.sistemas.rp.raca.item.Bateria;
import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraItem;
import net.aelysium.aelysiummod.conteudo.item.cigarro.CigarroItem;
import net.aelysium.aelysiummod.sistemas.admin.vip.item.SacolaVipItem;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorNPC;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorRegistro;
import net.aelysium.aelysiummod.conteudo.item.escudo.EscudoAbismoItem;
import net.aelysium.aelysiummod.conteudo.item.glock.GlockItem;
import net.aelysium.aelysiummod.conteudo.item.lanterna.LanternaItem;
import net.aelysium.aelysiummod.conteudo.item.medico.MedkitItem;
import net.aelysium.aelysiummod.conteudo.item.medico.SeringaCuraItem;
import net.aelysium.aelysiummod.conteudo.item.medico.SeringaItem;
import net.aelysium.aelysiummod.conteudo.item.cigarro.IsqueiroItem;
import net.aelysium.aelysiummod.conteudo.item.arma.ProtocoloGenesisItem;
import net.aelysium.aelysiummod.conteudo.item.arma.TridenteConchasItem;
import net.aelysium.aelysiummod.conteudo.item.arma.TriformeItem;
import net.aelysium.aelysiummod.conteudo.item.arma.UltimoSuspiroItem;
import net.aelysium.aelysiummod.sistemas.admin.protecao.item.VarinhaProtecao;
import net.aelysium.aelysiummod.registro.ModMateriaisArmaduras;
import net.aelysium.aelysiummod.registro.ModNiveisFerramenta;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItens {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AelysiumMod.MOD_ID);




    public static final DeferredItem<SwordItem> EXPERIMENTO_646 = ITEMS.register("experimento_646",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 14, -2.0f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.experimento_646.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.experimento_646"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> PROTOCOLO_GENESIS = ITEMS.register("protocolo_genesis",
            () -> new ProtocoloGenesisItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 14, -2.0f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.protocolo_genesis.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.protocolo_genesis"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });


    public static final DeferredItem<SwordItem> SLYNDOR_SYLVAN = ITEMS.register("slyndor_sylvan",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 12, -2.2f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.aelysiummod.slyndor_sylvan"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> AMBROSE = ITEMS.register("ambrose",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 12, -2.2f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    tooltipComponents.add(Component.translatable("tooltip.aelysiummod.ambrose"));
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> PURPLE_MOON = ITEMS.register("purple_moon",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 12, -2.2f))));

    public static final DeferredItem<SwordItem> KLAUS_ENTROPY = ITEMS.register("klaus_entropy",
            () -> new ArmaKlaus(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))));
    public static final DeferredItem<SwordItem> AURORA_HIBERNA = ITEMS.register("aurora_hiberna",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.aurora_hiberna.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.aurora_hiberna"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> LUZ_DA_ULTIMA_FENIX = ITEMS.register("luz_da_ultima_fenix",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.luz_da_ultima_fenix.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.luz_da_ultima_fenix"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> FULMENS_FANG = ITEMS.register("fulmens_fang",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.fulmens_fang.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.fulmens_fang"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> DARK_JUDGMENT = ITEMS.register("dark_judgment",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.dark_judgment.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.dark_judgment"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> DIVINE_SILENCE = ITEMS.register("divine_silence",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.divine_silence.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.divine_silence"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> ALVORADA_CELESTE = ITEMS.register("alvorada_celeste",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.alvorada_celeste.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.alvorada_celeste"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> AEON = ITEMS.register("aeon",
            () -> new ArmaAeon(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 96, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.aeon.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.aeon"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });


    public static final DeferredItem<Item> AEDA_COBRE = ITEMS.register("aeda_cobre",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> AEDA_PRATA = ITEMS.register("aeda_prata",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> AEDA_OURO = ITEMS.register("aeda_ouro",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> AEDA_AMETISTA = ITEMS.register("aeda_ametista",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> AEDA_RUBI = ITEMS.register("aeda_rubi",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> AEDA_CIANE = ITEMS.register("aeda_ciane",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> VINEDA = ITEMS.register("vineda",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final java.util.List<DeferredItem<Item>> AEDAS_CASSINO = registrarAedasCassino(
            "cobre", "prata", "ouro", "rubi", "ciane", "ametista");

    private static java.util.List<DeferredItem<Item>> registrarAedasCassino(String... metais) {
        java.util.List<DeferredItem<Item>> lista = new java.util.ArrayList<>();
        for (String metal : metais) {
            lista.add(ITEMS.register("aeda_cassino_" + metal,
                    () -> new net.aelysium.aelysiummod.conteudo.item.aeda.AedaCassinoItem(new Item.Properties().stacksTo(16))));
        }
        return lista;
    }


    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA1 = ITEMS.register("chave_ciano_dourado",
        () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA2 = ITEMS.register("chave_ciano_escuro",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA3 = ITEMS.register("chave_roxo_dourado",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA4 = ITEMS.register("chave_roxo_escuro",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA5 = ITEMS.register("chave_verde_dourado",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA6 = ITEMS.register("chave_verde_escuro",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA7 = ITEMS.register("chave_vermelho_dourado",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVERACHA8 = ITEMS.register("chave_vermelho_escuro",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem> CHAVE_QUE_NAO_VAI = ITEMS.register("chave_que_nao_vai",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.ChaveItem(new Item.Properties().stacksTo(1)));


    public static final DeferredItem<Item> ELEMENTO_FOGO = ITEMS.register("elemento_fogo",
           () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ELEMENTO_GELO = ITEMS.register("elemento_gelo",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ELEMENTO_NATUREZA = ITEMS.register("elemento_natureza",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ELEMENTO_RAIO = ITEMS.register("elemento_raio",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ELEMENTO_SACRA = ITEMS.register("elemento_sacra",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ELEMENTO_SANGUE = ITEMS.register("elemento_sangue",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ELEMENTO_VAZIO = ITEMS.register("elemento_vazio",
            () -> new Item(new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> CIANE_CRU = ITEMS.register("ciane_cru",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CIANE_FRAGMENTO = ITEMS.register("ciane_fragmento",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CIANE_BARRA = ITEMS.register("ciane_barra",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<ArmorItem> CIANE_CAPACETE = ITEMS.register("ciane_capacete",
            () -> new ArmorItem(ModMateriaisArmaduras.CIANE_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties()
                    .stacksTo(1).fireResistant().durability(ArmorItem.Type.HELMET.getDurability(50))));
    public static final DeferredItem<ArmorItem> CIANE_PEITORAL = ITEMS.register("ciane_peitoral",
            () -> new ArmorItem(ModMateriaisArmaduras.CIANE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties()
                    .stacksTo(1).fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(40))));
    public static final DeferredItem<ArmorItem> CIANE_CALCINHA = ITEMS.register("ciane_calcinha",
            () -> new ArmorItem(ModMateriaisArmaduras.CIANE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties()
                    .stacksTo(1).fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(40))));
    public static final DeferredItem<ArmorItem> CIANE_BOTAS = ITEMS.register("ciane_botas",
            () -> new ArmorItem(ModMateriaisArmaduras.CIANE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties()
                    .stacksTo(1).fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(40))));

    public static final DeferredItem<PickaxeItem> CIANE_PICARETA = ITEMS.register("ciane_picareta",
            () -> new PickaxeItem(ModNiveisFerramenta.CIANE, new Item.Properties()
                    .stacksTo(1).attributes(PickaxeItem.createAttributes(ModNiveisFerramenta.CIANE, 5, -3f))));
    public static final DeferredItem<ShovelItem> CIANE_PA = ITEMS.register("ciane_pa",
            () -> new ShovelItem(ModNiveisFerramenta.CIANE, new Item.Properties()
                    .stacksTo(1).attributes(ShovelItem.createAttributes(ModNiveisFerramenta.CIANE, 5.5F, -3f))));
    public static final DeferredItem<HoeItem> CIANE_ENXADA = ITEMS.register("ciane_enxada",
            () -> new HoeItem(ModNiveisFerramenta.CIANE, new Item.Properties()
                    .stacksTo(1).attributes(HoeItem.createAttributes(ModNiveisFerramenta.CIANE, 0, -3f))));
    public static final DeferredItem<AxeItem> CIANE_MACHADO = ITEMS.register("ciane_machado",
            () -> new AxeItem(ModNiveisFerramenta.CIANE, new Item.Properties()
                    .stacksTo(1).attributes(AxeItem.createAttributes(ModNiveisFerramenta.CIANE, 9, -3f))));

    public static final DeferredItem<SwordItem> CIANE_ESPADA = ITEMS.register("ciane_espada",
            () -> new SwordItem(ModNiveisFerramenta.CIANE, new Item.Properties()
                    .stacksTo(1).attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 10, -2.2f))));

    public static final DeferredItem<SwordItem> ESPADA_ADM = ITEMS.register("espada_adm",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 124, -1f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.espada_adm.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.espada_adm"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> ESPADA_ESPECIAL = ITEMS.register("espada_especial",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 6, -2.2f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.espada_especial.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.espada_especial"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> CANO = ITEMS.register("cano",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1).durability(0)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 124, -2f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.cano.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.cano"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<SwordItem> PARAR = ITEMS.register("parar",
            () -> new SwordItem(ModNiveisFerramenta.ADM, new Item.Properties().stacksTo(1)
                    .attributes(SwordItem.createAttributes(ModNiveisFerramenta.ADM, 124, -2f))) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (Screen.hasShiftDown()) {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.parar.shift_down"));
                    } else {
                        tooltipComponents.add(Component.translatable("tooltip.aelysiummod.parar"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });


    public static final DeferredItem<Item> POEIRA_LUNAR = ITEMS.register("poeira_lunar",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BATERIA = ITEMS.register("bateria",
            () -> new Bateria(new Item.Properties()));
    public static final DeferredItem<Item> VARINHA_PROTECAO = ITEMS.register("varinha_protecao",
            () -> new VarinhaProtecao(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CriadorNPC> CRIADOR_NPC = ITEMS.register("criador_npc",
            () -> new CriadorNPC(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CriadorRegistro> REGISTRO_NPC = ITEMS.register("registro_npc",
            () -> new CriadorRegistro(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CarteiraItem> CARTEIRA = ITEMS.register("carteira",
            () -> new CarteiraItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<SacolaVipItem> SACOLA_VIP = ITEMS.register("sacola_vip",
            () -> new SacolaVipItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CigarroItem> CIGARRO = ITEMS.register("cigarro",
            () -> new CigarroItem(new Item.Properties().stacksTo(1)
                    .component(ModComponentesDados.CIGARRO_ACESO.get(), false)
                    .component(ModComponentesDados.CIGARRO_FUMADAS.get(), 0)));
    public static final DeferredItem<IsqueiroItem> ISQUEIRO = ITEMS.register("isqueiro",
            () -> new IsqueiroItem(new Item.Properties().stacksTo(1)
                    .component(ModComponentesDados.ISQUEIRO_ACESO.get(), false)
                    .component(ModComponentesDados.ISQUEIRO_TAMPA_ABERTA.get(), false)));

    public static final DeferredItem<GlockItem> GLOCK = ITEMS.register("glock",
            () -> new GlockItem(new Item.Properties().stacksTo(1)
                    .component(ModComponentesDados.GLOCK_MUNICAO.get(), 0)));
    public static final DeferredItem<Item> BALA_GLOCK = ITEMS.register("bala_glock",
            () -> new Item(new Item.Properties().stacksTo(15)));

    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem> CARTAO_CIANO = ITEMS.register("cartao_ciano",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem> CARTAO_ROXO = ITEMS.register("cartao_roxo",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem> CARTAO_VERDE = ITEMS.register("cartao_verde",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem> CARTAO_VERMELHO = ITEMS.register("cartao_vermelho",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoMestreItem> CARTAO_MESTRE = ITEMS.register("cartao_mestre",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoMestreItem(new Item.Properties().stacksTo(1)));


    public static final DeferredItem<TridenteConchasItem> TRIDENTE_CORAL = ITEMS.register("tridente_coral",
            () -> new TridenteConchasItem(new Item.Properties().stacksTo(1).fireResistant()
                    .durability(Tiers.NETHERITE.getUses())
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4f))
                    .component(net.minecraft.core.component.DataComponents.TOOL, TridentItem.createToolProperties())));

    public static final DeferredItem<SwordItem> TRIFORME = ITEMS.register("triforme",
            () -> new TriformeItem(Tiers.NETHERITE, new Item.Properties().stacksTo(1).fireResistant()
                    .attributes(TriformeItem.atributos(0))));

    public static final DeferredItem<SwordItem> ULTIMO_SUSPIRO = ITEMS.register("ultimo_suspiro",
            () -> new UltimoSuspiroItem(Tiers.NETHERITE, new Item.Properties().stacksTo(1).fireResistant()
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4f))));

    public static final DeferredItem<EscudoAbismoItem> ESCUDO_ABISMO = ITEMS.register("escudo_abismo",
            () -> new EscudoAbismoItem(new Item.Properties().stacksTo(1).fireResistant().durability(Tiers.NETHERITE.getUses())));

    public static final DeferredItem<LanternaItem> LANTERNA = ITEMS.register("lanterna",
            () -> new LanternaItem(new Item.Properties().stacksTo(1)
                    .component(ModComponentesDados.LANTERNA_LIGADA.get(), false)));
    public static final DeferredItem<MedkitItem> MEDKIT = ITEMS.register("medkit",
            () -> new MedkitItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<SeringaItem> SERINGA = ITEMS.register("seringa",
            () -> new SeringaItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<SeringaCuraItem> SERINGA_CURA = ITEMS.register("seringa_cura",
            () -> new SeringaCuraItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> SERINGA_SANGUE = ITEMS.register("seringa_sangue",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<net.aelysium.aelysiummod.sistemas.mundo.porta.item.GazuaItem> GAZUA = ITEMS.register("gazua",
            () -> new net.aelysium.aelysiummod.sistemas.mundo.porta.item.GazuaItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BannerPatternItem> FIELD_MASONED_BANNER_PATTERN = ITEMS.register("field_masoned_banner_pattern",
            () -> new BannerPatternItem(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BANNER_PATTERN,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "pattern_item/field_masoned")),
                    new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BannerPatternItem> BORDURE_INDENTED_BANNER_PATTERN = ITEMS.register("bordure_indented_banner_pattern",
            () -> new BannerPatternItem(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BANNER_PATTERN,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "pattern_item/bordure_indented")),
                    new Item.Properties().stacksTo(1)));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}