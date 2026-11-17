package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EventosOvosSpawn {

    private EventosOvosSpawn() {
    }

    @SubscribeEvent
    public static void registrarCores(RegisterColorHandlersEvent.Item evento) {
        List<Item> ovos = new ArrayList<>();
        for (SpawnEggItem ovo : SpawnEggItem.eggs()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(ovo);
            if ("minecraft".equals(id.getNamespace())) ovos.add(ovo);
        }
        evento.register((pilha, indice) -> 0xFFFFFFFF, ovos.toArray(new Item[0]));
    }
}
