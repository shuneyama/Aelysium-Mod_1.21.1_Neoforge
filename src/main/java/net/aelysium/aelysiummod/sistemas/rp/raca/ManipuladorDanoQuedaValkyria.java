package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorDanoQuedaValkyria {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isValkyria(player)) return;
        event.setDistance(0.0f);
        event.setDamageMultiplier(0.0f);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getContainer().getSource().is(DamageTypes.FALL)) return;
        if (!isValkyria(player)) return;
        event.getContainer().setNewDamage(0.0f);
    }

    private static boolean isValkyria(ServerPlayer player) {
        if (player.level().isClientSide()) return false;
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;
        try {
            DadosRaca data = DadosRaca.get(serverLevel);
            return data.getRace(player.getUUID()) == TipoRaca.VALKYRIA;
        } catch (Exception e) {
            return false;
        }
    }
}