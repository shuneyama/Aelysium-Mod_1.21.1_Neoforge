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

    public static Vec3 basePredita(Player lider) {
        return lider.position().add(velocidade(lider).scale(ConstantesMaozinha.TICKS_PREDICAO));
    }

    public static Vec3 baseVisual(Player lider, float parcial) {
        Vec3 posicao = lider.getPosition(parcial);
        if (lider.isLocalPlayer()) return posicao;
        return posicao.add(velocidade(lider).scale(ConstantesMaozinha.TICKS_PREDICAO));
    }

    public static Vec3 posicao(Player jogador, float parcial) {
        return jogador.getPosition(parcial).add(SuavizadorPosicaoMaozinha.deslocamento(jogador));
    }

    public static Vec3 ombro(Player jogador, float parcial, boolean esquerdo) {
        double altura = jogador.isCrouching() ? ConstantesMaozinha.ALTURA_OMBRO_AGACHADO : ConstantesMaozinha.ALTURA_OMBRO;
        double lado = esquerdo ? ConstantesMaozinha.DESLOCAMENTO_OMBRO : -ConstantesMaozinha.DESLOCAMENTO_OMBRO;
        return posicao(jogador, parcial)
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
        return meio.subtract(0.0, queda, 0.0);
    }

    public static Vec3 ancoraSeguidor(Player lider, Player seguidor) {
        float yaw = OrientacaoLiderMaozinha.yaw(lider, 1.0F);
        Vec3 base = basePredita(lider);

        Vec3 aoLado = base
                .add(esquerda(yaw).scale(ConstantesMaozinha.AFASTAMENTO_LATERAL))
                .subtract(frente(yaw).scale(ConstantesMaozinha.AFASTAMENTO_ATRAS));
        if (livre(seguidor, aoLado)) return aoLado;

        Vec3 atras = base.subtract(frente(yaw).scale(ConstantesMaozinha.AFASTAMENTO_ATRAS_ALTERNATIVO));
        if (livre(seguidor, atras)) return atras;

        return aoLado;
    }

    public static boolean livre(Player jogador, Vec3 posicao) {
        Vec3 deslocamento = posicao.subtract(jogador.position());
        return jogador.level().noCollision(jogador, jogador.getBoundingBox().move(deslocamento.x, deslocamento.y + 0.05, deslocamento.z));
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
