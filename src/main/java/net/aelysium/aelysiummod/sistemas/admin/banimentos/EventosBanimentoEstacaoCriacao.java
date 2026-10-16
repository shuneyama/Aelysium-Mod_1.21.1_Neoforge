package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;

public class EventosBanimentoEstacaoCriacao {

    private static boolean banido(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && ConfiguracaoListaBanimentos.isBanned(id);
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (banido(event.getLeft()) || banido(event.getRight())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGrindstonePlace(GrindstoneEvent.OnPlaceItem event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (banido(event.getTopItem()) || banido(event.getBottomItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotionBrew(PotionBrewEvent.Pre event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        for (int i = 0; i < event.getLength(); i++) {
            if (banido(event.getItem(i))) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
