package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacoteAcionarMaozinha;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class TeclasMaozinhaCliente {

    public static KeyMapping TECLA_MAOZINHA;

    @SubscribeEvent
    public static void registrarTeclas(RegisterKeyMappingsEvent event) {
        TECLA_MAOZINHA = new KeyMapping(
                "key.aelysiummod.maozinha",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.aelysiummod"
        );
        event.register(TECLA_MAOZINHA);

        NeoForge.EVENT_BUS.addListener(TeclasMaozinhaCliente::aoTick);
    }

    private static void aoTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || TECLA_MAOZINHA == null) return;

        while (TECLA_MAOZINHA.consumeClick()) {
            if (minecraft.screen != null) continue;
            Entity mirado = minecraft.crosshairPickEntity;
            int alvoId = mirado instanceof Player ? mirado.getId() : -1;
            PacketDistributor.sendToServer(new PacoteAcionarMaozinha(alvoId));
        }
    }
}
