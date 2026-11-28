package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityTick;
import net.aelysium.aelysiummod.sistemas.chefes.base.particula.ClientParticleBuilder;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.RandomUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.Vec3Colors;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VoidBlossomClientSpikeHandler implements IEntityTick<Level> {
    private final LinkedHashMap<BlockPos, VoidBlossomClientSpikeHandler.Spike> spikes = new LinkedHashMap<>();
    private final ClientParticleBuilder spikeParticleFactory = new ClientParticleBuilder(RegistroFlorVazio.SPARKLES.get())
            .age(RandomUtils.range(10, 15))
            .color(Vec3Colors.VOID_PURPLE)
            .colorVariation(0.25)
            .brightness(RegistroFlorVazio.BRILHO_TOTAL);
    private final int maxAge = 10;

    public Map<BlockPos, VoidBlossomClientSpikeHandler.Spike> getSpikes(){
        return spikes;
    }

    public void addSpike(BlockPos pos){
        Vec3 center = VecUtils.asVec3(pos).add(VecUtils.unit.scale(0.5));
        double spikeHeight = 4.0 + RandomUtils.randDouble(0.5);
        spikes.put(pos, new Spike(
                center,
                RandomUtils.randVec().add(VecUtils.yAxis.scale(spikeHeight)).normalize(),
                (float) spikeHeight,
                maxAge,
                0
        ));
    }

    @Override
    public void tick(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();

        for (var kv : spikes.entrySet()){
            Spike oldSpike = kv.getValue();
            int newAge = oldSpike.age() + 1;
            Spike newSpike = new Spike(oldSpike.pos(), oldSpike.offset(), oldSpike.height(), oldSpike.maxAge(), newAge);
            spikes.put(kv.getKey(), newSpike);

            if (newAge == maxAge - 5) {
                spikeParticleFactory.build(
                        VecUtils.asVec3(kv.getKey()).add(RandomUtils.randVec().add(VecUtils.yAxis.scale(2.5 + RandomUtils.randDouble(2.0)))),
                        Vec3.ZERO
                );
            }

            if (newAge >= maxAge)
                toRemove.add(kv.getKey());
        }

        for (BlockPos removal : toRemove)
            spikes.remove(removal);
    }

    public record Spike(Vec3 pos, Vec3 offset, float height, int maxAge, int age){}
}
