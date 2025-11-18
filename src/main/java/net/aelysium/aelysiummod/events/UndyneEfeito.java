package net.aelysium.aelysiummod.events;

import net.aelysium.aelysiummod.command.racas.Dracono_Config;
import net.aelysium.aelysiummod.system.EffectTicker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class UndyneEfeito {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var draconoTeam = server.getScoreboard().getPlayerTeam("undynes");

        if (draconoTeam == null) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {

            if (p.getTeam() != draconoTeam) continue;

            if (!EffectTicker.shouldTrigger(p, 60)) continue;

            var cfg = Dracono_Config.DATA;
            if (cfg == null || !cfg.effects.enabled) continue;

            for (var e : cfg.effects.list) {
                var effectHolder = Dracono_Config.getEffect(e.effect);
                if (effectHolder != null) {
                    p.addEffect(new MobEffectInstance(effectHolder, e.duration * 20, e.amplifier));
                }
            }
        }
    }
}