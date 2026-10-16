package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AplicadorNerfs {

    private static final Logger LOGGER = LoggerFactory.getLogger("AelysiumNerfs");

    public static void aplicarConquistas(MinecraftServer server) {
        if (server == null) return;
        try {
            ServerAdvancementManager manager = server.getAdvancements();
            Field campo = ServerAdvancementManager.class.getDeclaredField("advancements");
            campo.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<ResourceLocation, AdvancementHolder> todas = (Map<ResourceLocation, AdvancementHolder>) campo.get(manager);

            Set<ResourceLocation> remover = new HashSet<>();
            ImmutableMap.Builder<ResourceLocation, AdvancementHolder> restantes = ImmutableMap.builder();
            for (Map.Entry<ResourceLocation, AdvancementHolder> e : todas.entrySet()) {
                if (ConfiguracaoModsDesativados.isConquistasCapped(e.getKey().getNamespace())) {
                    remover.add(e.getKey());
                } else {
                    restantes.put(e.getKey(), e.getValue());
                }
            }
            if (remover.isEmpty()) return;

            manager.tree().remove(remover);
            campo.set(manager, restantes.build());
            LOGGER.info("{} conquistas de mods nerfados removidas.", remover.size());
        } catch (Exception e) {
            LOGGER.error("Falha ao remover conquistas de mods nerfados: ", e);
        }
    }
}
