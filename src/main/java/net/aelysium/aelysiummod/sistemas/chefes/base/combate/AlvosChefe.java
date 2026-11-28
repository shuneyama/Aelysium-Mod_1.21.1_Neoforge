package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlvosChefe {
    private static final double DECAIMENTO_POR_SEGUNDO = 0.9;
    private static final double AMEACA_MINIMA = 0.1;
    private static final double DISTANCIA_PROXIMA = 10.0;

    private final Mob chefe;
    private final Map<UUID, Ameaca> ameacas = new HashMap<>();
    private long ultimoDano = Long.MIN_VALUE / 2;

    private static final class Ameaca {
        private double valor;
        private long tick;
    }

    public AlvosChefe(Mob chefe) {
        this.chefe = chefe;
    }

    public void registrarDano(Entity atacante, float dano) {
        long agora = chefe.level().getGameTime();
        ultimoDano = agora;
        if (!(atacante instanceof Player jogador) || dano <= 0) return;
        Ameaca ameaca = ameacas.computeIfAbsent(jogador.getUUID(), id -> new Ameaca());
        ameaca.valor = valorAtual(ameaca, agora) + dano;
        ameaca.tick = agora;
    }

    public long ticksDesdeUltimoDano() {
        return chefe.level().getGameTime() - ultimoDano;
    }

    public List<Player> candidatos() {
        long agora = chefe.level().getGameTime();
        ameacas.values().removeIf(ameaca -> valorAtual(ameaca, agora) < AMEACA_MINIMA);
        double alcance = chefe.getAttributeValue(Attributes.FOLLOW_RANGE);
        double alcanceQuadrado = alcance * alcance;
        return chefe.level().getEntitiesOfClass(Player.class, chefe.getBoundingBox().inflate(alcance),
                jogador -> jogador.isAlive() && !jogador.isSpectator() && !jogador.isCreative() && !jogador.isInvisible() && chefe.distanceToSqr(jogador) <= alcanceQuadrado);
    }

    public LivingEntity escolher() {
        List<Player> candidatos = candidatos();
        if (candidatos.isEmpty()) return null;
        if (candidatos.size() == 1) return candidatos.get(0);

        long agora = chefe.level().getGameTime();
        LivingEntity atual = chefe.getTarget();
        double[] pesos = new double[candidatos.size()];
        double total = 0;
        for (int i = 0; i < pesos.length; i++) {
            Player jogador = candidatos.get(i);
            double peso = 1 + ameaca(jogador, agora) / 10.0;
            if (chefe.distanceToSqr(jogador) <= DISTANCIA_PROXIMA * DISTANCIA_PROXIMA) peso += 1;
            if (jogador == atual) peso *= 0.5;
            pesos[i] = peso;
            total += peso;
        }

        double sorteio = chefe.getRandom().nextDouble() * total;
        for (int i = 0; i < pesos.length; i++) {
            sorteio -= pesos[i];
            if (sorteio <= 0) return candidatos.get(i);
        }
        return candidatos.get(candidatos.size() - 1);
    }

    public List<LivingEntity> alvos(int maximo) {
        return alvos(maximo, false);
    }

    public static boolean estaVoando(LivingEntity alvo) {
        return alvo instanceof Player jogador && (jogador.getAbilities().flying || jogador.isFallFlying());
    }

    public boolean alguemVoando() {
        return candidatos().stream().anyMatch(AlvosChefe::estaVoando);
    }

    public List<LivingEntity> alvos(int maximo, boolean priorizarVoando) {
        List<LivingEntity> resultado = new ArrayList<>();
        if (maximo <= 0) return resultado;

        LivingEntity atual = chefe.getTarget();
        if (atual != null && atual.isAlive()) resultado.add(atual);

        long agora = chefe.level().getGameTime();
        List<Player> candidatos = candidatos();
        candidatos.sort(Comparator.<Player>comparingInt(jogador -> priorizarVoando && estaVoando(jogador) ? 0 : 1).thenComparingDouble(jogador -> -ameaca(jogador, agora)).thenComparingDouble(chefe::distanceToSqr));
        for (Player jogador : candidatos) {
            if (resultado.size() >= maximo) break;
            if (jogador != atual) resultado.add(jogador);
        }
        return resultado;
    }

    private double ameaca(Player jogador, long agora) {
        Ameaca ameaca = ameacas.get(jogador.getUUID());
        return ameaca == null ? 0 : valorAtual(ameaca, agora);
    }

    private static double valorAtual(Ameaca ameaca, long agora) {
        return ameaca.valor * Math.pow(DECAIMENTO_POR_SEGUNDO, (agora - ameaca.tick) / 20.0);
    }
}
