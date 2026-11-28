package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.modelo.ferramentas;

import net.minecraft.util.Mth;

public class ControlledAnimation {
    private int timer;

    private int duration;

    private int timerChange;

    public ControlledAnimation(int d) {
        this.timer = 0;
        this.duration = d;
    }

    public void setDuration(int d) {
        this.timer = 0;
        this.duration = d;
    }

    public int getTimer() {
        return this.timer;
    }

    public void setTimer(int time) {
        this.timer = time;

        if (this.timer > this.duration) {
            this.timer = this.duration;
        } else if (this.timer < 0) {
            this.timer = 0;
        }
    }

    public void resetTimer() {
        this.timer = 0;
    }

    public void increaseTimer() {
        if (this.timer < this.duration) {
            this.timer++;
            this.timerChange = 1;
        }
    }

    public boolean canIncreaseTimer() {
        return this.timer < this.duration;
    }

    public void increaseTimer(int time) {
        int newTime = this.timer + time;
        if (newTime <= this.duration && newTime >= 0) {
            this.timer = newTime;
        } else {
            this.timer = newTime < 0 ? 0 : this.duration;
        }
    }

    public void decreaseTimer() {
        if (this.timer > 0.0D) {
            this.timer--;
            this.timerChange = -1;
        }
    }

    public boolean canDecreaseTimer() {
        return this.timer > 0.0D;
    }

    public void decreaseTimer(int time) {
        if (this.timer - time > 0.0D) {
            this.timer -= time;
        } else {
            this.timer = 0;
        }
    }

    public float getAnimationFraction() {
        return this.timer / (float) this.duration;
    }

    public float getAnimationProgressSmooth() {
        if (this.timer > 0.0D) {
            if (this.timer < this.duration) {
                return (float) (1.0D / (1.0D + Math.exp(4.0D - 8.0D * this.getAnimationFraction())));
            } else {
                return 1.0F;
            }
        }
        return 0.0F;
    }

    public float getAnimationProgressSteep() {
        return (float) (1.0D / (1.0D + Math.exp(6.0D - 12.0D * this.getAnimationFraction())));
    }

    public float getAnimationProgressSin() {
        return Mth.sin(1.57079632679F * this.getAnimationFraction());
    }

    public float getAnimationProgressSinSqrt() {
        float result = Mth.sin(1.57079632679F * this.getAnimationFraction());
        return result * result;
    }

    public float getAnimationProgressSinToTen() {
        return (float) Math.pow(Mth.sin(1.57079632679F * this.getAnimationFraction()), 10);
    }

    public float getAnimationProgressSinToTenWithoutReturn() {
        if (this.timerChange == -1) {
            return Mth.sin(1.57079632679F * this.getAnimationFraction()) * Mth.sin(1.57079632679F * this.getAnimationFraction());
        }
        return (float) Math.pow(Mth.sin(1.57079632679F * this.getAnimationFraction()), 10);
    }

    public float getAnimationProgressSinPowerOf(int i) {
        return (float) Math.pow(Mth.sin(1.57079632679F * this.getAnimationFraction()), i);
    }

    public float getAnimationProgressPoly2() {
        float x = this.getAnimationFraction();
        float x2 = x * x;
        return x2 / (x2 + (1 - x) * (1 - x));
    }

    public float getAnimationProgressPoly3() {
        float x = this.getAnimationFraction();
        float x3 = x * x * x;
        return x3 / (x3 + (1 - x) * (1 - x) * (1 - x));
    }

    public float getAnimationProgressPolyN(int n) {
        double x = this.getAnimationFraction();
        double xi = Math.pow(x, n);
        return (float) (xi / (xi + Math.pow(1.0D - x, n)));
    }

    public float getAnimationProgressArcTan() {
        return (float) (0.5F + 0.49806510671F * Math.atan(3.14159265359D * (this.getAnimationFraction() - 0.5D)));
    }

    public float getAnimationProgressTemporary() {
        float x = 6.28318530718F * this.getAnimationFraction();
        return 0.5F - 0.5F * Mth.cos(x + Mth.sin(x));
    }

    public float getAnimationProgressTemporaryFS() {
        float x = 3.14159265359F * this.getAnimationFraction();
        return Mth.sin(x + Mth.sin(x));
    }

    public float getAnimationProgressTemporaryInvesed() {
        float x = 6.28318530718F * this.getAnimationFraction();
        return 0.5F + 0.5F * Mth.cos(x + Mth.sin(x));
    }
}
