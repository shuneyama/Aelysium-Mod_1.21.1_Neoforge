package net.aelysium.aelysiummod.conteudo.efeito.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
public class ManipuladorCongelarCliente {

    private static final ResourceLocation CONGELAR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "habilidade_congelar");

    private static boolean estaCongelado(Player player) {
        return player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(CONGELAR_MODIFIER_ID) != null;
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !estaCongelado(mc.player)) return;
        if (event.getNewScreen() instanceof InventoryScreen) {
            event.setCanceled(true);
        }
    }
}
