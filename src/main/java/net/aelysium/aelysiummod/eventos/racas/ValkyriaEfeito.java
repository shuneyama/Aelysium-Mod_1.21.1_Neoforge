package net.aelysium.aelysiummod.eventos.racas;

import net.aelysium.aelysiummod.comandos.racas.Valkyria_Config;
import net.aelysium.aelysiummod.eventos.EfeitosTick;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ValkyriaEfeito {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var valkyriaTeam = server.getScoreboard().getPlayerTeam("valkyrias");

        if (valkyriaTeam == null) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {

            if (p.getTeam() != valkyriaTeam) continue;

            if (!EfeitosTick.shouldTrigger(p, 60)) continue;

            var cfg = Valkyria_Config.DATA;
            if (cfg == null || !cfg.effects.enabled) continue;

            for (var e : cfg.effects.list) {
                var effectHolder = Valkyria_Config.getEffect(e.effect);
                if (effectHolder != null) {
                    p.addEffect(new MobEffectInstance(effectHolder, e.duration * 20, e.amplifier));
                }
            }
        }
    }
}