package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;

public class EventosTrocaModsDesativados {

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!AelysiumGuarda.isAutorizadoAgora()) return;
        if (!(event.getTarget() instanceof Merchant)) return;

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType());
        if (typeId == null) return;

        if (ConfiguracaoModsDesativados.isTrocasCapped(typeId.getNamespace())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onWandererTrades(WandererTradesEvent event) {
        filtrarListagens(event.getGenericTrades());
        filtrarListagens(event.getRareTrades());
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        for (List<VillagerTrades.ItemListing> listagens : event.getTrades().values()) {
            filtrarListagens(listagens);
        }
    }

    private void filtrarListagens(List<VillagerTrades.ItemListing> listagens) {
        listagens.removeIf(this::listagemEhDeModCapado);
    }

    private boolean listagemEhDeModCapado(VillagerTrades.ItemListing listagem) {
        try {
            MerchantOffer offer = listagem.getOffer(null, RandomSource.create());
            if (offer == null) return false;
            return itemDeModCapado(offer.getResult());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean itemDeModCapado(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && ConfiguracaoModsDesativados.isTrocasCapped(id.getNamespace());
    }
}
