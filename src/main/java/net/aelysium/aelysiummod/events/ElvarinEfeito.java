package net.aelysium.aelysiummod.events;

import net.aelysium.aelysiummod.command.racas.Elvarin_Config;
import net.aelysium.aelysiummod.system.EfeitosTick;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ElvarinEfeito {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var elvarinTeam = server.getScoreboard().getPlayerTeam("elvarins");

        if (elvarinTeam == null) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {

            if (p.getTeam() != elvarinTeam) continue;

            if (!EfeitosTick.shouldTrigger(p, 60)) continue;

            var cfg = Elvarin_Config.DATA;
            if (cfg == null || !cfg.effects.enabled) continue;

            for (var e : cfg.effects.list) {
                var effectHolder = Elvarin_Config.getEffect(e.effect);
                if (effectHolder != null) {
                    p.addEffect(new MobEffectInstance(effectHolder, e.duration * 20, e.amplifier));
                }
            }
        }
    }
}