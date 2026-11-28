package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AceleracaoChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AlvosChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.VelocidadeEventos;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.acao.IActionWithCooldown;
import net.aelysium.aelysiummod.sistemas.chefes.base.aleatorio.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.function.Supplier;

public class VoidBlossomMoveLogic implements IActionWithCooldown {
    private static final double DISTANCIA_CURTA = 21.0;
    private static final Map<Byte, Integer> DURACOES = Map.of(
            VoidBlossomAttacks.spikeAttack, 125,
            VoidBlossomAttacks.spikeWaveAttack, 105,
            VoidBlossomAttacks.sporeAttack, 55,
            VoidBlossomAttacks.bladeAttack, 105,
            VoidBlossomAttacks.blossomAction, 105
    );

    private final Map<Byte, IActionWithCooldown> actions;
    private final VoidBlossomEntity entity;
    private final Supplier<Boolean> doBlossom;
    private final AlvosChefe alvos;

    public VoidBlossomMoveLogic(Map<Byte, IActionWithCooldown> actions, VoidBlossomEntity entity, Supplier<Boolean> doBlossom, AlvosChefe alvos) {
        this.actions = actions;
        this.entity = entity;
        this.doBlossom = doBlossom;
        this.alvos = alvos;
    }

    @Override
    public int perform() {
        LivingEntity novo = alvos.escolher();
        if (novo != null) entity.setTarget(novo);
        LivingEntity target = entity.getTarget();
        if (target == null) return 20;

        float healthPercentage = entity.getHealth() / entity.getMaxHealth();
        boolean alguemPerto = alvos.candidatos().stream().anyMatch(jogador -> jogador.distanceTo(entity) <= DISTANCIA_CURTA) || target.distanceTo(entity) <= DISTANCIA_CURTA;
        double shortDistanceRate = alguemPerto ? 1.0 : 0.0;
        double spikeWeight = ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ESPINHOS).peso();
        double waveWeight = shortDistanceRate * ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ONDA).peso();
        double sporeWeight = healthPercentage < VoidBlossomEntity.hpMilestones.get(3) ? shortDistanceRate * ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_ESPOROS).peso() : 0.0;
        double bladeWeight = healthPercentage < VoidBlossomEntity.hpMilestones.get(2) || alvos.alguemVoando() ? ConfiguracaoChefes.ataque(ConfiguracaoChefes.FLOR_LAMINAS).peso() : 0.0;

        Byte moveByte;
        if (doBlossom.get())
            moveByte = VoidBlossomAttacks.blossomAction;
        else {
            WeightedRandom<Byte> random = new WeightedRandom<>();
            random.add(spikeWeight, VoidBlossomAttacks.spikeAttack);
            random.add(waveWeight, VoidBlossomAttacks.spikeWaveAttack);
            random.add(sporeWeight, VoidBlossomAttacks.sporeAttack);
            random.add(bladeWeight, VoidBlossomAttacks.bladeAttack);
            moveByte = random.next();
        }
        if (moveByte == null) return 20;

        IActionWithCooldown action = actions.get(moveByte);
        if (action == null) throw new IllegalArgumentException(moveByte + " action not registered as an attack");
        entity.level().broadcastEntityEvent(entity, moveByte);
        double velocidade = AceleracaoChefe.multiplicador(entity, ConfiguracaoChefes.FLOR);
        int recarga = VelocidadeEventos.executar(velocidade, action::perform);
        if (moveByte != VoidBlossomAttacks.blossomAction) entity.registrarAtaque();
        int duracao = (int) Math.ceil(DURACOES.getOrDefault(moveByte, 0) / velocidade);
        return AceleracaoChefe.recarga(entity, ConfiguracaoChefes.FLOR, recarga, duracao);
    }
}
