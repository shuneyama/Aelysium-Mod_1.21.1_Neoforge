package net.aelysium.aelysiummod.sistemas.mundo.minifarol.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModEntidadesBloco;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class RegistroClienteMiniFarol {

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModEntidadesBloco.MINI_FAROL.get(), contexto -> new RenderizadorMiniFarol());
    }
}
