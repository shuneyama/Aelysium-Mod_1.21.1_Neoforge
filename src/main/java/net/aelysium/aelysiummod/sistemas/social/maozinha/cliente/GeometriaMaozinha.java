package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class GeometriaMaozinha {

    public static Vec3 frente(float yaw) {
        double radianos = yaw * Mth.DEG_TO_RAD;
        return new Vec3(-Math.sin(radianos), 0.0, Math.cos(radianos));
    }

    public static Vec3 esquerda(float yaw) {
        double radianos = yaw * Mth.DEG_TO_RAD;
        return new Vec3(Math.cos(radianos), 0.0, Math.sin(radianos));
    }

    public static float corpo(Player jogador, float parcial) {
        return Mth.rotLerp(parcial, jogador.yBodyRotO, jogador.yBodyRot);
    }

    public static Vec3 velocidade(Player jogador) {
        return new Vec3(jogador.getX() - jogador.xo, 0.0, jogador.getZ() - jogador.zo);
    }

    public static Vec3 posicao(Player jogador, float parcial) {
        return jogador.getPosition(parcial);
    }

    public static Vec3 ombro(Player jogador, float parcial, boolean esquerdo) {
        return ombroEm(jogador, posicao(jogador, parcial), parcial, esquerdo);
    }

    public static Vec3 ombroEm(Player jogador, Vec3 posicao, float parcial, boolean esquerdo) {
        double altura = jogador.isCrouching() ? ConstantesMaozinha.ALTURA_OMBRO_AGACHADO : ConstantesMaozinha.ALTURA_OMBRO;
        double lado = esquerdo ? ConstantesMaozinha.DESLOCAMENTO_OMBRO : -ConstantesMaozinha.DESLOCAMENTO_OMBRO;
        return posicao
                .add(0.0, altura, 0.0)
                .add(esquerda(corpo(jogador, parcial)).scale(lado));
    }

    public static Vec3 encontroDasMaos(Player lider, Player seguidor, float parcial) {
        Vec3 ombroLider = ombro(lider, parcial, true);
        Vec3 ombroSeguidor = ombro(seguidor, parcial, false);
        Vec3 meio = ombroLider.add(ombroSeguidor).scale(0.5);
        double metade = ombroLider.distanceTo(ombroSeguidor) * 0.5;
        double braco = ConstantesMaozinha.COMPRIMENTO_BRACO;
        double queda = metade < braco ? Math.sqrt(braco * braco - metade * metade) : 0.0;

        double distancia = ombroLider.distanceTo(ombroSeguidor);
        double porDistancia = Mth.clamp((distancia - ConstantesMaozinha.LEVANTE_DISTANCIA_INICIO)
                / (ConstantesMaozinha.LEVANTE_DISTANCIA_PLENO - ConstantesMaozinha.LEVANTE_DISTANCIA_INICIO), 0.0, 1.0);
        double porVelocidade = Mth.clamp(velocidade(lider).length() / ConstantesMaozinha.LEVANTE_VELOCIDADE_PLENA, 0.0, 1.0);
        double levante = Math.min(ConstantesMaozinha.LEVANTE_MAXIMO,
                porDistancia * ConstantesMaozinha.LEVANTE_PESO_DISTANCIA + porVelocidade * ConstantesMaozinha.LEVANTE_PESO_VELOCIDADE);
        return meio.subtract(0.0, queda * (1.0 - levante), 0.0);
    }

    public static boolean estaNaFrente(Player jogador, Vec3 ponto, float parcial) {
        Vec3 posicao = posicao(jogador, parcial);
        double dx = ponto.x - posicao.x;
        double dz = ponto.z - posicao.z;
        if (dx * dx + dz * dz < 1.0E-4) return true;
        float direcao = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        return Math.abs(Mth.wrapDegrees(direcao - corpo(jogador, parcial))) <= ConstantesMaozinha.LIMITE_ANGULO_OFERTA_GRAUS;
    }

    public static float[] angulos(Vec3 direcao, float yawCorpo) {
        double x = direcao.dot(esquerda(yawCorpo));
        double y = -direcao.y;
        double z = -direcao.dot(frente(yawCorpo));
        double horizontal = Math.sqrt(x * x + z * z);

        float rotacaoX = (float) -Mth.atan2(horizontal, y);
        float rotacaoY = horizontal < 1.0E-4 ? 0.0F : (float) Mth.atan2(-x, -z);
        return new float[]{rotacaoX, rotacaoY};
    }
}
