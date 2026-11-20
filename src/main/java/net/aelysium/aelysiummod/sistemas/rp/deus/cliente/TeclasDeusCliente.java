package net.aelysium.aelysiummod.sistemas.rp.deus.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteAtivarFormaDivina;
import net.aelysium.aelysiummod.sistemas.rp.deus.rede.PacoteAtivarVanish;
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

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class TeclasDeusCliente {

    public static KeyMapping TECLA_FORMA_DIVINA;
    public static KeyMapping TECLA_VANISH;

    @SubscribeEvent
    public static void registrarTeclas(RegisterKeyMappingsEvent event) {
        TECLA_FORMA_DIVINA = new KeyMapping(
                "key.aelysiummod.forma_divina",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.aelysiummod"
        );
        event.register(TECLA_FORMA_DIVINA);

        TECLA_VANISH = new KeyMapping(
                "key.aelysiummod.vanish",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.aelysiummod"
        );
        event.register(TECLA_VANISH);

        NeoForge.EVENT_BUS.addListener(TeclasDeusCliente::aoTick);
    }

    private static void aoTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;

        if (TECLA_FORMA_DIVINA != null && TECLA_FORMA_DIVINA.consumeClick()) {
            PacketDistributor.sendToServer(new PacoteAtivarFormaDivina());
        }

        if (TECLA_VANISH != null && TECLA_VANISH.consumeClick()) {
            PacketDistributor.sendToServer(new PacoteAtivarVanish());
        }
    }
}
