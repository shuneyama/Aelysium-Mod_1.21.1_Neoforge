package net.aelysium.aelysiummod.sistemas.rp.raca;

import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.raca.rede.PacoteAbrirSelecaoRaca;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelecaoRaca {

    private static final Set<UUID> PENDENTES = ConcurrentHashMap.newKeySet();

    public static void abrir(ServerPlayer jogador) {
        PENDENTES.add(jogador.getUUID());
        TipoRaca atual = TemporizadorRaca.getCachedRace(jogador.getUUID());
        PacketDistributor.sendToPlayer(jogador, new PacoteAbrirSelecaoRaca(atual.id));
    }

    public static boolean pendente(UUID uuid) {
        return PENDENTES.contains(uuid);
    }

    public static void concluir(UUID uuid) {
        PENDENTES.remove(uuid);
    }

    public static void escolher(ServerPlayer jogador, String idRaca) {
        if (!pendente(jogador.getUUID())) return;
        TipoRaca raca = TipoRaca.fromId(idRaca);
        if (raca == TipoRaca.NONE || raca == TipoRaca.ROBO) return;
        if (AnimacaoRevelacaoRaca.isAnimating(jogador.getUUID())) return;

        TipoRaca anterior = TemporizadorRaca.getCachedRace(jogador.getUUID());
        concluir(jogador.getUUID());
        AnimacaoRevelacaoRaca.start(jogador, raca, anterior);
    }
}
