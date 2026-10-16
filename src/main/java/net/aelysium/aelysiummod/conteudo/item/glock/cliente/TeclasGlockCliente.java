package net.aelysium.aelysiummod.conteudo.item.glock.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.glock.rede.PacoteRecarregarGlock;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TeclasGlockCliente {

    public static KeyMapping TECLA_RECARREGAR_GLOCK;

    @SubscribeEvent
    public static void registrarTeclas(RegisterKeyMappingsEvent event) {
        TECLA_RECARREGAR_GLOCK = new KeyMapping(
                "key.aelysiummod.recarregar_glock",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.aelysiummod"
        );
        event.register(TECLA_RECARREGAR_GLOCK);

        NeoForge.EVENT_BUS.addListener(TeclasGlockCliente::aoTick);
    }

    private static void aoTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;

        if (TECLA_RECARREGAR_GLOCK != null && TECLA_RECARREGAR_GLOCK.consumeClick()) {
            PacketDistributor.sendToServer(new PacoteRecarregarGlock());
        }
    }
}
