package net.aelysium.aelysiummod.sistemas.rp.raca.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteAtivarVisaoNoturna;
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
public class TeclasRacaCliente {

    public static KeyMapping TECLA_VISAO_NOTURNA;

    @SubscribeEvent
    public static void registrarTeclas(RegisterKeyMappingsEvent event) {
        TECLA_VISAO_NOTURNA = new KeyMapping(
                "key.aelysiummod.visao_noturna",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "key.categories.aelysiummod"
        );
        event.register(TECLA_VISAO_NOTURNA);

        NeoForge.EVENT_BUS.addListener(TeclasRacaCliente::aoTick);
    }

    private static void aoTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;

        if (TECLA_VISAO_NOTURNA != null && TECLA_VISAO_NOTURNA.consumeClick()) {
            PacketDistributor.sendToServer(new PacoteAtivarVisaoNoturna());
        }
    }
}
