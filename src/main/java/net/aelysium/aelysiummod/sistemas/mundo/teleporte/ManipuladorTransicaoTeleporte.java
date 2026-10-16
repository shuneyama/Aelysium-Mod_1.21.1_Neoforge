package net.aelysium.aelysiummod.sistemas.mundo.teleporte;

import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManipuladorTransicaoTeleporte {

    public enum Fase {
        GATHERING,
        WAITING,
        DISPERSING,
        DONE
    }

    public static class DadosTransicao {
        public final ServerPlayer player;
        public final TipoDeus deus;
        public final ResourceKey<Level> destDim;
        public final double destX, destY, destZ;
        public final float destYaw, destPitch;
        public final boolean temCoordenada;
        public Fase phase = Fase.GATHERING;
        public int tickCount = 0;
        public boolean glowingAtivo = true;
        public int glowToggleTicks = 0;

        public DadosTransicao(ServerPlayer player, TipoDeus deus, ResourceKey<Level> destDim,
                              double destX, double destY, double destZ,
                              float destYaw, float destPitch, boolean temCoordenada) {
            this.player = player;
            this.deus = deus;
            this.destDim = destDim;
            this.destX = destX;
            this.destY = destY;
            this.destZ = destZ;
            this.destYaw = destYaw;
            this.destPitch = destPitch;
            this.temCoordenada = temCoordenada;
        }
    }

    public static final Map<UUID, DadosTransicao> activeTransitions = new ConcurrentHashMap<>();

    public static void startTransition(ServerPlayer player, TipoDeus deus, ResourceKey<Level> destDim,
                                       double x, double y, double z,
                                       float yaw, float pitch, boolean temCoordenada) {
        UUID id = player.getUUID();
        if (activeTransitions.containsKey(id)) return;
        activeTransitions.put(id, new DadosTransicao(player, deus, destDim, x, y, z, yaw, pitch, temCoordenada));
    }

    public static void removeTransition(UUID id) {
        activeTransitions.remove(id);
    }

    public static boolean isInTransition(UUID id) {
        return activeTransitions.containsKey(id);
    }
}