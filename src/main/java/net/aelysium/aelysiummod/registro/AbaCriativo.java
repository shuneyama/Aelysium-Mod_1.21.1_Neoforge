package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

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

                        output.accept(ModItens.AEDA_COBRE);
                        output.accept(ModItens.AEDA_PRATA);
                        output.accept(ModItens.AEDA_OURO);
                        output.accept(ModItens.AEDA_RUBI);
                        output.accept(ModItens.AEDA_CIANE);
                        output.accept(ModItens.AEDA_AMETISTA);
                        output.accept(ModItens.VINEDA);
                        ModItens.AEDAS_CASSINO.forEach(output::accept);

                        output.accept(ModItens.CHAVERACHA1);
                        output.accept(ModItens.CHAVERACHA2);
                        output.accept(ModItens.CHAVERACHA3);
                        output.accept(ModItens.CHAVERACHA4);
                        output.accept(ModItens.CHAVERACHA5);
                        output.accept(ModItens.CHAVERACHA6);
                        output.accept(ModItens.CHAVERACHA7);
                        output.accept(ModItens.CHAVERACHA8);
                        output.accept(ModItens.CHAVE_QUE_NAO_VAI);

                        output.accept(ModItens.ELEMENTO_FOGO);
                        output.accept(ModItens.ELEMENTO_GELO);
                        output.accept(ModItens.ELEMENTO_NATUREZA);
                        output.accept(ModItens.ELEMENTO_RAIO);
                        output.accept(ModItens.ELEMENTO_SACRA);
                        output.accept(ModItens.ELEMENTO_SANGUE);
                        output.accept(ModItens.ELEMENTO_VAZIO);

                        output.accept(ModItens.CIANE_BARRA);
                        output.accept(ModItens.CIANE_CRU);
                        output.accept(ModItens.CIANE_FRAGMENTO);

                        output.accept(ModItens.POEIRA_LUNAR);

                        output.accept(ModItens.CRIADOR_NPC);
                        output.accept(ModItens.REGISTRO_NPC);
                        output.accept(ModItens.VARINHA_PROTECAO);
                        output.accept(ModItens.CARTEIRA);
                        output.accept(ModItens.CIGARRO);
                        output.accept(ModItens.ISQUEIRO);
                        output.accept(ModItens.BATERIA);
                        output.accept(ModItens.BALA_GLOCK);
                        output.accept(ModItens.CARTAO_CIANO);
                        output.accept(ModItens.CARTAO_ROXO);
                        output.accept(ModItens.CARTAO_VERDE);
                        output.accept(ModItens.CARTAO_VERMELHO);
                        output.accept(ModItens.CARTAO_MESTRE);
                    }).build());



    public static final Supplier<CreativeModeTab> AELYSIUM_BLOCOS_TAB = CREATIVE_MODE_TAB.register("aelysium_blocos_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocos.DAMA_DA_NOITE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "aelysium_armas_tab"))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_blocos"))
                    .displayItems((itemDisplayParameters, output) -> {
                        if (!net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda.isAutorizadoAgora()) return;

                        output.accept(ModBlocos.BLOCO_AR);
                        ModBlocos.MANCHAS_SANGUE.forEach(output::accept);
                        ModBlocos.MANCHAS_SANGUE_GRANDES.forEach(output::accept);
                        output.accept(ModBlocos.CIANE_MINERIO);
                        output.accept(ModBlocos.DAMA_DA_NOITE);
                        output.accept(ModBlocos.FENDA_KAIROS);
                        output.accept(ModBlocos.PORTA_UM_PAINEL);
                        output.accept(ModBlocos.COFRE_COM_PAINEL);
                    }).build());



    public static final Supplier<CreativeModeTab> AELYSIUM_ARMAS_TAB = CREATIVE_MODE_TAB.register("aelysium_armas_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItens.CANO.get()))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_armas"))
                    .displayItems((itemDisplayParameters, output) -> {
                        if (!net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda.isAutorizadoAgora()) return;

                        output.accept(ModItens.CIANE_CAPACETE);
                        output.accept(ModItens.CIANE_PEITORAL);
                        output.accept(ModItens.CIANE_CALCINHA);
                        output.accept(ModItens.CIANE_BOTAS);

                        output.accept(ModItens.CIANE_ESPADA);
                        output.accept(ModItens.CIANE_PICARETA);
                        output.accept(ModItens.CIANE_MACHADO);
                        output.accept(ModItens.CIANE_PA);
                        output.accept(ModItens.CIANE_ENXADA);

                        output.accept(ModItens.CANO);
                        output.accept(ModItens.PARAR);
                        output.accept(ModItens.ESPADA_ADM);
                        output.accept(ModItens.SLYNDOR_SYLVAN);
                        output.accept(ModItens.AMBROSE);
                        output.accept(ModItens.PURPLE_MOON);
                        output.accept(ModItens.GLOCK);
                        output.accept(ModItens.EXPERIMENTO_646);
                        output.accept(ModItens.PROTOCOLO_GENESIS);
                        output.accept(ModItens.ESPADA_ESPECIAL);

                        output.accept(ModItens.DARK_JUDGMENT);
                        output.accept(ModItens.DIVINE_SILENCE);
                        output.accept(ModItens.FULMENS_FANG);
                        output.accept(ModItens.ALVORADA_CELESTE);
                        output.accept(ModItens.AURORA_HIBERNA);
                        output.accept(ModItens.LUZ_DA_ULTIMA_FENIX);
                        output.accept(ModItens.AEON);
                        output.accept(ModItens.KLAUS_ENTROPY);

                        output.accept(ModItens.TRIDENTE_CORAL);
                        output.accept(ModItens.TRIFORME);
                        output.accept(ModItens.ULTIMO_SUSPIRO);
                        output.accept(ModItens.ESCUDO_ABISMO);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}