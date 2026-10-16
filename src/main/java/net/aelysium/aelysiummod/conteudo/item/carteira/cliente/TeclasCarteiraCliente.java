package net.aelysium.aelysiummod.conteudo.item.carteira.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.carteira.rede.PacoteAbrirCarteira;
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
public class TeclasCarteiraCliente {

    public static KeyMapping TECLA_CARTEIRA;

    @SubscribeEvent
    public static void registrarTeclas(RegisterKeyMappingsEvent event) {
        TECLA_CARTEIRA = new KeyMapping(
                "key.aelysiummod.carteira",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.categories.aelysiummod"
        );
        event.register(TECLA_CARTEIRA);

        NeoForge.EVENT_BUS.addListener(TeclasCarteiraCliente::aoTick);
    }

    private static void aoTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;

        if (TECLA_CARTEIRA != null && TECLA_CARTEIRA.consumeClick()) {
            PacketDistributor.sendToServer(new PacoteAbrirCarteira());
        }
    }
}
