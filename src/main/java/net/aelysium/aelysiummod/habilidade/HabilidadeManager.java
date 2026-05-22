package net.aelysium.aelysiummod.habilidade;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HabilidadeManager {

    private static final Set<UUID> vooAtivo = new HashSet<>();
    private static final Set<UUID> deusAtivo = new HashSet<>();
    private static final Set<UUID> congeladoAtivo = new HashSet<>();
    private static final Set<UUID> mudoAtivo = new HashSet<>();

    private static boolean manutencaoAtiva = false;

    public static boolean toggleVoo(ServerPlayer player) {
        UUID id = player.getUUID();
        if (vooAtivo.contains(id)) {
            vooAtivo.remove(id);
            return false;
        } else {
            vooAtivo.add(id);
            return true;
        }
    }

    public static boolean temVoo(ServerPlayer player) {
        return vooAtivo.contains(player.getUUID());
    }

    public static boolean toggleDeus(ServerPlayer player) {
        UUID id = player.getUUID();
        if (deusAtivo.contains(id)) {
            deusAtivo.remove(id);
            return false;
        } else {
            deusAtivo.add(id);
            return true;
        }
    }

    public static boolean temDeus(ServerPlayer player) {
        return deusAtivo.contains(player.getUUID());
    }

    public static boolean toggleCongelar(ServerPlayer player) {
        UUID id = player.getUUID();
        if (congeladoAtivo.contains(id)) {
            congeladoAtivo.remove(id);
            return false;
        } else {
            congeladoAtivo.add(id);
            return true;
        }
    }

    public static boolean estaCongelado(ServerPlayer player) {
        return congeladoAtivo.contains(player.getUUID());
    }

    public static boolean toggleMudo(ServerPlayer player) {
        UUID id = player.getUUID();
        if (mudoAtivo.contains(id)) {
            mudoAtivo.remove(id);
            return false;
        } else {
            mudoAtivo.add(id);
            return true;
        }
    }

    public static boolean estaMudo(UUID uuid) {
        return mudoAtivo.contains(uuid);
    }

    public static boolean isManutencaoAtiva() {
        return manutencaoAtiva;
    }

    public static void setManutencao(boolean ativo) {
        manutencaoAtiva = ativo;
    }

    public static boolean toggleManutencao() {
        manutencaoAtiva = !manutencaoAtiva;
        return manutencaoAtiva;
    }

    public static void limpar(UUID uuid) {
        vooAtivo.remove(uuid);
        deusAtivo.remove(uuid);
        congeladoAtivo.remove(uuid);
        mudoAtivo.remove(uuid);
    }
}