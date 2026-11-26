package net.aelysium.aelysiummod.sistemas.social.maozinha;

import net.aelysium.aelysiummod.sistemas.social.maozinha.cliente.CacheMaozinha;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class FormacaoMaozinha {

    private static final class Estado {
        boolean iniciado;
        int ultimoTick = Integer.MIN_VALUE;
        Vec3 base;
        Vec3 posicao;
        float yawAnterior;
        float yawAtual;
        double angulo;
        double raio;
        boolean alternativa;
        int ticksBloqueado;
        int ticksLivre;
    }

    private static final Map<Entity, Estado> ESTADOS_CLIENTE = new WeakHashMap<>();
    private static final Map<Entity, Estado> ESTADOS_SERVIDOR = new WeakHashMap<>();
    private static final Set<UUID> SOLTANDO = ConcurrentHashMap.newKeySet();

    private FormacaoMaozinha() {
    }

    private static Map<Entity, Estado> mapa(Entity lider) {
        return lider.level().isClientSide ? ESTADOS_CLIENTE : ESTADOS_SERVIDOR;
    }

    public static boolean ehPar(Entity lider, Entity seguidor) {
        if (!(lider instanceof Player) || !(seguidor instanceof Player)) return false;
        UUID idLider = lider.level().isClientSide
                ? CacheMaozinha.liderDe(seguidor.getUUID())
                : GerenciadorMaozinha.liderDe(seguidor.getUUID());
        return lider.getUUID().equals(idLider);
    }

    public static boolean ehSeguidor(Entity seguidor) {
        Entity veiculo = seguidor.getVehicle();
        return veiculo != null && ehPar(veiculo, seguidor);
    }

    public static boolean ehLiderComSeguidor(Entity lider) {
        if (!(lider instanceof Player)) return false;
        for (Entity passageiro : lider.getPassengers()) {
            if (ehPar(lider, passageiro)) return true;
        }
        return false;
    }

    public static void marcarSoltura(UUID seguidor) {
        SOLTANDO.add(seguidor);
    }

    public static boolean consumirSoltura(UUID seguidor) {
        return SOLTANDO.remove(seguidor);
    }

    public static void limparCliente() {
        ESTADOS_CLIENTE.clear();
    }

    public static void limparServidor() {
        ESTADOS_SERVIDOR.clear();
        SOLTANDO.clear();
    }

    public static float yaw(Player lider, float parcial) {
        Estado estado = mapa(lider).get(lider);
        if (estado == null || !estado.iniciado) return Mth.rotLerp(parcial, lider.yBodyRotO, lider.yBodyRot);
        return Mth.rotLerp(parcial, estado.yawAnterior, estado.yawAtual);
    }

    public static float[] yaws(Player lider) {
        Estado estado = mapa(lider).get(lider);
        if (estado == null || !estado.iniciado) return null;
        return new float[]{estado.yawAnterior, estado.yawAtual};
    }

    public static void posicionar(Player lider, Player seguidor, Entity.MoveFunction mover) {
        Estado estado = mapa(lider).computeIfAbsent(lider, chave -> new Estado());
        boolean novoTick = estado.ultimoTick != lider.tickCount;
        if (novoTick || estado.posicao == null || estado.base == null || estado.base.distanceToSqr(lider.position()) > 1.0E-8) {
            calcular(estado, lider, seguidor, novoTick);
            estado.ultimoTick = lider.tickCount;
        }

        Vec3 alvo = estado.posicao;
        mover.accept(seguidor, alvo.x, alvo.y, alvo.z);
        if (novoTick) {
            float passo = (float) Math.hypot(alvo.x - seguidor.xo, alvo.z - seguidor.zo);
            seguidor.walkAnimation.update(Math.min(passo * 4.0F, 1.0F), 0.4F);
        }
    }

    private static void calcular(Estado estado, Player lider, Player seguidor, boolean novoTick) {
        Vec3 base = lider.position();
        double salto = ConstantesMaozinha.SALTO_REINICIO_FORMACAO;
        if (estado.base != null && estado.base.distanceToSqr(base) > salto * salto) estado.iniciado = false;

        if (novoTick || !estado.iniciado) atualizarYaw(estado, lider);

        Vec3 aoLado = deslocamento(estado.yawAtual, false);
        if (novoTick) {
            if (espacoLivre(seguidor, base.add(aoLado))) {
                estado.ticksLivre++;
                estado.ticksBloqueado = 0;
            } else {
                estado.ticksBloqueado++;
                estado.ticksLivre = 0;
            }
            if (!estado.alternativa && estado.ticksBloqueado >= ConstantesMaozinha.TICKS_BLOQUEIO_PARA_TROCAR_FORMACAO) {
                estado.alternativa = true;
            } else if (estado.alternativa && estado.ticksLivre >= ConstantesMaozinha.TICKS_LIVRE_PARA_VOLTAR_FORMACAO) {
                estado.alternativa = false;
            }
        }

        Vec3 desejada = estado.alternativa ? deslocamento(estado.yawAtual, true) : aoLado;
        double raioAlvo = Math.hypot(desejada.x, desejada.z);
        double anguloAlvo = Math.atan2(desejada.z, desejada.x);

        if (!estado.iniciado) {
            Vec3 atual = seguidor.position().subtract(base);
            double raioAtual = Math.hypot(atual.x, atual.z);
            if (raioAtual > 0.3 && raioAtual < ConstantesMaozinha.RAIO_INICIAL_MAXIMO) {
                estado.raio = raioAtual;
                estado.angulo = Math.atan2(atual.z, atual.x);
            } else {
                estado.raio = raioAlvo;
                estado.angulo = anguloAlvo;
            }
        } else if (novoTick) {
            double limite = Math.toRadians(ConstantesMaozinha.GIRO_FORMACAO_MAXIMO_POR_TICK);
            double diferenca = Mth.wrapDegrees((float) Math.toDegrees(anguloAlvo - estado.angulo)) * Mth.DEG_TO_RAD;
            estado.angulo += Mth.clamp(diferenca, -limite, limite);
            estado.raio += (raioAlvo - estado.raio) * ConstantesMaozinha.AJUSTE_RAIO_FORMACAO_POR_TICK;
        }

        Vec3 alvo = base.add(Math.cos(estado.angulo) * estado.raio, 0.0, Math.sin(estado.angulo) * estado.raio);
        estado.posicao = resolver(lider, seguidor, alvo);
        estado.base = base;
        estado.iniciado = true;
    }

    private static void atualizarYaw(Estado estado, Player lider) {
        float corpo = lider.yBodyRot;
        if (!estado.iniciado) {
            estado.yawAnterior = corpo;
            estado.yawAtual = corpo;
            return;
        }
        estado.yawAnterior = estado.yawAtual;
        float diferenca = Mth.wrapDegrees(corpo - estado.yawAtual);
        double velocidade = Math.hypot(lider.getX() - lider.xo, lider.getZ() - lider.zo);
        if (velocidade < ConstantesMaozinha.VELOCIDADE_PARADO) {
            float zona = ConstantesMaozinha.ZONA_MORTA_PARADO_GRAUS;
            diferenca = Math.abs(diferenca) <= zona ? 0.0F : diferenca - Math.signum(diferenca) * zona;
        }
        estado.yawAtual += Mth.clamp(diferenca, -ConstantesMaozinha.GIRO_MAXIMO_POR_TICK, ConstantesMaozinha.GIRO_MAXIMO_POR_TICK);
    }

    private static Vec3 deslocamento(float yaw, boolean alternativa) {
        double radianos = yaw * Mth.DEG_TO_RAD;
        Vec3 frente = new Vec3(-Math.sin(radianos), 0.0, Math.cos(radianos));
        Vec3 esquerda = new Vec3(Math.cos(radianos), 0.0, Math.sin(radianos));
        if (alternativa) return frente.scale(-ConstantesMaozinha.AFASTAMENTO_ATRAS_ALTERNATIVO);
        return esquerda.scale(ConstantesMaozinha.AFASTAMENTO_LATERAL).subtract(frente.scale(ConstantesMaozinha.AFASTAMENTO_ATRAS));
    }

    private static boolean espacoLivre(Player seguidor, Vec3 ponto) {
        EntityDimensions dimensoes = seguidor.getDimensions(Pose.STANDING);
        if (seguidor.level().noCollision(seguidor, dimensoes.makeBoundingBox(ponto.add(0.0, 0.05, 0.0)))) return true;
        return seguidor.level().noCollision(seguidor, dimensoes.makeBoundingBox(ponto.add(0.0, 1.05, 0.0)));
    }

    private static Vec3 colidir(Player jogador, AABB caixa, Vec3 movimento) {
        return Entity.collideBoundingBox(jogador, movimento, caixa, jogador.level(), List.of());
    }

    private static double chao(Player jogador) {
        double queda = ConstantesMaozinha.QUEDA_MAXIMA_FORMACAO;
        double movido = colidir(jogador, jogador.getBoundingBox(), new Vec3(0.0, -queda, 0.0)).y;
        return movido > -queda + 1.0E-4 ? jogador.getY() + movido : Double.NaN;
    }

    private static Vec3 resolver(Player lider, Player seguidor, Vec3 alvo) {
        EntityDimensions dimensoes = seguidor.getDimensions(Pose.STANDING);
        Vec3 atual = seguidor.position();
        double reinicio = ConstantesMaozinha.DISTANCIA_REINICIO_SEGUIDOR;
        double desnivel = ConstantesMaozinha.SUBIDA_MAXIMA_FORMACAO + ConstantesMaozinha.QUEDA_MAXIMA_FORMACAO;
        boolean doLider = atual.distanceToSqr(lider.position()) > reinicio * reinicio || Math.abs(atual.y - lider.getY()) > desnivel;
        Vec3 inicio = doLider ? lider.position() : atual;

        Vec3 resultado = resolverDe(lider, seguidor, alvo, inicio, dimensoes);
        double desvio = Math.hypot(resultado.x - alvo.x, resultado.z - alvo.z);
        if (!doLider && (desvio > ConstantesMaozinha.DESVIO_MAXIMO_FORMACAO || Math.abs(resultado.y - lider.getY()) > desnivel)) {
            Vec3 alternativo = resolverDe(lider, seguidor, alvo, lider.position(), dimensoes);
            boolean melhorAltura = Math.abs(alternativo.y - lider.getY()) < Math.abs(resultado.y - lider.getY());
            if (Math.hypot(alternativo.x - alvo.x, alternativo.z - alvo.z) <= desvio && (melhorAltura || desvio > ConstantesMaozinha.DESVIO_MAXIMO_FORMACAO)) return alternativo;
        }
        return resultado;
    }

    private static Vec3 resolverDe(Player lider, Player seguidor, Vec3 alvo, Vec3 inicio, EntityDimensions dimensoes) {
        AABB caixa = dimensoes.makeBoundingBox(inicio);
        double subida = colidir(seguidor, caixa, new Vec3(0.0, ConstantesMaozinha.SUBIDA_MAXIMA_FORMACAO, 0.0)).y;
        AABB alta = caixa.move(0.0, subida, 0.0);
        Vec3 horizontal = colidir(seguidor, alta, new Vec3(alvo.x - inicio.x, 0.0, alvo.z - inicio.z));
        AABB movida = alta.move(horizontal.x, 0.0, horizontal.z);
        double x = inicio.x + horizontal.x;
        double z = inicio.z + horizontal.z;

        double alcance = subida + ConstantesMaozinha.QUEDA_MAXIMA_FORMACAO;
        double queda = colidir(seguidor, movida, new Vec3(0.0, -alcance, 0.0)).y;
        boolean temChao = queda > -alcance + 1.0E-4;
        double yChao = movida.minY + queda;
        double chaoLider = chao(lider);

        if (temChao && !Double.isNaN(chaoLider) && Math.abs(yChao - chaoLider) <= ConstantesMaozinha.DESNIVEL_MAXIMO_FORMACAO) {
            double ar = Math.max(0.0, lider.getY() - chaoLider);
            if (ar < 1.0E-4) return new Vec3(x, yChao, z);
            AABB noChao = dimensoes.makeBoundingBox(new Vec3(x, yChao, z));
            return new Vec3(x, yChao + colidir(seguidor, noChao, new Vec3(0.0, ar, 0.0)).y, z);
        }

        double vertical = colidir(seguidor, movida, new Vec3(0.0, lider.getY() - movida.minY, 0.0)).y;
        return new Vec3(x, movida.minY + vertical, z);
    }
}
