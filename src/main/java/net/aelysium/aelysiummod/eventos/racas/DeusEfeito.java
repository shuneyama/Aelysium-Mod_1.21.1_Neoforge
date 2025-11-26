package net.aelysium.aelysiummod.eventos.racas;

import net.aelysium.aelysiummod.comandos.racas.Deus_Config;
import net.aelysium.aelysiummod.eventos.EfeitosTick;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class DeusEfeito {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var deusTeam = server.getScoreboard().getPlayerTeam("deuses");

        if (deusTeam == null) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {

            if (p.getTeam() != deusTeam) continue;

            if (!EfeitosTick.shouldTrigger(p, 30)) continue;

            var cfg = Deus_Config.DATA;
            if (cfg == null || !cfg.effects.enabled) continue;

            for (var e : cfg.effects.list) {
                var effectHolder = Deus_Config.getEffect(e.effect);
                if (effectHolder != null) {
                    p.addEffect(new MobEffectInstance(effectHolder, e.duration * 20, e.amplifier));
                }
            }
        }
    }

}
