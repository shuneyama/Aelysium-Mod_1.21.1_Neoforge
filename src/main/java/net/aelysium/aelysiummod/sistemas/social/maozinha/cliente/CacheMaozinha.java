package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMaozinha {

    private static final Map<UUID, UUID> ofertas = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> segurandoPorLider = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> liderPorSeguidor = new ConcurrentHashMap<>();

    public static void ofertar(UUID ofertante, UUID alvo) {
        ofertas.put(ofertante, alvo);
    }

    public static void ligar(UUID lider, UUID seguidor) {
        ofertas.remove(lider);
        ofertas.remove(seguidor);
        segurandoPorLider.put(lider, seguidor);
        liderPorSeguidor.put(seguidor, lider);
    }

    public static void desfazer(UUID lider, UUID alvo) {
        ofertas.remove(lider);
        UUID seguidor = segurandoPorLider.get(lider);
        if (seguidor != null && seguidor.equals(alvo)) {
            segurandoPorLider.remove(lider);
            liderPorSeguidor.remove(seguidor);
        }
    }

    public static UUID parceiro(UUID jogador) {
        UUID seguidor = segurandoPorLider.get(jogador);
        if (seguidor != null) return seguidor;
        return liderPorSeguidor.get(jogador);
    }

    public static UUID alvoDaOferta(UUID jogador) {
        return ofertas.get(jogador);
    }

    public static UUID liderDe(UUID seguidor) {
        return liderPorSeguidor.get(seguidor);
    }

    public static UUID seguidorDe(UUID lider) {
        return segurandoPorLider.get(lider);
    }

    public static boolean ehLider(UUID jogador) {
        return segurandoPorLider.containsKey(jogador);
    }

    public static boolean estaAtivo(UUID jogador) {
        return segurandoPorLider.containsKey(jogador) || liderPorSeguidor.containsKey(jogador) || ofertas.containsKey(jogador);
    }

    public static void limpar() {
        ofertas.clear();
        segurandoPorLider.clear();
        liderPorSeguidor.clear();
    }
}
