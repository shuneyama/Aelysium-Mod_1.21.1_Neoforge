package net.aelysium.aelysiummod.banlist.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.aelysium.aelysiummod.banlist.config.BanlistConfig;
import net.aelysium.aelysiummod.banlist.data.BanlistUtil;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import net.neoforged.neoforge.common.util.TriState;

public class CuriosCompat {

    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCurioCanEquip(CurioCanEquipEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (BanlistUtil.isBlockedForPlayer(stack, player)) {
                event.setEquipResult(TriState.FALSE);
            }
        }
    }

    @SubscribeEvent
    public void onCurioChange(CurioChangeEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        ItemStack to = event.getTo();
        if (to.isEmpty()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (BanlistUtil.isBlockedForPlayer(to, player)) {
                player.getServer().execute(() -> {
                    top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                        String slotId = event.getIdentifier();
                        int slotIndex = event.getSlotIndex();
                        handler.getStacksHandler(slotId).ifPresent(stacksHandler -> {
                            ItemStack current = stacksHandler.getStacks().getStackInSlot(slotIndex);
                            ResourceLocation id = BuiltInRegistries.ITEM.getKey(current.getItem());
                            if (id != null && BanlistConfig.isBanned(id)) {
                                stacksHandler.getStacks().setStackInSlot(slotIndex, ItemStack.EMPTY);
                                player.drop(current, false);
                                player.sendSystemMessage(Component.literal(BanlistConfig.getTooltipBanned())
                                        .withStyle(ChatFormatting.RED));
                            }
                        });
                    });
                });
            }
        }
    }
}
