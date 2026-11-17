package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class EstadosAnimacaoFilhotes {

    private static final Map<Entity, Coelho> COELHOS = new WeakHashMap<>();
    private static final Map<Entity, Axolote> AXOLOTES = new WeakHashMap<>();

    private EstadosAnimacaoFilhotes() {
    }

    public static Coelho coelho(Rabbit coelho) {
        return COELHOS.computeIfAbsent(coelho, c -> new Coelho(c.getRandom().nextInt(40) + 180));
    }

    public static Axolote axolote(Axolotl axolote) {
        return AXOLOTES.computeIfAbsent(axolote, a -> new Axolote());
    }

    @SubscribeEvent
    public static void aoTick(EntityTickEvent.Post event) {
        Entity entidade = event.getEntity();
        if (!entidade.level().isClientSide()) return;
        if (entidade instanceof Rabbit coelho) {
            coelho(coelho).atualizar(coelho);
        } else if (entidade instanceof Axolotl axolote && axolote.isBaby()) {
            axolote(axolote).atualizar(axolote);
        }
    }

    public static final class Coelho {
        public final AnimationState pulo = new AnimationState();
        public final AnimationState inclinarCabeca = new AnimationState();
        private int tempoOcioso;

        private Coelho(int tempoOcioso) {
            this.tempoOcioso = tempoOcioso;
        }

        private void atualizar(Rabbit coelho) {
            boolean ocioso = tempoOcioso <= 0 && !coelho.isLeashed() && !coelho.isNoAi();
            if (ocioso) {
                tempoOcioso = coelho.getRandom().nextInt(40) + 180;
                inclinarCabeca.start(coelho.tickCount);
            } else if (coelho.getJumpCompletion(0.0F) > 0.0F) {
                pulo.startIfStopped(coelho.tickCount);
                inclinarCabeca.stop();
            } else {
                --tempoOcioso;
                pulo.stop();
            }
            if (coelho.isLeashed()) inclinarCabeca.stop();
        }
    }

    public static final class Axolote {
        public final AnimationState nadar = new AnimationState();
        public final AnimationState andar = new AnimationState();
        public final AnimationState andarSubmerso = new AnimationState();
        public final AnimationState paradoSubmerso = new AnimationState();
        public final AnimationState paradoSubmersoNoChao = new AnimationState();
        public final AnimationState paradoNoChao = new AnimationState();
        public final AnimationState fingirMorto = new AnimationState();
        private final List<AnimationState> todos = List.of(nadar, andar, andarSubmerso, paradoSubmerso, paradoSubmersoNoChao, paradoNoChao, fingirMorto);

        private void atualizar(Axolotl axolote) {
            boolean naAgua = axolote.isInWater();
            boolean noChao = axolote.onGround();
            boolean movendo = axolote.walkAnimation.isMoving() || axolote.getXRot() != axolote.xRotO || axolote.getYRot() != axolote.yRotO;
            if (axolote.isPlayingDead()) {
                solo(fingirMorto, axolote.tickCount);
            } else if (movendo) {
                if (naAgua && !noChao) {
                    solo(nadar, axolote.tickCount);
                } else if (!naAgua && noChao) {
                    solo(andar, axolote.tickCount);
                } else {
                    solo(andarSubmerso, axolote.tickCount);
                }
            } else if (naAgua && !noChao) {
                solo(paradoSubmerso, axolote.tickCount);
            } else if (naAgua) {
                solo(paradoSubmersoNoChao, axolote.tickCount);
            } else {
                solo(paradoNoChao, axolote.tickCount);
            }
        }

        private void solo(AnimationState iniciar, int tick) {
            for (AnimationState estado : todos) {
                if (estado == iniciar) {
                    estado.startIfStopped(tick);
                } else {
                    estado.stop();
                }
            }
        }
    }
}
