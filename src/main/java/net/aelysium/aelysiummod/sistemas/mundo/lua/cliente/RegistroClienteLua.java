package net.aelysium.aelysiummod.sistemas.mundo.lua.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistroClienteLua {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void aoConfigurarCliente(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new RenderizadorLuaCliente());
        net.aelysium.aelysiummod.conteudo.bloco.DamaNoiteItem.luaVermelhaNoCliente =
                () -> RenderizadorLuaCliente.luaCliente == net.aelysium.aelysiummod.sistemas.mundo.lua.TipoLua.VERMELHA;
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(
                net.aelysium.aelysiummod.registro.ModBlocos.DAMA_DA_NOITE.get().asItem(),
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "lua_vermelha"),
                (stack, level, entity, seed) -> RenderizadorLuaCliente.luaCliente == net.aelysium.aelysiummod.sistemas.mundo.lua.TipoLua.VERMELHA ? 1.0F : 0.0F));
    }
}
