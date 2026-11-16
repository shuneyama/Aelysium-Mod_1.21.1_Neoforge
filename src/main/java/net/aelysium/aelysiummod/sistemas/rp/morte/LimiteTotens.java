package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimiteTotens {

    public static final int LIMITE = 3;

    private static final Map<UUID, Integer> USOS = new HashMap<>();

    public static void reiniciar() {
        USOS.clear();
    }

    public static boolean esgotado(UUID jogador) {
        return USOS.getOrDefault(jogador, 0) >= LIMITE;
    }

    public static void sincronizarEfeito(ServerPlayer jogador) {
        boolean deveTer = ConfiguracaoMorte.isHabilitada() && esgotado(jogador.getUUID());
        boolean tem = jogador.hasEffect(ModEfeitos.TOTEM_ESGOTADO);
        if (deveTer && !tem) {
            jogador.addEffect(new MobEffectInstance(ModEfeitos.TOTEM_ESGOTADO, Integer.MAX_VALUE, 0, false, false, true));
        } else if (!deveTer && tem) {
            jogador.removeEffect(ModEfeitos.TOTEM_ESGOTADO);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void aoUsarTotem(LivingUseTotemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (!ConfiguracaoMorte.isHabilitada()) return;

        UUID id = jogador.getUUID();
        if (esgotado(id) || jogador.hasEffect(ModEfeitos.TOTEM_ESGOTADO)) {
            event.setCanceled(true);
            return;
        }
        USOS.merge(id, 1, Integer::sum);
    }
}
