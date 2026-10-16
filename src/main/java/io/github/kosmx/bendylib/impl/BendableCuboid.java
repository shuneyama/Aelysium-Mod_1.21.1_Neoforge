package io.github.kosmx.bendylib.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.ICuboidBuilder;
import io.github.kosmx.bendylib.impl.accessors.DirectionMutator;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

public class BendableCuboid implements ICuboid, IBendable, IterableRePos {
    protected final Quad[] sides;
    protected final RememberingPos[] positions;
    protected Matrix4f lastPosMatrix;
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;
    protected final float fixX;
    protected final float fixY;
    protected final float fixZ;
    protected final Direction direction;
    protected final Plane basePlane;
    protected final Plane otherPlane;
    protected final float fullSize;

    private float bend, bendAxis;

    protected BendableCuboid(Quad[] sides, RememberingPos[] positions, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float fixX, float fixY, float fixZ, Direction direction, Plane basePlane, Plane otherPlane, float fullSize) {
        this.sides = sides;
        this.positions = positions;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.fixX = fixX;
        this.fixY = fixY;
        this.fixZ = fixZ;
        this.direction = direction;
        this.basePlane = basePlane;
        this.otherPlane = otherPlane;
        this.fullSize = fullSize;

        this.applyBend(0, 0);
    }

    public Matrix4f applyBend(float bendAxis, float bendValue){
        this.bend = bendValue; this.bendAxis = bendAxis;
        return this.lastPosMatrix = this.applyBend(bendAxis, bendValue, this);
    }

    @Override
    public Direction getBendDirection() {
        return this.direction;
    }

    @Override
    public float getBendX() {
        return fixX;
    }

    @Override
    public float getBendY() {
        return fixY;
    }

    @Override
    public float getBendZ() {
        return fixZ;
    }

    @Override
    public Plane getBasePlane() {
        return basePlane;
    }

    @Override
    public Plane getOtherSidePlane() {
        return otherPlane;
    }

    @Override
    public float bendHeight() {
        return fullSize;
    }

    @Override
    public void iteratePositions(Consumer<IPosWithOrigin> consumer){
        for(IPosWithOrigin pos:positions){
            consumer.accept(pos);
        }
    }

    public float getBend() {
        return bend;
    }

    public float getBendAxis() {
        return bendAxis;
    }

    public static class Builder implements ICuboidBuilder<BendableCuboid> {
        public Direction direction;

        public Builder setDirection(Direction d){
            this.direction = d;
            return this;
        }

        public BendableCuboid build(Data data, BuildableBendable builder){
            ArrayList<Quad> planes = new ArrayList<>();
            HashMap<Vector3f, RememberingPos> positions = new HashMap<>();
            float minX = data.x, minY = data.y, minZ = data.z, maxX = data.x + data.sizeX, maxY = data.y + data.sizeY, maxZ = data.z + data.sizeZ;
            float pminX = data.x - data.extraX, pminY = data.y - data.extraY, pminZ = data.z - data.extraZ, pmaxX = maxX + data.extraX, pmaxY = maxY + data.extraY, pmaxZ = maxZ + data.extraZ;
            if(data.mirror){
                float tmp = pminX;
                pminX = pmaxX;
                pmaxX = tmp;
            }

            Vector3f vertex1 = new Vector3f(pminX, pminY, pminZ);
            Vector3f vertex2 = new Vector3f(pmaxX, pminY, pminZ);
            Vector3f vertex3 = new Vector3f(pmaxX, pmaxY, pminZ);
            Vector3f vertex4 = new Vector3f(pminX, pmaxY, pminZ);
            Vector3f vertex5 = new Vector3f(pminX, pminY, pmaxZ);
            Vector3f vertex6 = new Vector3f(pmaxX, pminY, pmaxZ);
            Vector3f vertex7 = new Vector3f(pmaxX, pmaxY, pmaxZ);
            Vector3f vertex8 = new Vector3f(pminX, pmaxY, pmaxZ);

            int j = data.u;
            int k = (int) (data.u + data.sizeZ);
            int l = (int) (data.u + data.sizeZ + data.sizeX);
            int m = (int) (data.u + data.sizeZ + data.sizeX + data.sizeX);
            int n = (int) (data.u + data.sizeZ + data.sizeX + data.sizeZ);
            int o = (int) (data.u + data.sizeZ + data.sizeX + data.sizeZ + data.sizeX);
            int p = data.v;
            int q = (int) (data.v + data.sizeZ);
            int r = (int) (data.v + data.sizeZ + data.sizeY);
            createAndAddQuads(planes, positions, new Vector3f[]{vertex6, vertex5, vertex2}, k, p, l, q, data.textureWidth, data.textureHeight, data.mirror, data);
            createAndAddQuads(planes, positions, new Vector3f[]{vertex3, vertex4, vertex7}, l, q, m, p, data.textureWidth, data.textureHeight, data.mirror, data);
            createAndAddQuads(planes, positions, new Vector3f[]{vertex1, vertex5, vertex4}, j, q, k, r, data.textureWidth, data.textureHeight, data.mirror, data);
            createAndAddQuads(planes, positions, new Vector3f[]{vertex2, vertex1, vertex3}, k, q, l, r, data.textureWidth, data.textureHeight, data.mirror, data);
            createAndAddQuads(planes, positions, new Vector3f[]{vertex6, vertex2, vertex7}, l, q, n, r, data.textureWidth, data.textureHeight, data.mirror, data);
            createAndAddQuads(planes, positions, new Vector3f[]{vertex5, vertex6, vertex8}, n, q, o, r, data.textureWidth, data.textureHeight, data.mirror, data);

            Vector3f pivot = new Vector3f(0, 0, 0);
            if (data.pivot >= 0) {
                float size = direction.step().mul(maxX - minX, maxY - minY, maxZ - minZ).length();
                if (data.pivot <= size) {
                    pivot = direction.step().mul(size - (data.pivot * 2));
                    vertex7 = vertex7.sub(pivot);
                }
            }
            Plane aPlane = new Plane(new Vector3f(direction.step()), vertex7);
            Plane bPlane = new Plane(new Vector3f(direction.step()), vertex1);
            boolean bl = direction == Direction.UP || direction == Direction.SOUTH || direction == Direction.EAST;
            float fullSize = - direction.step().dot(vertex1) + direction.step().dot(vertex7);
            float bendX = ((float) data.sizeX + data.x + data.x - pivot.x())/2;
            float bendY = ((float) data.sizeY + data.y + data.y - pivot.y())/2;
            float bendZ = ((float) data.sizeZ + data.z + data.z - pivot.z())/2;
            return builder.build(planes.toArray(new Quad[0]), positions.values().toArray(new RememberingPos[0]), minX, minY, minZ, maxX, maxY, maxZ, bendX, bendY, bendZ, direction, bl ? aPlane : bPlane, bl ? bPlane : aPlane, fullSize);
        }

        public BendableCuboid build(Data data) {
            return build(data, BendableCuboid::new);
        }

        private void createAndAddQuads(Collection<Quad> quads, HashMap<Vector3f, RememberingPos> positions, Vector3f[] edges, int u1, int v1, int u2, int v2, float squishU, float squishV, boolean flip, Data data){
            int du = u2 < u1 ? 1 : -1;
            int dv = v1 < v2 ? 1 : -1;
            for(int localU = u2; localU != u1; localU += du){
                for(int localV = v1; localV != v2; localV += dv){
                    int localU2 = localU + du;
                    int localV2 = localV + dv;
                    RememberingPos rp0 = getOrCreate(positions, transformVector(new Vector3f(edges[0]), new Vector3f(edges[1]), new Vector3f(edges[2]), u2, v1, u1, v2, localU2, localV));
                    RememberingPos rp1 = getOrCreate(positions, transformVector(new Vector3f(edges[0]), new Vector3f(edges[1]), new Vector3f(edges[2]), u2, v1, u1, v2, localU2, localV2));
                    RememberingPos rp2 = getOrCreate(positions, transformVector(new Vector3f(edges[0]), new Vector3f(edges[1]), new Vector3f(edges[2]), u2, v1, u1, v2, localU, localV2));
                    RememberingPos rp3 = getOrCreate(positions, transformVector(new Vector3f(edges[0]), new Vector3f(edges[1]), new Vector3f(edges[2]), u2, v1, u1, v2, localU, localV));
                    quads.add(new Quad(new RememberingPos[]{rp3, rp0, rp1, rp2}, localU2, localV, localU, localV2, data.textureWidth, data.textureHeight, data.mirror));
                }
            }
        }

        Vector3f transformVector(Vector3f pos, Vector3f vectorU, Vector3f vectorV, int u1, int v1, int u2, int v2, int u, int v){
            vectorU.sub(pos);
            vectorU.mul(((float)u - u1)/(u2-u1));
            vectorV.sub(pos);
            vectorV.mul(((float)v - v1)/(v2-v1));
            pos.add(vectorU);
            pos.add(vectorV);
            return pos;
        }


        RememberingPos getOrCreate(HashMap<Vector3f, RememberingPos> positions, Vector3f pos){
            if(!positions.containsKey(pos)){
                positions.put(pos, new RememberingPos(pos));
            }
            return positions.get(pos);
        }

    }

    @FunctionalInterface
    public interface BuildableBendable {
        BendableCuboid build(Quad[] sides, RememberingPos[] positions, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float fixX, float fixY, float fixZ, Direction direction, Plane basePlane, Plane otherPlane, float fullSize);
    }

    @Deprecated
    public Matrix4f setRotationRad(float axisf, float value){
        return this.applyBend(axisf, value);
    }

    @Deprecated
    public Matrix4f setRotationDeg(float axis, float val){
        return this.applyBend(axis * 0.0174533f, val * 0.0174533f);
    }

    @Override
    public void render(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        for(Quad quad:sides){
            quad.render(matrices, vertexConsumer, light, overlay, color);
        }
    }

    @Override
    public void copyState(ICuboid other) {
        if(other instanceof BendableCuboid b){
            this.applyBend(b.bendAxis, b.bend);
        }
    }

    public Matrix4f getLastPosMatrix(){
        return new Matrix4f(this.lastPosMatrix);
    }

    public static class Quad{
        public final IVertex[] vertices;
        final float u1, u2, v1, v2, su, sv;

        public Quad(RememberingPos[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV, boolean flip){
            this.u1 = u1; this.u2 = u2; this.v1 = v1; this.v2 = v2; su = squishU; sv = squishV;
            float f = 0/squishU;
            float g = 0/squishV;
            this.vertices = new IVertex[4];
            this.vertices[0] = new RepositionableVertex(u2 / squishU - f, v1 / squishV + g, vertices[0]);
            this.vertices[1] = new RepositionableVertex(u1 / squishU + f, v1 / squishV + g, vertices[1]);
            this.vertices[2] = new RepositionableVertex(u1 / squishU + f, v2 / squishV - g, vertices[2]);
            this.vertices[3] = new RepositionableVertex(u2 / squishU - f, v2 / squishV - g, vertices[3]);
            if(flip){
                int i = vertices.length;

                for(int j = 0; j < i / 2; ++j) {
                    IVertex vertex = this.vertices[j];
                    this.vertices[j] = this.vertices[i - 1 - j];
                    this.vertices[i - 1 - j] = vertex;
                }
            }
        }
        public void render(PoseStack.Pose matrices, VertexConsumer vertexConsumer, int light, int overlay, int color){
            Vector3f direction = new Vector3f();
            matrices.transformNormal(this.getDirection(), direction);

            Matrix4f positionMatrix = matrices.pose();
            Vector3f transformed = new Vector3f();
            for (int i = 0; i != 4; ++i){
                IVertex vertex = this.vertices[i];
                Vector3f vertexPos = vertex.getPos();
                positionMatrix.transformPosition(vertexPos.x()/16f, vertexPos.y()/16f, vertexPos.z()/16f, transformed);
                vertexConsumer.addVertex(transformed.x(), transformed.y(), transformed.z(), color, vertex.getU(), vertex.getV(), overlay, light, direction.x(), direction.y(), direction.z());
            }
        }

        private Vector3f getDirection(){
            Vector3f buf = new Vector3f(vertices[3].getPos());
            buf.mul(-1);
            Vector3f vecB = new Vector3f(vertices[1].getPos());
            vecB.add(buf);
            buf = new Vector3f(vertices[2].getPos());
            buf.mul(-1);
            Vector3f vecA = new Vector3f(vertices[0].getPos());
            vecA.add(buf);
            vecA.cross(vecB);
            if(vecA.lengthSquared() < 1.0E-8f){
                return new Vector3f(Direction.NORTH.step());
            }
            return vecA.normalize();
        }

        @SuppressWarnings({"ConstantConditions"})
        private ModelPart.Polygon toModelPart_Polygon(){
            ModelPart.Polygon polygon = new ModelPart.Polygon(new ModelPart.Vertex[]{
                    vertices[0].toMojVertex(),
                    vertices[1].toMojVertex(),
                    vertices[2].toMojVertex(),
                    vertices[3].toMojVertex()
            }, u1, v1, u2, v2, su, sv, false, Direction.UP);
            ((DirectionMutator) polygon).setDirection(this.getDirection());
            return polygon;
        }
    }

    @Override
    public boolean disableAfterDraw() {
        return false;
    }

    @Override
    public List<ModelPart.Polygon> getQuads() {
        List<ModelPart.Polygon> sides = new ArrayList<>();
        for(Quad quad : this.sides){
            sides.add(quad.toModelPart_Polygon());
        }
        return sides;
    }
}
