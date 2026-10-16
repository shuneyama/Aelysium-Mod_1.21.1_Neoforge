package net.aelysium.aelysiummod.sistemas.mundo.regras;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorRecargaPocao {

    private static final int COOLDOWN_TICKS = 5 * 20;

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ThrowablePotionItem)) return;

        aplicarCooldown(player);
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        if (!(stack.getItem() instanceof PotionItem) || stack.getItem() instanceof ThrowablePotionItem) return;

        aplicarCooldown(player);
    }

    private static void aplicarCooldown(ServerPlayer player) {
        player.getCooldowns().addCooldown(Items.POTION, COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(Items.SPLASH_POTION, COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(Items.LINGERING_POTION, COOLDOWN_TICKS);
    }
}
