package net.aelysium.aelysiummod.eventos.racas;

import net.aelysium.aelysiummod.comandos.racas.Humano_Config;
import net.aelysium.aelysiummod.eventos.EfeitosTick;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class HumanoEfeito {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var humanoTeam = server.getScoreboard().getPlayerTeam("humanos");

        if (humanoTeam == null) return;

        for (ServerPlayer p : server.getPlayerList().getPlayers()) {

            if (p.getTeam() != humanoTeam) continue;

            if (!EfeitosTick.shouldTrigger(p, 60)) continue;

            var cfg = Humano_Config.DATA;
            if (cfg == null || !cfg.effects.enabled) continue;

            for (var e : cfg.effects.list) {
                var effectHolder = Humano_Config.getEffect(e.effect);
                if (effectHolder != null) {
                    p.addEffect(new MobEffectInstance(effectHolder, e.duration * 20, e.amplifier));
                }
            }
        }
    }
}