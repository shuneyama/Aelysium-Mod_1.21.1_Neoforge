package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorSpawnDificuldade {

    private static final String CHAVE_HORDA = "AelysiumHorda";

    @SubscribeEvent
    public static void aoEntrarNoMundo(EntityJoinLevelEvent evento) {
        if (!(evento.getLevel() instanceof ServerLevel mundo)) return;
        if (!(evento.getEntity() instanceof Mob mob) || !AplicadorDificuldade.elegivel(mob)) return;

        if (AplicadorDificuldade.marcado(mob)) {
            AplicadorDificuldade.aplicarComportamentos(mob, AplicadorDificuldade.nivelAplicado(mob));
            return;
        }
        if (evento.loadedFromDisk()) return;

        int nivel = DadosDificuldade.get(mundo.getServer()).getNivel();
        if (nivel <= 0) return;
        AplicadorDificuldade.aplicar(mob, nivel);
    }

    @SubscribeEvent
    public static void aoFinalizarSpawn(FinalizeSpawnEvent evento) {
        if (evento.getSpawnType() != MobSpawnType.NATURAL) return;
        if (!(evento.getLevel().getLevel() instanceof ServerLevel mundo)) return;

        Mob mob = evento.getEntity();
        if (!AplicadorDificuldade.elegivel(mob) || !ConfiguracaoDificuldade.podeFormarHorda(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()))) return;
        if (mob.getPersistentData().getBoolean(CHAVE_HORDA)) return;

        DadosDificuldade dados = DadosDificuldade.get(mundo.getServer());
        int nivel = dados.getNivel();
        if (nivel <= 0 || !dados.isHordas()) return;

        RandomSource aleatorio = mob.getRandom();
        if (aleatorio.nextDouble() >= PerfilDificuldade.chanceHorda(nivel)) return;

        AABB area = new AABB(evento.getX(), evento.getY(), evento.getZ(), evento.getX(), evento.getY(), evento.getZ()).inflate(PerfilDificuldade.raioLimiteHorda());
        if (mundo.getEntitiesOfClass(Monster.class, area).size() >= PerfilDificuldade.limiteMonstrosPorHorda()) return;

        int extras = 1 + aleatorio.nextInt(PerfilDificuldade.extrasMaximosHorda(nivel));
        for (int i = 0; i < extras; i++) {
            gerarMembroDaHorda(mundo, mob.getType(), evento.getX(), evento.getY(), evento.getZ(), aleatorio);
        }
    }

    private static void gerarMembroDaHorda(ServerLevel mundo, EntityType<?> tipo, double x, double y, double z, RandomSource aleatorio) {
        for (int tentativa = 0; tentativa < 3; tentativa++) {
            double posX = x + (aleatorio.nextDouble() - 0.5) * 2.0 * PerfilDificuldade.raioMembrosHorda();
            double posZ = z + (aleatorio.nextDouble() - 0.5) * 2.0 * PerfilDificuldade.raioMembrosHorda();

            if (!(tipo.create(mundo) instanceof Mob copia)) return;
            copia.moveTo(posX, y, posZ, aleatorio.nextFloat() * 360.0f, 0.0f);

            if (!copia.checkSpawnRules(mundo, MobSpawnType.NATURAL) || !copia.checkSpawnObstruction(mundo)) {
                copia.discard();
                continue;
            }

            copia.finalizeSpawn(mundo, mundo.getCurrentDifficultyAt(BlockPos.containing(posX, y, posZ)), MobSpawnType.REINFORCEMENT, null);
            copia.getPersistentData().putBoolean(CHAVE_HORDA, true);
            mundo.addFreshEntityWithPassengers(copia);
            return;
        }
    }
}
