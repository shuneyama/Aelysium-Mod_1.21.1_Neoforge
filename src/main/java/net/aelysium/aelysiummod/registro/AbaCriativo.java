package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AbaCriativo {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AelysiumMod.MOD_ID);


    public static final Supplier<CreativeModeTab> AELYSIUM_ITENS_TAB = CREATIVE_MODE_TAB.register("aelysium_itens_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItens.AEDA_CIANE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "aelysium_blocos_tab"))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_itens"))
                    .displayItems((itemDisplayParameters, output) -> {
                        if (!net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda.isAutorizadoAgora()) return;
                        conteudoItens().forEach(output::accept);
                    }).build());



    public static final Supplier<CreativeModeTab> AELYSIUM_BLOCOS_TAB = CREATIVE_MODE_TAB.register("aelysium_blocos_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocos.DAMA_DA_NOITE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "aelysium_armas_tab"))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_blocos"))
                    .displayItems((itemDisplayParameters, output) -> {
                        if (!net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda.isAutorizadoAgora()) return;
                        conteudoBlocos().forEach(output::accept);
                    }).build());



    public static final Supplier<CreativeModeTab> AELYSIUM_DECORACAO_TAB = CREATIVE_MODE_TAB.register("aelysium_decoracao_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocosDecoracao.ITENS.getEntries().iterator().next().get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "aelysium_blocos_tab"))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_decoracao"))
                    .displayItems((itemDisplayParameters, output) -> ModBlocosDecoracao.ITENS.getEntries().forEach(item -> output.accept(item.get()))).build());



    public static final Supplier<CreativeModeTab> AELYSIUM_ARMAS_TAB = CREATIVE_MODE_TAB.register("aelysium_armas_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItens.CANO.get()))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_armas"))
                    .displayItems((itemDisplayParameters, output) -> {
                        if (!net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda.isAutorizadoAgora()) return;
                        conteudoArmas().forEach(output::accept);
                    }).build());


    public static List<ItemLike> conteudoItens() {
        List<ItemLike> conteudo = new ArrayList<>();
        conteudo.add(ModItens.AEDA_COBRE);
        conteudo.add(ModItens.AEDA_PRATA);
        conteudo.add(ModItens.AEDA_OURO);
        conteudo.add(ModItens.AEDA_RUBI);
        conteudo.add(ModItens.AEDA_CIANE);
        conteudo.add(ModItens.AEDA_AMETISTA);
        conteudo.add(ModItens.VINEDA);
        conteudo.addAll(ModItens.AEDAS_CASSINO);

        conteudo.add(ModItens.CHAVERACHA1);
        conteudo.add(ModItens.CHAVERACHA2);
        conteudo.add(ModItens.CHAVERACHA3);
        conteudo.add(ModItens.CHAVERACHA4);
        conteudo.add(ModItens.CHAVERACHA5);
        conteudo.add(ModItens.CHAVERACHA6);
        conteudo.add(ModItens.CHAVERACHA7);
        conteudo.add(ModItens.CHAVERACHA8);
        conteudo.add(ModItens.CHAVE_QUE_NAO_VAI);

        conteudo.add(ModItens.ELEMENTO_FOGO);
        conteudo.add(ModItens.ELEMENTO_GELO);
        conteudo.add(ModItens.ELEMENTO_NATUREZA);
        conteudo.add(ModItens.ELEMENTO_RAIO);
        conteudo.add(ModItens.ELEMENTO_SACRA);
        conteudo.add(ModItens.ELEMENTO_SANGUE);
        conteudo.add(ModItens.ELEMENTO_VAZIO);

        conteudo.add(ModItens.CIANE_BARRA);
        conteudo.add(ModItens.CIANE_CRU);
        conteudo.add(ModItens.CIANE_FRAGMENTO);

        conteudo.add(ModItens.POEIRA_LUNAR);

        conteudo.add(ModItens.CRIADOR_NPC);
        conteudo.add(ModItens.REGISTRO_NPC);
        conteudo.add(ModItens.VARINHA_PROTECAO);
        conteudo.add(ModItens.CARTEIRA);
        conteudo.add(ModItens.CIGARRO);
        conteudo.add(ModItens.ISQUEIRO);
        conteudo.add(ModItens.BATERIA);
        conteudo.add(ModItens.BALA_GLOCK);
        conteudo.add(ModItens.CARTAO_CIANO);
        conteudo.add(ModItens.CARTAO_ROXO);
        conteudo.add(ModItens.CARTAO_VERDE);
        conteudo.add(ModItens.CARTAO_VERMELHO);
        conteudo.add(ModItens.CARTAO_MESTRE);
        conteudo.add(ModItens.GAZUA);
        conteudo.add(ModItens.LANTERNA);
        conteudo.add(ModItens.MEDKIT);
        conteudo.add(ModItens.SERINGA);
        conteudo.add(ModItens.SERINGA_CURA);
        conteudo.add(ModItens.SERINGA_SANGUE);
        return conteudo;
    }

    public static List<ItemLike> conteudoBlocos() {
        List<ItemLike> conteudo = new ArrayList<>();
        conteudo.add(ModBlocos.BLOCO_AR);
        conteudo.addAll(ModBlocos.MANCHAS_SANGUE);
        conteudo.addAll(ModBlocos.MANCHAS_SANGUE_GRANDES);
        conteudo.add(ModBlocos.CIANE_MINERIO);
        conteudo.add(ModBlocos.DAMA_DA_NOITE);
        conteudo.add(ModBlocos.FENDA_KAIROS);
        conteudo.add(ModBlocos.PORTA_UM_PAINEL);
        conteudo.add(ModBlocos.COFRE_COM_PAINEL);
        conteudo.add(ModBlocos.MINI_FAROL);
        return conteudo;
    }

    public static List<ItemLike> conteudoArmas() {
        return List.of(
                ModItens.CIANE_CAPACETE,
                ModItens.CIANE_PEITORAL,
                ModItens.CIANE_CALCINHA,
                ModItens.CIANE_BOTAS,
                ModItens.CUECA_PREFEITO,

                ModItens.CIANE_ESPADA,
                ModItens.CIANE_PICARETA,
                ModItens.CIANE_MACHADO,
                ModItens.CIANE_PA,
                ModItens.CIANE_ENXADA,

                ModItens.CANO,
                ModItens.PARAR,
                ModItens.ESPADA_ADM,
                ModItens.SLYNDOR_SYLVAN,
                ModItens.AMBROSE,
                ModItens.PURPLE_MOON,
                ModItens.GLOCK,
                ModItens.EXPERIMENTO_646,
                ModItens.PROTOCOLO_GENESIS,
                ModItens.ESPADA_ESPECIAL,

                ModItens.DARK_JUDGMENT,
                ModItens.DIVINE_SILENCE,
                ModItens.FULMENS_FANG,
                ModItens.ALVORADA_CELESTE,
                ModItens.AURORA_HIBERNA,
                ModItens.LUZ_DA_ULTIMA_FENIX,
                ModItens.AEON,
                ModItens.KLAUS_ENTROPY,

                ModItens.TRIDENTE_CORAL,
                ModItens.TRIFORME,
                ModItens.ULTIMO_SUSPIRO,
                ModItens.ESCUDO_ABISMO
        );
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}