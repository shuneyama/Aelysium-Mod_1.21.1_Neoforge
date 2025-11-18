package net.aelysium.aelysiummod;

import net.aelysium.aelysiummod.block.ModBlocks;
import net.aelysium.aelysiummod.item.ModItens;
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

//itens !! =============================================================================================================
    public static final Supplier<CreativeModeTab> AELYSIUM_ITENS_TAB = CREATIVE_MODE_TAB.register("aelysium_itens_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItens.AEDA_CIANE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "aelysium_blocos_tab"))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_itens"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModItens.AEDA_COBRE);
                        output.accept(ModItens.AEDA_PRATA);
                        output.accept(ModItens.AEDA_OURO);
                        output.accept(ModItens.AEDA_RUBI);
                        output.accept(ModItens.AEDA_CIANE);
                        output.accept(ModItens.AEDA_AMETISTA);

                        output.accept(ModItens.CHAVERACHA1);
                        output.accept(ModItens.CHAVERACHA2);
                        output.accept(ModItens.CHAVERACHA3);
                        output.accept(ModItens.CHAVERACHA4);
                        output.accept(ModItens.CHAVERACHA5);
                        output.accept(ModItens.CHAVERACHA6);
                        output.accept(ModItens.CHAVERACHA7);
                        output.accept(ModItens.CHAVERACHA8);

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
                    }).build());


//blocos !! ============================================================================================================
    public static final Supplier<CreativeModeTab> AELYSIUM_BLOCOS_TAB = CREATIVE_MODE_TAB.register("aelysium_blocos_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DAMA_DA_NOITE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "aelysium_armas_tab"))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_blocos"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.BLOCO_AR);
                        output.accept(ModBlocks.CIANE_MINERIO);
                        output.accept(ModBlocks.DAMA_DA_NOITE);
                        output.accept(ModBlocks.DAMA_VERMELHA_DA_NOITE);
                    }).build());


//armas !! =============================================================================================================
    public static final Supplier<CreativeModeTab> AELYSIUM_ARMAS_TAB = CREATIVE_MODE_TAB.register("aelysium_armas_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItens.CANO.get()))
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_armas"))
                    .displayItems((itemDisplayParameters, output) -> {

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
                        output.accept(ModItens.CRIMSON_WHISPER);
                        output.accept(ModItens.DARK_JUDGMENT);
                        output.accept(ModItens.DIVINE_SILENCE);
                        output.accept(ModItens.ESPADA_ADM);
                        output.accept(ModItens.ESPADA_ESPECIAL);
                        output.accept(ModItens.EXPERIMENTO_646);
                        output.accept(ModItens.FULMENS_FANG);
                        output.accept(ModItens.ALVORADA_CELESTE);
                        output.accept(ModItens.AURORA_HIBERNA);
                        output.accept(ModItens.LUZ_DA_ULTIMA_FENIX);
                        output.accept(ModItens.PROTOCOLO_GENESIS);
                        output.accept(ModItens.THUNDERLASH);
                        output.accept(ModItens.BLOOD_REGRET);
                        output.accept(ModItens.AEON);
                        output.accept(ModItens.KLAUS_SWORD);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}