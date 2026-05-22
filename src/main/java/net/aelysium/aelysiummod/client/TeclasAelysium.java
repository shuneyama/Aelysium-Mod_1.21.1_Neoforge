package net.aelysium.aelysiummod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.network.FormaDivinaActivatePacket;
import net.aelysium.aelysiummod.network.VanishActivatePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class TeclasAelysium {

    public static KeyMapping TECLA_FORMA_DIVINA;
    public static KeyMapping TECLA_VANISH;

    public static void registrar(RegisterKeyMappingsEvent event) {
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
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;

        if (TECLA_FORMA_DIVINA != null && TECLA_FORMA_DIVINA.consumeClick()) {
            PacketDistributor.sendToServer(new FormaDivinaActivatePacket());
        }

        if (TECLA_VANISH != null && TECLA_VANISH.consumeClick()) {
            PacketDistributor.sendToServer(new VanishActivatePacket());
        }
    }
}