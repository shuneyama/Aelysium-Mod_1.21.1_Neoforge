package net.aelysium.aelysiummod.compatibilidade.articulacoes.cpm;

import com.tom.cpl.math.Mat4f;
import org.joml.Matrix4f;

public final class ConversaoMatriz {

    public static Matrix4f paraJoml(Mat4f matriz) {
        return new Matrix4f().set(matriz.toArray()).transpose();
    }

    private ConversaoMatriz() {
    }
}
