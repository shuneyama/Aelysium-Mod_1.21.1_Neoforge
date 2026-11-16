package net.aelysium.aelysiummod.sistemas.mundo.regras;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManipuladorRecargaTeia {

    private static final long COOLDOWN_MS = 3_000L;
    private static final int COOLDOWN_TICKS = 60;

    private static final Map<UUID, Long> ultimaColocacao = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        if (stack.getItem() != Items.COBWEB) return;

        long agora = System.currentTimeMillis();
        Long ultimo = ultimaColocacao.get(player.getUUID());

        if (ultimo != null) {
            long restanteMs = COOLDOWN_MS - (agora - ultimo);
            if (restanteMs > 0) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                long restanteSeg = (restanteMs + 999) / 1000;
                player.displayClientMessage(Component.translatableEscape("mensagem.aelysiummod.regras.aguarde_s_para_colocar_outra_teia", restanteSeg), true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!AelysiumGuarda.isAutorizado()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getPlacedBlock().is(Blocks.COBWEB)) return;

        ultimaColocacao.put(player.getUUID(), System.currentTimeMillis());
        player.getCooldowns().addCooldown(Items.COBWEB, COOLDOWN_TICKS);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ultimaColocacao.remove(event.getEntity().getUUID());
    }
}
