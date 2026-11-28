package net.aelysium.aelysiummod.sistemas.mundo.lithynor;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.Set;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class EventosLithynor {

    private static final Set<MobSpawnType> SPAWNS_NATURAIS = Set.of(
            MobSpawnType.NATURAL,
            MobSpawnType.CHUNK_GENERATION,
            MobSpawnType.STRUCTURE,
            MobSpawnType.PATROL
    );

    private EventosLithynor() {
    }

    @SubscribeEvent
    public static void aoFinalizarSpawn(FinalizeSpawnEvent evento) {
        if (!SPAWNS_NATURAIS.contains(evento.getSpawnType())) return;
        if (!RestricoesLithynor.ehLithynor(evento.getLevel().getLevel())) return;
        String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(evento.getEntity().getType()).getNamespace();
        if (RestricoesLithynor.modExcluido(namespace)) evento.setSpawnCancelled(true);
    }
}
