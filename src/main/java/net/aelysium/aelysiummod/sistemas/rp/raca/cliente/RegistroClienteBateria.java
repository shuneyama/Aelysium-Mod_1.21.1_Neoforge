package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModItens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteBateria {

    @SubscribeEvent
    public static void registrarExtensoesCliente(RegisterClientExtensionsEvent event) {
        event.registerItem(new BateriaExtensoes(), ModItens.BATERIA.get());
    }
}
