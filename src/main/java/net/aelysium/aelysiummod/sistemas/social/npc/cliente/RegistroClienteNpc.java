package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.RegistroNpc;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.aelysium.aelysiummod.registro.ModMenus;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteNpc {

    @SubscribeEvent
    public static void registrarTelas(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenus.MERCADOR_NPC.get(), net.minecraft.client.gui.screens.inventory.MerchantScreen::new);
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RegistroNpc.CUSTOM_NPC.get(), RenderizadorNpcPersonalizado::new);
    }
}
