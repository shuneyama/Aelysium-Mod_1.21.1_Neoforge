package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AtaqueChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.util.UtilFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.rede.*;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionWithCooldown;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.HitboxId;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventSeries;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class SpikeAction implements IActionWithCooldown {
    private final VoidBlossomEntity entity;
    private final EventScheduler eventScheduler;
    private final Supplier<Boolean> shouldCancel;

    private final List<Vec3> circlePoints = MathUtils.buildBlockCircle(2.0);

    public static final int indicatorDelay = 20;

    public SpikeAction(VoidBlossomEntity entity, EventScheduler eventScheduler, Supplier<Boolean> shouldCancel) {
        this.entity = entity;
        this.eventScheduler = eventScheduler;
        this.shouldCancel = shouldCancel;
    }

    @Override
    public int perform() {
        LivingEntity target = entity.getTarget();
        if (!(target instanceof ServerPlayer principal)) return 80;
        AtaqueChefe ataque = ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ESPINHOS);
        ServerLevel level = principal.serverLevel();

        Spikes riftBurst = new Spikes(
                entity,
                level,
                RegistroFlorVazio.VOID_BLOSSOM_SPIKE_INDICATOR.get(),
                indicatorDelay,
                eventScheduler,
                shouldCancel,
                ConfiguracaoChefes.FLOR_ESPINHOS
        );

        SoundUtils.playSound(level, entity.position(), RegistroFlorVazio.VOID_BLOSSOM_BURROW.get(), SoundSource.HOSTILE, 1.5f, 32, null);

        eventScheduler.addEvent(
                new EventSeries(
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Spike.getId()),
                                20,
                                1,
                                shouldCancel
                        ),
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Idle.getId()),
                                100
                        )
                )
        );

        for (LivingEntity alvo : entity.getAlvos().alvos(ataque.alvos())) {
            if (alvo instanceof ServerPlayer jogador) placeSpikes(jogador, riftBurst);
        }
        return ataque.recarga();
    }

    private void placeSpikes(ServerPlayer target, Spikes riftBurst) {
        for (int i = 0; i < 3; i++) {
            int timeBetweenRifts = 30;
            int initialDelay = 30;
            eventScheduler.addEvent(
                    new TimedEvent(
                            () -> {
                                Vec3 placement = UtilFlorVazio.approximatePlayerNextPosition(UtilFlorVazio.getPlayerPositions(target), target.position());
                                SoundUtils.playSound(target.serverLevel(), placement, RegistroFlorVazio.VOID_SPIKE_INDICATOR.get(), SoundSource.HOSTILE, 1.0f, 32, null);

                                List<BlockPos> successfulSpikes = circlePoints.stream()
                                        .flatMap(point -> riftBurst.tryPlaceRift(placement.add(point)).stream())
                                        .toList();

                                eventScheduler.addEvent(
                                        new TimedEvent(
                                                () -> {
                                                    SoundUtils.playSound(target.serverLevel(), placement, RegistroFlorVazio.VOID_BLOSSOM_SPIKE.get(), SoundSource.HOSTILE, 1.2f, 32, null);
                                                    RedeChefes.enviarPerto(new PacoteEspinhosFlor(entity.getId(), successfulSpikes), (ServerLevel) entity.level(), entity.position());
                                                },
                                                indicatorDelay,
                                                1,
                                                shouldCancel
                                        )
                                );
                            },
                            initialDelay + i * timeBetweenRifts,
                            1,
                            shouldCancel
                    )
            );
        }
    }
}
