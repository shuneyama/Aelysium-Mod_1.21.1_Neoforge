package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.rede.*;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionWithCooldown;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.HitboxId;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventScheduler;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.EventSeries;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.SoundUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlossomAction implements IActionWithCooldown {
    private final VoidBlossomEntity entity;
    private final EventScheduler eventScheduler;
    private final Supplier<Boolean> shouldCancel;

    private final List<Vec3> blossomPositions = Stream.of(
            VecUtils.xAxis,
            VecUtils.zAxis,
            VecUtils.xAxis.reverse(),
            VecUtils.zAxis.reverse(),
            VecUtils.xAxis.add(VecUtils.zAxis),
            VecUtils.xAxis.add(VecUtils.zAxis.reverse()),
            VecUtils.xAxis.reverse().add(VecUtils.zAxis),
            VecUtils.xAxis.reverse().add(VecUtils.zAxis.reverse())
    ).map(vec3 -> vec3.normalize().scale(15.0)).toList();

    public BlossomAction(VoidBlossomEntity entity, EventScheduler eventScheduler, Supplier<Boolean> shouldCancel) {
        this.entity = entity;
        this.eventScheduler = eventScheduler;
        this.shouldCancel = shouldCancel;
    }

    @Override
    public int perform() {
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) return 80;

        eventScheduler.addEvent(
                new EventSeries(
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.SpikeWave3.getId()),
                                20,
                                1,
                                shouldCancel
                        ),
                        new TimedEvent(
                                () -> entity.getEntityData().set(VoidBlossomEntity.hitbox, HitboxId.Idle.getId()),
                                80
                        )
                )
        );
        placeBlossoms((ServerLevel) level);
        return ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_FLORESCER).recarga();
    }

    private void placeBlossoms(ServerLevel level){
        List<BlockPos> positions = blossomPositions.stream()
                .map(pos -> BlockPos.containing(pos.add(entity.position())))
                .collect(Collectors.toList());
        Collections.shuffle(positions);

        float hpRatio = entity.getHealth() / entity.getMaxHealth();
        int protectedPositions;

        if (hpRatio < VoidBlossomEntity.hpMilestones.get(1))
            protectedPositions = 6;
        else if (hpRatio < VoidBlossomEntity.hpMilestones.get(2))
            protectedPositions = 3;
        else
            protectedPositions = 0;

        SoundUtils.playSound(level, entity.position(), RegistroFlorVazio.SPIKE_WAVE_INDICATOR.get(), SoundSource.HOSTILE, 2.0f, 0.7f, 64.0, null);

        for (int i = 0; i < 8; i++){
            int i1 = i;
            eventScheduler.addEvent(
                    new TimedEvent(
                            () -> {
                                BlockPos blossomPos = positions.get(i1);
                                level.setBlockAndUpdate(blossomPos, RegistroFlorVazio.MUSGO_VAZIO.get().defaultBlockState());
                                level.setBlockAndUpdate(blossomPos.above(), RegistroFlorVazio.VOID_BLOSSOM_BLOCK.get().defaultBlockState());
                                RedeChefes.enviarPerto(new PacoteColocarFlor(VecUtils.asVec3(blossomPos).add(VecUtils.unit.scale(0.5))), level, entity.position());
                                SoundUtils.playSound(level, VecUtils.asVec3(blossomPos), RegistroFlorVazio.PETAL_BLADE_SOM.get(), SoundSource.HOSTILE, 1.0f, SoundUtils.randomPitch(entity.getRandom()), 64, null);

                                if(i1 < protectedPositions) {
                                    for (int x = -1; x <= 1; x++) {
                                        for (int z = -1; z <= 1; z++) {
                                            for (int y = 0; y <= 2; y++) {
                                                if ((x != 0 || z != 0)) {
                                                    level.setBlockAndUpdate(blossomPos.offset(x, y, z), RegistroFlorVazio.VINE_WALL.get().defaultBlockState());
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            40 + i * 8,
                            1,
                            shouldCancel
                    )
            );
        }
    }
}
