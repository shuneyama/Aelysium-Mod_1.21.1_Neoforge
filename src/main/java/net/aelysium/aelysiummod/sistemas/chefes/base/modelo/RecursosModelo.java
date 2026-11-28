package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao.AnimacaoBedrock;
import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.animacao.CarregadorAnimacoes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class RecursosModelo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Optional<ModeloBedrock>> MODELOS = new HashMap<>();
    private static final Map<ResourceLocation, Map<String, AnimacaoBedrock>> ANIMACOES = new HashMap<>();

    private RecursosModelo() {
    }

    public static ModeloBedrock modelo(ResourceLocation local) {
        return MODELOS.computeIfAbsent(local, l -> Optional.ofNullable(ler(l, CarregadorModelo::carregar))).orElse(null);
    }

    public static Map<String, AnimacaoBedrock> animacoes(ResourceLocation local) {
        return ANIMACOES.computeIfAbsent(local, l -> {
            Map<String, AnimacaoBedrock> lidas = ler(l, CarregadorAnimacoes::carregar);
            return lidas == null ? Map.of() : lidas;
        });
    }

    private static <T> T ler(ResourceLocation local, Function<JsonObject, T> conversor) {
        try (Reader leitor = Minecraft.getInstance().getResourceManager().openAsReader(local)) {
            return conversor.apply(JsonParser.parseReader(leitor).getAsJsonObject());
        } catch (Exception e) {
            LOGGER.error("Falha ao carregar {}", local, e);
            return null;
        }
    }

    @SubscribeEvent
    public static void registrarRecarga(RegisterClientReloadListenersEvent evento) {
        evento.registerReloadListener((ResourceManagerReloadListener) gerenciador -> {
            MODELOS.clear();
            ANIMACOES.clear();
        });
    }
}
