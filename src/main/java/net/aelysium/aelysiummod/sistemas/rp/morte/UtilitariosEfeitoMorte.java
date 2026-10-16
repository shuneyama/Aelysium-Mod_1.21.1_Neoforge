package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class UtilitariosEfeitoMorte {

    public static void aplicar(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(ModEfeitos.MORTE_CANONICA_EVENTO, Integer.MAX_VALUE, 0, false, false, true));
    }

    public static void remover(ServerPlayer player) {
        player.removeEffect(ModEfeitos.MORTE_CANONICA_EVENTO);
    }
}
