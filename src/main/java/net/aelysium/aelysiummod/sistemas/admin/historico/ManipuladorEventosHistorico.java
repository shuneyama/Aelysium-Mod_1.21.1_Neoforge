package net.aelysium.aelysiummod.sistemas.admin.historico;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorEventosHistorico {

    private static final int INTERVALO_SEGURANCA_TICKS = 1200;
    private static volatile boolean conferirOps;

    @SubscribeEvent
    public static void aoComando(CommandEvent evento) {
        String comando = evento.getParseResults().getReader().getString().trim();
        if (comando.startsWith("/")) comando = comando.substring(1);
        String[] partes = comando.split("\\s+");
        if (partes.length < 2) return;
        if (!partes[0].equalsIgnoreCase("op") && !partes[0].equalsIgnoreCase("deop")) return;

        String autor = evento.getParseResults().getContext().getSource().getTextName();
        for (int i = 1; i < partes.length; i++) DadosHistorico.registrarAutorOp(partes[i], autor);
        conferirOps = true;
    }

    @SubscribeEvent
    public static void aoTickServidor(ServerTickEvent.Post evento) {
        if (conferirOps || evento.getServer().getTickCount() % INTERVALO_SEGURANCA_TICKS == 0) {
            conferirOps = false;
            DadosHistorico.manutencao(evento.getServer());
        }
    }
}
