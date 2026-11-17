package net.aelysium.aelysiummod.sistemas.mundo.mochila.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.DicaMochila;
import net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroModelosMochila {

    private static List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        nomes.add("mochila");
        ModMochilas.COLORIDAS.keySet().forEach(cor -> nomes.add("mochila_" + cor.getName()));
        return nomes;
    }

    private static ResourceLocation id(String caminho) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, caminho);
    }

    @SubscribeEvent
    public static void tooltips(RegisterClientTooltipComponentFactoriesEvent evento) {
        evento.register(DicaMochila.class, ClienteDicaMochila::new);
    }

    @SubscribeEvent
    public static void adicionais(ModelEvent.RegisterAdditional evento) {
        for (String nome : nomes()) {
            evento.register(ModelResourceLocation.standalone(id("item/" + nome + "_open_back")));
            evento.register(ModelResourceLocation.standalone(id("item/" + nome + "_open_front")));
        }
    }

    @SubscribeEvent
    public static void modificar(ModelEvent.ModifyBakingResult evento) {
        for (String nome : nomes()) {
            ModelResourceLocation local = ModelResourceLocation.inventory(id(nome));
            BakedModel base = evento.getModels().get(local);
            if (base == null) continue;
            evento.getModels().put(local, new ModeloMochila(base,
                    ModelResourceLocation.standalone(id("item/" + nome + "_open_back")),
                    ModelResourceLocation.standalone(id("item/" + nome + "_open_front"))));
        }
    }
}
