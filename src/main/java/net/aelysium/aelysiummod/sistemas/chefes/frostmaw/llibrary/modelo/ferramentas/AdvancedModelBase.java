package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.modelo.ferramentas;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public abstract class AdvancedModelBase<T extends Entity> extends BasicModelBase<T> {
    private float movementScale = 1.0F;

    protected AdvancedModelBase() {
        super();
    }

    protected AdvancedModelBase(Function<ResourceLocation, RenderType> renderTypeFunction) {
        super(renderTypeFunction);
    }

    public void updateDefaultPose() {
        this.boxList.stream().filter(modelRenderer -> modelRenderer instanceof AdvancedModelRenderer).forEach(modelRenderer -> {
            AdvancedModelRenderer advancedModelRenderer = (AdvancedModelRenderer) modelRenderer;
            advancedModelRenderer.updateDefaultPose();
        });
    }

    public void resetToDefaultPose() {
        this.boxList.stream().filter(modelRenderer -> modelRenderer instanceof AdvancedModelRenderer).forEach(modelRenderer -> {
            AdvancedModelRenderer advancedModelRenderer = (AdvancedModelRenderer) modelRenderer;
            advancedModelRenderer.resetToDefaultPose();
        });
    }

    public void faceTarget(float yaw, float pitch, float rotationDivisor, AdvancedModelRenderer... boxes) {
        float actualRotationDivisor = rotationDivisor * boxes.length;
        float yawAmount = (float) (Math.toRadians(yaw) / actualRotationDivisor);
        float pitchAmount = (float) (Math.toRadians(pitch) / actualRotationDivisor);
        for (AdvancedModelRenderer box : boxes) {
            box.rotateAngleY += yawAmount;
            box.rotateAngleX += pitchAmount;
        }
    }

    public void chainSwing(AdvancedModelRenderer[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
        float offset = this.calculateChainOffset(rootOffset, boxes);
        for (int index = 0; index < boxes.length; index++) {
            boxes[index].rotateAngleY += this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
        }
    }

    public void chainWave(AdvancedModelRenderer[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
        float offset = this.calculateChainOffset(rootOffset, boxes);
        for (int index = 0; index < boxes.length; index++) {
            boxes[index].rotateAngleX += this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
        }
    }

    public void chainFlap(AdvancedModelRenderer[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
        float offset = this.calculateChainOffset(rootOffset, boxes);
        for (int index = 0; index < boxes.length; index++) {
            boxes[index].rotateAngleZ += this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
        }
    }

    private float calculateChainRotation(float speed, float degree, float swing, float swingAmount, float offset, int boxIndex) {
        return Mth.cos(swing * (speed * this.movementScale) + offset * boxIndex) * swingAmount * (degree * this.movementScale);
    }

    private float calculateChainOffset(double rootOffset, AdvancedModelRenderer... boxes) {
        return (float) ((rootOffset * Math.PI) / (2 * boxes.length));
    }

    public float getMovementScale() {
        return this.movementScale;
    }

    public void setMovementScale(float movementScale) {
        this.movementScale = movementScale;
    }

    public void walk(AdvancedModelRenderer box, float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
        box.walk(speed, degree, invert, offset, weight, walk, walkAmount);
    }

    public void flap(AdvancedModelRenderer box, float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
        box.flap(speed, degree, invert, offset, weight, flap, flapAmount);
    }

    public void swing(AdvancedModelRenderer box, float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
        box.swing(speed, degree, invert, offset, weight, swing, swingAmount);
    }

    public void bob(AdvancedModelRenderer box, float speed, float degree, boolean bounce, float f, float f1) {
        box.bob(speed, degree, bounce, f, f1);
    }

    public float moveBox(float speed, float degree, boolean bounce, float f, float f1) {
        if (bounce) {
            return -Mth.abs((Mth.sin(f * speed) * f1 * degree));
        } else {
            return Mth.sin(f * speed) * f1 * degree - f1 * degree;
        }
    }
}
