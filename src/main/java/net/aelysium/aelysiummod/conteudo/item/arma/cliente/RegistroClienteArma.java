package net.aelysium.aelysiummod.conteudo.item.arma.cliente;

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
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteArma {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registrarHudSabre(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new RenderizadorHudSabre());
    }

    @SubscribeEvent
    public static void registrarPropriedadesItem(FMLClientSetupEvent event) {
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                ModItens.ULTIMO_SUSPIRO.get(), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "carregando"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem().is(stack.getItem()) ? 1.0F : 0.0F));
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                ModItens.TRIDENTE_CORAL.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("throwing"),
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void registrarExtensoesCliente(RegisterClientExtensionsEvent event) {
        event.registerItem(new UltimoSuspiroExtensoes(), ModItens.ULTIMO_SUSPIRO.get());
    }

    @SubscribeEvent
    public static void registrarDecoracoesItem(RegisterItemDecorationsEvent event) {
        event.register(ModItens.TRIDENTE_CORAL.get(), new DecoradorRecargaTridente());
        event.register(ModItens.ULTIMO_SUSPIRO.get(), new DecoradorCargasSabre());
    }

    @SubscribeEvent
    public static void registrarRenderizadores(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntidades.TRIDENTE_CONCHAS.get(), RenderizadorTridenteConchas::new);
    }
}
