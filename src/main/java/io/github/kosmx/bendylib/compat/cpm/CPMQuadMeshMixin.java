package io.github.kosmx.bendylib.compat.cpm;

import com.tom.cpl.math.Mat3f;
import com.tom.cpl.math.Mat4f;
import com.tom.cpl.math.MatrixStack;
import com.tom.cpl.math.Vec3f;
import com.tom.cpl.math.Vec4f;
import com.tom.cpl.render.VertexBuffer;
import io.github.kosmx.bendylib.compat.cpmsupport.BendContext;
import io.github.kosmx.bendylib.compat.cpmsupport.CPMReflect;
import io.github.kosmx.bendylib.compat.cpmsupport.SubQuad;
import io.github.kosmx.bendylib.compat.cpmsupport.TempPos;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.tom.cpm.shared.model.render.BoxRender$QuadMesh", remap = false)
public abstract class CPMQuadMeshMixin {

    private static final int MAX_SEGMENTS = 24;

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, require = 0)
    private void bendylib$drawBent(MatrixStack matrixStackIn, VertexBuffer bufferIn, float red, float green, float blue, float alpha, CallbackInfo ci) {
        BendableCuboid bend = BendContext.get();
        if (bend == null) {
            return;
        }

        Object[] quads = CPMReflect.getQuadList(this);
        if (quads == null || quads.length == 0) {
            return;
        }
        ci.cancel();

        Mat4f cpmMatrix = matrixStackIn.getLast().getMatrix();
        Mat3f cpmNormalMatrix = matrixStackIn.getLast().getNormal();
        Vector3f bendAxisDir = new Vector3f(bend.getBendDirection().step());

        List<SubQuad> subQuads = new ArrayList<>();
        List<TempPos> allPoints = new ArrayList<>();
        for (Object quadObj : quads) {
            Object[] vertexObjs = CPMReflect.getVertexPositions(quadObj);
            if (vertexObjs.length != 4) {
                continue;
            }
            Vector3f[] corners = new Vector3f[4];
            float[] us = new float[4];
            float[] vs = new float[4];
            for (int i = 0; i < 4; i++) {
                VertexAccessor va = (VertexAccessor) vertexObjs[i];
                Vec3f p = va.bendylib$getPosition();
                corners[i] = new Vector3f(p.x, p.y, p.z);
                us[i] = va.bendylib$getTextureU();
                vs[i] = va.bendylib$getTextureV();
            }
            subdivide(corners, us, vs, bendAxisDir, subQuads, allPoints);
        }

        bend.applyBend(bend.getBendAxis(), bend.getBend(), consumer -> {
            for (TempPos p : allPoints) {
                consumer.accept(p);
            }
        });

        for (SubQuad sq : subQuads) {
            Vector3f[] bent = new Vector3f[4];
            for (int i = 0; i < 4; i++) {
                bent[i] = sq.points[i].getPos();
            }

            Vector3f localNormal = computeNormal(bent);
            Vec3f cpmNormal = new Vec3f(localNormal.x(), localNormal.y(), localNormal.z());
            cpmNormal.transform(cpmNormalMatrix);

            for (int i = 0; i < 4; i++) {
                Vector3f local = bent[i];
                Vec4f resolved = new Vec4f(local.x() / 16f, local.y() / 16f, local.z() / 16f, 1f);
                resolved.transform(cpmMatrix);

                bufferIn.pos(resolved.x, resolved.y, resolved.z)
                        .color(red, green, blue, alpha)
                        .tex(sq.u[i], sq.v[i])
                        .normal(cpmNormal.x, cpmNormal.y, cpmNormal.z)
                        .endVertex();
            }
        }
    }

    private static void subdivide(Vector3f[] c, float[] u, float[] v, Vector3f bendAxisDir, List<SubQuad> outQuads, List<TempPos> outPoints) {
        Vector3f edge01 = new Vector3f(c[1]).sub(c[0]);
        Vector3f edge03 = new Vector3f(c[3]).sub(c[0]);
        boolean splitOnS = Math.abs(edge01.dot(bendAxisDir)) >= Math.abs(edge03.dot(bendAxisDir));
        float extent = splitOnS ? Math.abs(edge01.dot(bendAxisDir)) : Math.abs(edge03.dot(bendAxisDir));
        int segments = Math.max(1, Math.min(MAX_SEGMENTS, Math.round(extent)));

        TempPos[] row0 = new TempPos[segments + 1];
        TempPos[] row1 = new TempPos[segments + 1];
        float[] row0u = new float[segments + 1];
        float[] row0v = new float[segments + 1];
        float[] row1u = new float[segments + 1];
        float[] row1v = new float[segments + 1];

        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            Vector3f p0;
            Vector3f p1;
            float u0;
            float v0v;
            float u1;
            float v1v;
            if (splitOnS) {
                p0 = lerp(c[0], c[1], t);
                p1 = lerp(c[3], c[2], t);
                u0 = lerp(u[0], u[1], t);
                v0v = lerp(v[0], v[1], t);
                u1 = lerp(u[3], u[2], t);
                v1v = lerp(v[3], v[2], t);
            } else {
                p0 = lerp(c[0], c[3], t);
                p1 = lerp(c[1], c[2], t);
                u0 = lerp(u[0], u[3], t);
                v0v = lerp(v[0], v[3], t);
                u1 = lerp(u[1], u[2], t);
                v1v = lerp(v[1], v[2], t);
            }
            row0[i] = new TempPos(p0);
            row1[i] = new TempPos(p1);
            row0u[i] = u0;
            row0v[i] = v0v;
            row1u[i] = u1;
            row1v[i] = v1v;
            outPoints.add(row0[i]);
            outPoints.add(row1[i]);
        }

        for (int i = 0; i < segments; i++) {
            TempPos[] points;
            float[] uu;
            float[] vv;
            if (splitOnS) {
                points = new TempPos[]{row0[i], row0[i + 1], row1[i + 1], row1[i]};
                uu = new float[]{row0u[i], row0u[i + 1], row1u[i + 1], row1u[i]};
                vv = new float[]{row0v[i], row0v[i + 1], row1v[i + 1], row1v[i]};
            } else {
                points = new TempPos[]{row0[i], row1[i], row1[i + 1], row0[i + 1]};
                uu = new float[]{row0u[i], row1u[i], row1u[i + 1], row0u[i + 1]};
                vv = new float[]{row0v[i], row1v[i], row1v[i + 1], row0v[i + 1]};
            }
            outQuads.add(new SubQuad(points, uu, vv));
        }
    }

    private static Vector3f lerp(Vector3f a, Vector3f b, float t) {
        return new Vector3f(a).lerp(b, t);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Vector3f computeNormal(Vector3f[] v) {
        Vector3f buf = new Vector3f(v[3]).mul(-1);
        Vector3f vecB = new Vector3f(v[1]).add(buf);
        buf = new Vector3f(v[2]).mul(-1);
        Vector3f vecA = new Vector3f(v[0]).add(buf);
        vecA.cross(vecB);
        if (vecA.lengthSquared() < 1.0E-8f) {
            return new Vector3f(Direction.NORTH.step());
        }
        return vecA.normalize();
    }
}
