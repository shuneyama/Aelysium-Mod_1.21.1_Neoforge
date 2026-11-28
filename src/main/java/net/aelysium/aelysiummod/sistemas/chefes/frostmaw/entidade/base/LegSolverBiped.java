package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base;

public final class LegSolverBiped extends LegSolver {
    public final Leg left, right;

    public LegSolverBiped(float forward, float side) {
        super(new Leg(forward, side), new Leg(forward, -side));
        this.left = this.legs[0];
        this.right = this.legs[1];
    }
}
