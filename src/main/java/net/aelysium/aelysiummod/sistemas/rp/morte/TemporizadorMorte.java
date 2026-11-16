package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.registro.ModEfeitos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TemporizadorMorte {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        AnimacaoMorte.tick(event.getServer());

        if (event.getServer().getTickCount() % 20 == 0) {
            boolean habilitada = ConfiguracaoMorte.isHabilitada();
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                LimiteTotens.sincronizarEfeito(player);
                boolean tem = player.hasEffect(ModEfeitos.MORTE_CANONICA_EVENTO);
                if (habilitada && !tem) {
                    UtilitariosEfeitoMorte.aplicar(player);
                } else if (!habilitada && tem) {
                    UtilitariosEfeitoMorte.remover(player);
                }
            }
        }
    }
}
