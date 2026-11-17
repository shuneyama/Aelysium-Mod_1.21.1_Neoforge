package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class Creaking extends Monster {

    private static final EntityDataAccessor<Boolean> PODE_MOVER = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> CORACAO = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public int tempoAtaque;
    public int tempoTorcer;
    private int esperaTorcer;

    public Creaking(EntityType<? extends Creaking> tipo, Level mundo) {
        super(tipo, mundo);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder construtor) {
        super.defineSynchedData(construtor);
        construtor.define(PODE_MOVER, true);
        construtor.define(CORACAO, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new Ataque(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public boolean podeMover() {
        return this.entityData.get(PODE_MOVER);
    }

    public Optional<BlockPos> coracao() {
        return this.entityData.get(CORACAO);
    }

    public void setCoracao(BlockPos pos) {
        this.entityData.set(CORACAO, Optional.of(pos));
        this.restrictTo(pos, 32);
        this.setPersistenceRequired();
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || !podeMover();
    }

    private boolean observada() {
        for (Player jogador : this.level().players()) {
            if (!jogador.canBeSeenAsEnemy() || jogador.distanceToSqr(this) > 1024.0D) continue;
            if (naTela(jogador)) return true;
        }
        return false;
    }

    private boolean naTela(Player jogador) {
        Vec3 olho = jogador.getEyePosition();
        Vec3 frente = jogador.getViewVector(1.0F).normalize();
        Vec3 direita = frente.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (direita.lengthSqr() < 1.0E-6D) return true;
        direita = direita.normalize();
        Vec3 cima = direita.cross(frente).normalize();
        double[] alturas = {0.2D, 0.9D, 1.6D, 2.5D};
        double[] lados = {-0.45D, 0.0D, 0.45D};
        for (double altura : alturas) {
            for (double lado : lados) {
                Vec3 ponto = new Vec3(this.getX() + direita.x * lado, this.getY() + altura, this.getZ() + direita.z * lado);
                Vec3 v = ponto.subtract(olho);
                double z = v.dot(frente);
                if (z <= 0.05D) continue;
                if (Math.abs(v.dot(direita)) / z > 1.35D || Math.abs(v.dot(cima)) / z > 0.75D) continue;
                if (this.level().clip(new net.minecraft.world.level.ClipContext(olho, ponto, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, jogador)).getType() == net.minecraft.world.phys.HitResult.Type.MISS) return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (tempoAtaque > 0) tempoAtaque--;
        if (tempoTorcer > 0) tempoTorcer--;
        if (esperaTorcer > 0) esperaTorcer--;
        if (this.level().isClientSide) return;
        boolean observada = observada();
        if (podeMover() == observada) {
            this.entityData.set(PODE_MOVER, !observada);
            this.playSound((observada ? SonsJardimPalido.CREAKING_CONGELAR : SonsJardimPalido.CREAKING_DESCONGELAR).get(), 1.0F, 1.0F);
        }
        if (observada) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }
        if (this.tickCount % 10 == 0) verificarCoracao();
    }

    private void verificarCoracao() {
        Optional<BlockPos> pos = coracao();
        if (pos.isEmpty()) return;
        BlockPos p = pos.get();
        boolean valido = this.level().isLoaded(p)
                && this.level().getBlockEntity(p) instanceof CoracaoDoCreakingEntidade
                && this.level().getBlockState(p).getValue(CoracaoDoCreaking.ESTADO) == EstadoCoracao.AWAKE
                && this.distanceToSqr(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D) < 48.0D * 48.0D;
        if (valido) return;
        this.playSound(SonsJardimPalido.CREAKING_MORTE.get(), 1.0F, 1.0F);
        if (this.level() instanceof ServerLevel servidor) {
            servidor.sendParticles(ParticleTypes.POOF, getX(), getY(0.5D), getZ(), 12, 0.3D, 0.8D, 0.3D, 0.02D);
        }
        this.discard();
    }

    @Override
    public boolean doHurtTarget(Entity alvo) {
        this.level().broadcastEntityEvent(this, (byte) 4);
        this.playSound(SonsJardimPalido.CREAKING_ATAQUE.get(), 1.0F, 1.0F);
        return super.doHurtTarget(alvo);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.tempoAtaque = 15;
        } else if (id == 66) {
            this.tempoTorcer = 10;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hurt(DamageSource fonte, float quantidade) {
        if (this.level().isClientSide || coracao().isEmpty() || fonte.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(fonte, quantidade);
        }
        if (fonte.getEntity() instanceof Player && esperaTorcer <= 0) {
            esperaTorcer = 20;
            this.level().broadcastEntityEvent(this, (byte) 66);
            this.playSound(SonsJardimPalido.CREAKING_TORCER.get(), 1.0F, 1.0F);
            if (this.level().getBlockEntity(coracao().get()) instanceof CoracaoDoCreakingEntidade coracao) coracao.criaturaFerida();
        }
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distancia) {
        return coracao().isEmpty() && super.removeWhenFarAway(distancia);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag etiqueta) {
        super.addAdditionalSaveData(etiqueta);
        coracao().ifPresent(p -> {
            etiqueta.putInt("CoracaoX", p.getX());
            etiqueta.putInt("CoracaoY", p.getY());
            etiqueta.putInt("CoracaoZ", p.getZ());
        });
    }

    @Override
    public void readAdditionalSaveData(CompoundTag etiqueta) {
        super.readAdditionalSaveData(etiqueta);
        if (etiqueta.contains("CoracaoX")) setCoracao(new BlockPos(etiqueta.getInt("CoracaoX"), etiqueta.getInt("CoracaoY"), etiqueta.getInt("CoracaoZ")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return podeMover() ? SonsJardimPalido.CREAKING_AMBIENTE.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SonsJardimPalido.CREAKING_TORCER.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SonsJardimPalido.CREAKING_MORTE.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState estado) {
        this.playSound(SonsJardimPalido.CREAKING_PASSO.get(), 0.15F, 1.0F);
    }

    private static class Ataque extends MeleeAttackGoal {
        private final Creaking criatura;

        Ataque(Creaking criatura) {
            super(criatura, 1.0D, false);
            this.criatura = criatura;
        }

        @Override
        public boolean canUse() {
            return criatura.podeMover() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return criatura.podeMover() && super.canContinueToUse();
        }
    }
}
