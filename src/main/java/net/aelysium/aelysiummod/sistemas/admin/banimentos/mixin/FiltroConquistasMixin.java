package net.aelysium.aelysiummod.sistemas.admin.banimentos.mixin;

import com.google.gson.JsonElement;
import net.aelysium.aelysiummod.nucleo.armazenamento.PastaAelysium;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class FiltroConquistasMixin {

    @ModifyVariable(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"), argsOnly = true)
    private Map<ResourceLocation, JsonElement> aelysium$filtrarConquistasDeModNerfado(Map<ResourceLocation, JsonElement> conquistas) {
        if (!PastaAelysium.pronta()) return conquistas;
        Map<ResourceLocation, JsonElement> restantes = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entrada : conquistas.entrySet()) {
            if (!ConfiguracaoModsDesativados.isConquistasCapped(entrada.getKey().getNamespace())) {
                restantes.put(entrada.getKey(), entrada.getValue());
            }
        }
        return restantes;
    }
}
