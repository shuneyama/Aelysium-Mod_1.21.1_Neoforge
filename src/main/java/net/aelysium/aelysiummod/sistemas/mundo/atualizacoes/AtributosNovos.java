package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AtributosNovos {

    public static final DeferredRegister<Attribute> ATRIBUTOS = DeferredRegister.create(Registries.ATTRIBUTE, AelysiumMod.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> DISTANCIA_CAMERA = ATRIBUTOS.register("camera_distance",
            () -> new RangedAttribute("attribute.name.camera_distance", 4.0D, 0.0D, 128.0D).setSyncable(true));

    private AtributosNovos() {
    }

    public static void registrar(IEventBus barramento) {
        ATRIBUTOS.register(barramento);
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class Modificacao {
        @SubscribeEvent
        public static void adicionar(EntityAttributeModificationEvent evento) {
            Holder<Attribute> atributo = DISTANCIA_CAMERA;
            evento.getTypes().forEach(tipo -> {
                if (!evento.has(tipo, atributo)) evento.add(tipo, atributo);
            });
        }
    }
}
