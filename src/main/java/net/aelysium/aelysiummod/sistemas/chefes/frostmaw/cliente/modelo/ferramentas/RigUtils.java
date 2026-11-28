package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ferramentas;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RigUtils {

    public static Vector3f eulerAnglesXYZFromMatrix(Matrix4f matrix4f) {
        float thetaZ;
        float thetaY;
        float thetaX;
        if (matrix4f.m20() < 1f) {
            if (matrix4f.m20() > -1f) {
                thetaY = (float) Math.asin(matrix4f.m02());
                thetaX = (float) Math.atan2(-matrix4f.m12(), matrix4f.m22()) ;
                thetaZ = (float) Math.atan2(-matrix4f.m01(), matrix4f.m00());
            }
            else {
                thetaY = -(float) (Math.PI / 2f);
                thetaX = -(float) Math.atan2(matrix4f.m10(), matrix4f.m11());
                thetaZ = 0;
            }
        }
        else {
            thetaY = (float) (Math.PI / 2f);
            thetaX = (float) Math.atan2(matrix4f.m10(), matrix4f.m11());
            thetaZ = 0;
        }
        return new Vector3f(thetaX, thetaY, thetaZ);
    }
}
