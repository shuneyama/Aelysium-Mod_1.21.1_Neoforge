package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ferramentas;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.modelo.Transform;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.modelo.ferramentas.BasicModelRenderer;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.Animation;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.IAnimatedEntity;
import net.minecraft.util.Mth;

import java.util.HashMap;

public class MMModelAnimator {
    private int tempTick;
    private int prevTempTick;
    private float delta;
    private boolean correctAnimation;
    private IAnimatedEntity entity;
    private final HashMap<BasicModelRenderer, Transform> transformMap;
    private final HashMap<BasicModelRenderer, Transform> prevTransformMap;

    private MMModelAnimator() {
        this.tempTick = 0;
        this.correctAnimation = false;
        this.transformMap = new HashMap<>();
        this.prevTransformMap = new HashMap<>();
    }

    public static MMModelAnimator create() {
        return new MMModelAnimator();
    }

    public IAnimatedEntity getEntity() {
        return this.entity;
    }

    public void update(IAnimatedEntity entity, float delta) {
        this.tempTick = this.prevTempTick = 0;
        this.delta = delta;
        this.correctAnimation = false;
        this.entity = entity;
        this.transformMap.clear();
        this.prevTransformMap.clear();
    }

    public boolean setAnimation(Animation animation) {
        this.tempTick = this.prevTempTick = 0;
        if (entity != null) this.correctAnimation = this.entity.getAnimation() == animation;
        return this.correctAnimation;
    }

    public void startKeyframe(int duration) {
        if (!this.correctAnimation) {
            return;
        }
        this.prevTempTick = this.tempTick;
        this.tempTick += duration;
    }

    public void setStaticKeyframe(int duration) {
        this.startKeyframe(duration);
        this.endKeyframe(true);
    }

    public void resetKeyframe(int duration) {
        this.startKeyframe(duration);
        this.endKeyframe();
    }

    public void rotate(BasicModelRenderer box, float x, float y, float z) {
        if (!this.correctAnimation) {
            return;
        }
        this.getTransform(box).addRotation(x, y, z);
    }

    public void move(BasicModelRenderer box, float x, float y, float z) {
        if (!this.correctAnimation) {
            return;
        }
        this.getTransform(box).addOffset(x, y, z);
    }

    private Transform getTransform(BasicModelRenderer box) {
        return this.transformMap.computeIfAbsent(box, b -> new Transform());
    }

    public void endKeyframe() {
        this.endKeyframe(false);
    }

    private void endKeyframe(boolean stationary) {
        if (!this.correctAnimation) {
            return;
        }
        int animationTick = this.entity.getAnimationTick();

        if (animationTick >= this.prevTempTick && animationTick < this.tempTick) {
            if (stationary) {
                for (BasicModelRenderer box : this.prevTransformMap.keySet()) {
                    Transform transform = this.prevTransformMap.get(box);
                    box.rotateAngleX += transform.getRotationX();
                    box.rotateAngleY += transform.getRotationY();
                    box.rotateAngleZ += transform.getRotationZ();
                    box.rotationPointX += transform.getOffsetX();
                    box.rotationPointY += transform.getOffsetY();
                    box.rotationPointZ += transform.getOffsetZ();
                }
            } else {
                float tick = (animationTick - this.prevTempTick + this.delta) / (this.tempTick - this.prevTempTick);
                float inc = Mth.sin((float) (tick * Math.PI / 2.0F)), dec = 1.0F - inc;
                for (BasicModelRenderer box : this.prevTransformMap.keySet()) {
                    Transform transform = this.prevTransformMap.get(box);
                    box.rotateAngleX += dec * transform.getRotationX();
                    box.rotateAngleY += dec * transform.getRotationY();
                    box.rotateAngleZ += dec * transform.getRotationZ();
                    box.rotationPointX += dec * transform.getOffsetX();
                    box.rotationPointY += dec * transform.getOffsetY();
                    box.rotationPointZ += dec * transform.getOffsetZ();
                }
                for (BasicModelRenderer box : this.transformMap.keySet()) {
                    Transform transform = this.transformMap.get(box);
                    box.rotateAngleX += inc * transform.getRotationX();
                    box.rotateAngleY += inc * transform.getRotationY();
                    box.rotateAngleZ += inc * transform.getRotationZ();
                    box.rotationPointX += inc * transform.getOffsetX();
                    box.rotationPointY += inc * transform.getOffsetY();
                    box.rotationPointZ += inc * transform.getOffsetZ();
                }
            }
        }

        if (!stationary) {
            this.prevTransformMap.clear();
            this.prevTransformMap.putAll(this.transformMap);
            this.transformMap.clear();
        }
    }
}
