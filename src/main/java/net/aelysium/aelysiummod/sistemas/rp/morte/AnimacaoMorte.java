package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.compatibilidade.jade.ControleOverlayJade;
import net.aelysium.aelysiummod.sistemas.rp.morte.rede.PacoteImpactoMorte;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class AnimacaoMorte {

    private static final int DURACAO_TICKS_HABILITAR = 580;
    private static final int DURACAO_TICKS_DESABILITAR = 420;

    private static boolean ativo = false;
    private static int tickCounter = 0;
    private static int duracaoTicks = DURACAO_TICKS_DESABILITAR;

    public static boolean isRunning() {
        return ativo;
    }

    public static void start(MinecraftServer server) {
        beginSequence(server, true);
    }

    public static void startDisable(MinecraftServer server) {
        beginSequence(server, false);
    }

    private static void beginSequence(MinecraftServer server, boolean habilitando) {
        if (ativo) return;

        ativo = true;
        tickCounter = 0;
        duracaoTicks = habilitando ? DURACAO_TICKS_HABILITAR : DURACAO_TICKS_DESABILITAR;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.closeContainer();
        }

        ControleOverlayJade.hide(server.getPlayerList().getPlayers());
        PacketDistributor.sendToAllPlayers(new PacoteImpactoMorte(habilitando));
    }

    public static void stop(MinecraftServer server) {
        ativo = false;
        ControleOverlayJade.show(server.getPlayerList().getPlayers());
    }

    public static void tick(MinecraftServer server) {
        if (!ativo) return;

        tickCounter++;
        if (tickCounter >= duracaoTicks) {
            stop(server);
        }
    }
}
