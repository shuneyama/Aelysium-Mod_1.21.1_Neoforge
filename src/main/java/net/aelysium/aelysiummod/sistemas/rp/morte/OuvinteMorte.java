package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class OuvinteMorte {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean duranteEvento = ConfiguracaoMorte.isHabilitada();
        if (!duranteEvento && !player.hasEffect(ModEfeitos.MORTE_CANONICA_EVENTO)) return;

        RegistradorMortes.registrar(player, event.getSource(), duranteEvento);
    }
}
