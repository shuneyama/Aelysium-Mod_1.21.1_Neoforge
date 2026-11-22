package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class EventosLootModsDesativados {

    private static boolean itemDeModCapado(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && ConfiguracaoModsDesativados.isLootCapped(id.getNamespace());
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player) return;

        event.getDrops().removeIf(itemEntity -> itemDeModCapado(itemEntity.getItem()));
    }

    @SubscribeEvent
    public void onBlockDrops(BlockDropsEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;

        event.getDrops().removeIf(itemEntity -> itemDeModCapado(itemEntity.getItem()));
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (event.getLevel().isClientSide()) return;
        if (event.loadedFromDisk()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;
        if (itemEntity.getOwner() instanceof net.minecraft.world.entity.player.Player) return;

        if (itemDeModCapado(itemEntity.getItem())) {
            event.setCanceled(true);
        }
    }
}
