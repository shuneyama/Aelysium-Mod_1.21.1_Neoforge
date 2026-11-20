package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class EventosCavaloZumbi {

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void camadas(EntityRenderersEvent.AddLayers evento) {
        if (evento.getRenderer(EntityType.ZOMBIE_HORSE) instanceof LivingEntityRenderer<?, ?> renderizador) {
            ((LivingEntityRenderer<AbstractHorse, HorseModel<AbstractHorse>>) renderizador)
                    .addLayer(new CamadaArmaduraCavaloZumbi((LivingEntityRenderer<AbstractHorse, HorseModel<AbstractHorse>>) renderizador, evento.getEntityModels()));
        }
    }
}
