package net.aelysium.aelysiummod.sistemas.rp.morte;

import net.aelysium.aelysiummod.compatibilidade.jade.ControleOverlayJade;
import net.aelysium.aelysiummod.sistemas.rp.morte.rede.PacoteImpactoMorte;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class AnimacaoMorte {

    private static final int DURACAO_TICKS = 100;

    private static boolean ativo = false;
    private static int tickCounter = 0;

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
        if (tickCounter >= DURACAO_TICKS) {
            stop(server);
        }
    }
}
