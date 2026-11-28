package net.aelysium.aelysiummod.sistemas.chefes.florvazio.cliente.render;

import net.aelysium.aelysiummod.sistemas.chefes.base.modelo.OssoModelo;
import org.joml.Vector4f;

@FunctionalInterface
public interface IBoneLight {
    int getLightForBone(OssoModelo bone, int packedLight);

    default Vector4f getColorForBone(OssoModelo bone, Vector4f rgbaColor) {
        return rgbaColor;
    }

    int fullbright = 15728880;
}
