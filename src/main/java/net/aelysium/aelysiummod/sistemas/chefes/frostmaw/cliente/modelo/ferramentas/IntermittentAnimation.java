package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.modelo.ferramentas;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.IntermittentAnimatableEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.Random;

public class IntermittentAnimation<T extends Entity & IntermittentAnimatableEntity> {
    private final Random rand = new Random();

    private final T entity;

    private int timeRunning;
    private int prevTimeRunning;

    private int duration;

    private boolean isRunning;

    private int runDirection;

    private int timeIdle;

    private final int minIdleTime;

    private final int startProbability;

    private final boolean isOperator;

    private byte id;

    public IntermittentAnimation(T entity, int duration, int intervalDuration, int startPropbability, boolean isOperator) {
        this.entity = entity;
        this.duration = duration;
        this.minIdleTime = intervalDuration;
        this.startProbability = startPropbability;
        this.isOperator = isOperator;
        timeRunning = 0;
        isRunning = false;
        runDirection = -1;
    }

    public void setID(byte id) {
        this.id = id;
    }

    public void setDuration(int duration) {
        timeRunning = 0;
        this.duration = duration;
    }

    public int getTimeRunning() {
        return timeRunning;
    }

    public void setTimeRunning(int timeRunning) {
        this.timeRunning = timeRunning;

        if (this.timeRunning > duration) {
            this.timeRunning = duration;
        } else if (this.timeRunning < 0) {
            this.timeRunning = 0;
        }
    }

    public void resetTimeRunning() {
        timeRunning = 0;
    }

    public void update() {
        if (isRunning) {
            if (timeRunning < duration && timeRunning > 0) {
                timeRunning += runDirection;
            } else {
                if (timeRunning >= duration) {
                    timeRunning = duration;
                } else if (timeRunning <= 0) {
                    timeRunning = 0;
                }
                timeIdle = 0;
                isRunning = false;
            }
        } else if (isOperator) {
            if (timeIdle < minIdleTime) {
                timeIdle++;
            } else {
                if (rand.nextInt(startProbability) == 0) {
                    start();
                    entity.level().broadcastEntityEvent(entity, (byte) (entity.getOffsetEntityState() + id));
                }
            }
        }
    }

    public void start() {
        runDirection = -runDirection;
        timeRunning += runDirection;
        isRunning = true;
    }

    public void stop() {
        if (timeRunning > 0) {
            timeRunning--;
        } else {
            timeRunning = 0;
            isRunning = false;
            timeIdle = 0;
            runDirection = 1;
        }
    }

    public void stop(int timeDelta) {
        if (timeRunning - timeDelta > 0) {
            timeRunning -= timeDelta;
        } else {
            timeRunning = 0;
            isRunning = false;
            timeIdle = 0;
            runDirection = 1;
        }
    }

    public float getAnimationFraction() {
        return timeRunning / (float) duration;
    }

    public float getAnimationFraction(float partialTicks) {
        float interpTimer = prevTimeRunning + (timeRunning - prevTimeRunning) * partialTicks;
        return interpTimer / (float) duration;
    }

    public float getAnimationProgressSmooth() {
        if (timeRunning > 0) {
            if (timeRunning < duration) {
                return (float) (1.0D / (1.0D + Math.exp(4.0D - 8.0D * getAnimationFraction())));
            } else {
                return 1.0F;
            }
        }
        return 0.0F;
    }

    public float getAnimationProgressSteep() {
        return (float) (1.0D / (1.0D + Math.exp(6.0D - 12.0D * getAnimationFraction())));
    }

    public float getAnimationProgressSin() {
        return Mth.sin(1.57079632679F * getAnimationFraction());
    }

    public float getAnimationProgressSinSqrt() {
        float result = Mth.sin(1.57079632679F * getAnimationFraction());
        return result * result;
    }

    public float getAnimationProgressSinSqrt(float partialTicks) {
        float result = Mth.sin(1.57079632679F * getAnimationFraction(partialTicks));
        return result * result;
    }

    public float getAnimationProgressSinToTen() {
        return (float) Math.pow(Mth.sin(1.57079632679F * getAnimationFraction()), 10);
    }

    public float getAnimationProgressSinToTenWithoutReturn() {
        if (runDirection == -1) {
            return Mth.sin(1.57079632679F * getAnimationFraction()) * Mth.sin(1.57079632679F * getAnimationFraction());
        }
        return (float) Math.pow(Mth.sin(1.57079632679F * getAnimationFraction()), 10);
    }

    public float getAnimationProgressSinToTenWithoutReturn(float partialTicks) {
        if (runDirection == -1) {
            return Mth.sin(1.57079632679F * getAnimationFraction(partialTicks)) * Mth.sin(1.57079632679F * getAnimationFraction(partialTicks));
        }
        return (float) Math.pow(Mth.sin(1.57079632679F * getAnimationFraction(partialTicks)), 10);
    }

    public float getAnimationProgressSinPowerOf(int i) {
        return (float) Math.pow(Mth.sin(1.57079632679F * getAnimationFraction()), i);
    }

    public float getAnimationProgressPoly2() {
        float x = getAnimationFraction();
        float x2 = x * x;
        return x2 / (x2 + (1 - x) * (1 - x));
    }

    public float getAnimationProgressPoly3() {
        float x = getAnimationFraction();
        float x3 = x * x * x;
        return x3 / (x3 + (1 - x) * (1 - x) * (1 - x));
    }

    public float getAnimationProgressPolyN(int n) {
        double x = timeRunning / duration;
        double xi = Math.pow(x, n);
        return (float) (xi / (xi + Math.pow(1.0D - x, n)));
    }

    public float getAnimationProgressArcTan() {
        return (float) (0.5F + 0.49806510671F * Math.atan(3.14159265359D * (timeRunning / duration - 0.5D)));
    }

    public float getAnimationProgressTemporary() {
        float x = 6.28318530718F * getAnimationFraction();
        return 0.5F - 0.5F * Mth.cos(x + Mth.sin(x));
    }

    public float getAnimationProgressTemporaryFS() {
        float x = 3.14159265359F * getAnimationFraction();
        return Mth.sin(x + Mth.sin(x));
    }

    public float getAnimationProgressTemporaryInvesed() {
        float x = 6.28318530718F * getAnimationFraction();
        return 0.5F + 0.5F * Mth.cos(x + Mth.sin(x));
    }

    public void updatePrevTimer() {
        prevTimeRunning = timeRunning;
    }
}
