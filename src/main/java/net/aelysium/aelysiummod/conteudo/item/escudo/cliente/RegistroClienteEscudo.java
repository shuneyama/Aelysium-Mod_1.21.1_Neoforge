package net.aelysium.aelysiummod.conteudo.item.escudo.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModEntidades;
import net.aelysium.aelysiummod.registro.ModItens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteEscudo {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registrarTemporizadorBarreira(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new RenderizadorTemporizadorBarreira());
    }

    @SubscribeEvent
    public static void registrarPropriedadesItem(FMLClientSetupEvent event) {
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                ModItens.ESCUDO_ABISMO.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("blocking"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem().is(stack.getItem()) ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void registrarDecoracoesItem(RegisterItemDecorationsEvent event) {
        event.register(ModItens.ESCUDO_ABISMO.get(), new DecoradorRecargaEscudo());
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntidades.BARREIRA_PAINEL.get(), RenderizadorPainelBarreira::new);
    }
}
