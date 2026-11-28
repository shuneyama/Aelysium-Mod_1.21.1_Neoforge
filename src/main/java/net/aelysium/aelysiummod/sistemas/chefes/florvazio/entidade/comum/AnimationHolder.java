package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.comum;

import net.aelysium.aelysiummod.sistemas.chefes.base.evento.VelocidadeEventos;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityEventHandler;

import java.util.Map;

public class AnimationHolder implements IEntityEventHandler {
    private static final String IDLE = "idle";

    private final Map<Byte, Animation> animationStatusFlags;
    private final Byte stopAttackByte;
    private Animation nextAnimation;
    private boolean doIdleAnimation = true;
    private String animacaoUnica;
    private String animacaoRepetida;
    private double inicio;
    private double velocidade = 1.0;
    private double proximaVelocidade = 1.0;

    public AnimationHolder(Map<Byte, Animation> animationStatusFlags, Byte stopAttackByte) {
        this.animationStatusFlags = animationStatusFlags;
        this.stopAttackByte = stopAttackByte;
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (animationStatusFlags.containsKey(status)) {
            doIdleAnimation = false;
            nextAnimation = animationStatusFlags.get(status);
            proximaVelocidade = VelocidadeEventos.atual();
        }
        if (status == stopAttackByte) doIdleAnimation = true;
    }

    public void atualizar(double agora) {
        Animation animationData = nextAnimation;
        nextAnimation = null;

        if (animationData != null) iniciar(animationData.animationName(), animationData.idleAnimationName(), agora, proximaVelocidade);

        if (doIdleAnimation && (animacaoUnica != null || !IDLE.equals(animacaoRepetida))) iniciar(null, IDLE, agora, 1.0);
    }

    private void iniciar(String unica, String repetida, double agora, double velocidade) {
        this.velocidade = velocidade;
        animacaoUnica = unica;
        animacaoRepetida = repetida;
        inicio = agora;
    }

    public String animacaoUnica() {
        return animacaoUnica;
    }

    public String animacaoRepetida() {
        return animacaoRepetida;
    }

    public double inicio() {
        return inicio;
    }

    public double velocidade() {
        return velocidade;
    }

    public record Animation(String animationName, String idleAnimationName) {}
}
