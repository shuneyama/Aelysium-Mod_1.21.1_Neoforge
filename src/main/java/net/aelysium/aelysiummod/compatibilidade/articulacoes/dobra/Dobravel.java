package net.aelysium.aelysiummod.compatibilidade.articulacoes.dobra;

import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface Dobravel {

    default Matrix4f aplicarDobra(float eixoDobra, float valorDobra, IteravelPosicoes posicoes) {
        Vector3f eixo = new Vector3f((float) Math.cos(eixoDobra), 0, (float) Math.sin(eixoDobra));
        new Matrix3f().set(direcaoDobra().getRotation()).transform(eixo);

        float centroX = centroDobraX();
        float centroY = centroDobraY();
        float centroZ = centroDobraZ();

        Matrix4f transformacao = new Matrix4f()
                .translate(centroX, centroY, centroZ)
                .rotate(new Quaternionf().rotationAxis(valorDobra, eixo.x, eixo.y, eixo.z))
                .translate(-centroX, -centroY, -centroZ);

        Plano base = planoBase();
        Plano oposto = planoOposto();
        Plano planoDobra = new Plano(direcaoDobra().step().cross(eixo), new Vector3f(centroX, centroY, centroZ));
        float metadeAltura = alturaDobra() / 2;
        boolean invertida = dobraInvertida();

        posicoes.percorrerPosicoes(posicao -> {
            Vector3f nova = posicao.posicaoOriginal();
            float distanciaDobra = invertida ? -planoDobra.distanciaAte(nova) : planoDobra.distanciaAte(nova);
            float distanciaBase = base.distanciaAte(nova);
            float distanciaOposto = oposto.distanciaAte(nova);
            double deslocamento = Math.tan(valorDobra / 2) * distanciaDobra;
            Vector3f direcao = direcaoDobra().step();
            if (Math.abs(distanciaBase) < Math.abs(distanciaOposto)) {
                direcao.mul((float) (-distanciaBase / metadeAltura * deslocamento));
                nova.add(direcao);
                Vector4f reposicionada = transformacao.transform(new Vector4f(nova, 1.0F));
                nova = new Vector3f(reposicionada.x, reposicionada.y, reposicionada.z);
            } else {
                direcao.mul((float) (-distanciaOposto / metadeAltura * deslocamento));
                nova.add(direcao);
            }
            posicao.definirPosicao(nova);
        });

        return transformacao;
    }

    default boolean dobraInvertida() {
        Direction direcao = direcaoDobra();
        return direcao == Direction.UP || direcao == Direction.SOUTH || direcao == Direction.EAST;
    }

    Direction direcaoDobra();

    float centroDobraX();

    float centroDobraY();

    float centroDobraZ();

    Plano planoBase();

    Plano planoOposto();

    float alturaDobra();
}
