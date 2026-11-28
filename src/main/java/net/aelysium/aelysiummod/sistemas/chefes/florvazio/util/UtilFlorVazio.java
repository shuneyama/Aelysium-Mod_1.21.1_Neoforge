package net.aelysium.aelysiummod.sistemas.chefes.florvazio.util;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.dados.HistoricalData;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MathUtils;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.VecUtils;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntity;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.IEntityStats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class UtilFlorVazio {

    private static final Map<Player, HistoricalData<Vec3>> HISTORICO = new WeakHashMap<>();

    private UtilFlorVazio() {
    }

    public static void cappedHeal(IEntity iEntity, IEntityStats stats, List<Float> hpPercentRageModes, float healingStrength, Consumer<Float> heal) {
        if (iEntity.isAlive()) {
            float targetHealthRatio = MathUtils.roundedStep(stats.getHealth() / stats.getMaxHealth(), hpPercentRageModes, false);
            float healAmt = Mth.clamp(targetHealthRatio * stats.getMaxHealth() - stats.getHealth() - 1, 0f, healingStrength);
            if (healAmt > 0) heal.accept(healAmt);
        }
    }

    public static Vec3 approximatePlayerNextPosition(List<Vec3> previousPosition, Vec3 currentPos) {
        return previousPosition.stream()
                .map(vec3 -> VecUtils.planeProject(vec3.subtract(currentPos), VecUtils.yAxis))
                .reduce(Vec3::add).orElse(Vec3.ZERO)
                .multiply(-0.5, -0.5, -0.5).add(currentPos);
    }

    public static synchronized List<Vec3> getPlayerPositions(Player player) {
        HistoricalData<Vec3> dados = HISTORICO.get(player);
        return dados == null ? List.of(player.position()) : dados.getAll();
    }

    @SubscribeEvent
    public static void aoTickJogador(PlayerTickEvent.Post evento) {
        Player jogador = evento.getEntity();
        if (jogador.level().isClientSide) return;
        synchronized (UtilFlorVazio.class) {
            HistoricalData<Vec3> dados = HISTORICO.computeIfAbsent(jogador, j -> new HistoricalData<>(Vec3.ZERO, 10));
            Vec3 nova = jogador.position();
            if (dados.get(0).distanceToSqr(nova) > 5) dados.clear();
            dados.add(nova);
        }
    }
}
