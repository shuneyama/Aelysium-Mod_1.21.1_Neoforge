package io.github.kosmx.bendylib.impl;

import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface IBendable {

    default Matrix4f applyBend(float bendAxis, float bendValue, IterableRePos posSupplier){
        Vector3f axis = new Vector3f((float) Math.cos(bendAxis), 0, (float) Math.sin(bendAxis));
        Matrix3f matrix3f = new Matrix3f().set(getBendDirection().getRotation());
        matrix3f.transform(axis);
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();

        transformMatrix.translate(getBendX(), getBendY(), getBendZ());
        transformMatrix.rotate(new Quaternionf().rotationAxis(bendValue, axis.x, axis.y, axis.z));
        transformMatrix.translate(-getBendX(), -getBendY(), -getBendZ());

        Vector3f directionUnit;

        Plane basePlane = getBasePlane();
        Plane otherPlane = getOtherSidePlane();

        directionUnit = this.getBendDirection().step();
        directionUnit.cross(axis);
        Plane bendPlane = new Plane(directionUnit, new Vector3f(this.getBendX(), this.getBendY(), this.getBendZ()));
        float halfSize = bendHeight()/2;

        boolean bl = isBendInverted();

        posSupplier.iteratePositions(iPosWithOrigin -> {
            Vector3f newPos = iPosWithOrigin.getOriginalPos();
            float distFromBend = bl ? -bendPlane.distanceTo(newPos) : bendPlane.distanceTo(newPos);
            float distFromBase = basePlane.distanceTo(newPos);
            float distFromOther = otherPlane.distanceTo(newPos);
            double s = Math.tan(bendValue/2)*distFromBend;
            Vector3f x = getBendDirection().step();
            if(Math.abs(distFromBase) < Math.abs(distFromOther)){
                x.mul((float) (-distFromBase/halfSize*s));
                newPos.add(x);
                Vector4f reposVector = new Vector4f(newPos, 1.0F);
                transformMatrix.transform(reposVector);
                newPos = new Vector3f(reposVector.x, reposVector.y, reposVector.z);
            }
            else {
                x.mul((float) (-distFromOther/halfSize*s));
                newPos.add(x);
            }
            iPosWithOrigin.setPos(newPos);
        });

        return transformMatrix;
    }

    default boolean isBendInverted() {
        return getBendDirection() == Direction.UP || getBendDirection() == Direction.SOUTH || getBendDirection() == Direction.EAST;
    }

    Direction getBendDirection();

    float getBendX();
    float getBendY();
    float getBendZ();
    Plane getBasePlane();
    Plane getOtherSidePlane();

    default float bendHeight(){
        return this.getBasePlane().distanceTo(this.getOtherSidePlane());
    }

    final class Plane{
        public final Vector3f normal;
        private final float normDistance;

        public Plane(Vector3f normal, Vector3f position){
            this.normal = normal;
            this.normal.normalize();
            this.normDistance = -this.normal.dot(position);
        }

        private Plane(Vector3f normal, float normDistance) {
            this.normal = normal;
            this.normDistance = normDistance;
        }

        public Plane scaled(float scalar) {
            return new Plane(new Vector3f(this.normal), normDistance * scalar);
        }

        public float distanceTo(Vector3f pos){
            return normal.dot(pos) + normDistance;
        }

        public float distanceTo(Plane otherPlane){
            Vector3f tmp = new Vector3f(this.normal);
            tmp.cross(otherPlane.normal);
            if(tmp.dot(tmp) < 0.01){
                return this.normDistance + this.normal.dot(otherPlane.normal) * otherPlane.normDistance;
            }
            else {
                return 0;
            }
        }
    }
}
