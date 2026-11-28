package net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AceleracaoChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AlvosChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ChaoArmadilha;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ChefeAelysium;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.VelocidadeEventos;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.objetivos.ActionGoal;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.objetivos.CompositeGoal;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.ia.objetivos.FindTargetGoal;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.comum.AnimationHolder;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.HitboxId;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.NetworkedHitboxManager;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.hitbox.VoidBlossomHitboxes;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.dano.CompositeDamageHandler;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.dano.StagedDamageHandler;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.BaseEntity;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.CompositeEntityEventHandler;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.entidade.util.CompositeEntityTick;
import net.aelysium.aelysiummod.sistemas.chefes.base.dados.BooleanFlag;
import net.aelysium.aelysiummod.sistemas.chefes.base.evento.TimedEvent;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.EntityBounds;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MultipartAwareEntity;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.util.CompoundOrientedBox;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.MobUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class VoidBlossomEntity extends BaseEntity implements MultipartAwareEntity, ChefeAelysium {
    private final AnimationHolder animationHolder;
    private final AlvosChefe alvos;
    private int ataquesMusgoFofo = 0;
    private final NetworkedHitboxManager hitboxHelper;

    public final VoidBlossomClientSpikeHandler clientSpikeHandler;
    public static final List<Float> hpMilestones = List.of(0.0f, 0.25f, 0.5f, 0.75f, 1.0f);
    public static final EntityDataAccessor<Byte> hitbox = SynchedEntityData.defineId(VoidBlossomEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Float> VELOCIDADE_ATAQUE = SynchedEntityData.defineId(VoidBlossomEntity.class, EntityDataSerializers.FLOAT);

    public VoidBlossomEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        noCulling = true;

        this.animationHolder = new AnimationHolder(
                Map.of(
                VoidBlossomAttacks.spikeAttack, new AnimationHolder.Animation("spike", "idle"),
                VoidBlossomAttacks.spikeWaveAttack, new AnimationHolder.Animation("spike_wave", "idle"),
                VoidBlossomAttacks.sporeAttack, new AnimationHolder.Animation("spore", "idle"),
                VoidBlossomAttacks.bladeAttack, new AnimationHolder.Animation("leaf_blade", "idle"),
                VoidBlossomAttacks.blossomAction, new AnimationHolder.Animation("blossom", "idle"),
                VoidBlossomAttacks.spawnAction, new AnimationHolder.Animation("spawn", "idle"),
                (byte) 3, new AnimationHolder.Animation("death", "idle")),
                VoidBlossomAttacks.stopAttackAnimation);

        VoidBlossomHitboxes hitboxes = new VoidBlossomHitboxes(this);
        this.hitboxHelper = new NetworkedHitboxManager(this, hitboxes.getMap(), hitbox);
        entityEventHandler = new CompositeEntityEventHandler(animationHolder, new ClientSporeEffectHandler(this, preTickEvents), new ClientDeathEffectHandler(this, preTickEvents));
        this.alvos = new AlvosChefe(this);

        BooleanFlag shouldSpawnBlossoms = new BooleanFlag();
        StagedDamageHandler hpDetector = new StagedDamageHandler(hpMilestones, shouldSpawnBlossoms::flag);

        bossBar = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_12);

        this.clientSpikeHandler = new VoidBlossomClientSpikeHandler();
        clientTick = clientSpikeHandler;

        serverTick = new CompositeEntityTick<>(
                new LightBlockPlacer(this),
                new VoidBlossomSpikeTick(this),
                hitboxes.getTickers(),
                new CappedHeal(this, hpMilestones, ConfiguracaoChefes.FLOR)
        );
        deathServerTick = new CompositeEntityTick<>(
                new LightBlockRemover(this),
                new VoidBlossomDropExpDeathTick(this, preTickEvents, RegistroFlorVazio.EXPERIENCIA)
        );

        damageHandler = new CompositeDamageHandler(hpDetector, hitboxes.getDamageHandlers());

        if (!level.isClientSide() && level instanceof ServerLevel){
            VoidBlossomAttacks attackHandler = new VoidBlossomAttacks(this, preTickEvents, shouldSpawnBlossoms::getAndReset, alvos);
            goalSelector.addGoal(2, new CompositeGoal());
            goalSelector.addGoal(1, new CompositeGoal(attackHandler.buildAttackGoal(), new ActionGoal(this::canContinueAttack, null, this::lookAtTarget, null, null)));

            targetSelector.addGoal(2, new FindTargetGoal<>(this, Player.class, d -> getBoundingBox().inflate(d), 10, true, false, null));

            preTickEvents.addEvent(
                    new TimedEvent(
                            () -> playSound(RegistroFlorVazio.SPIKE_WAVE_INDICATOR.get(), 2.0f, 0.7f),
                            1
                    )
            );
        }else if (level().isClientSide())
            animationHolder.handleEntityEvent(VoidBlossomAttacks.spawnAction);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(hitbox, HitboxId.Idle.getId());
        builder.define(VELOCIDADE_ATAQUE, 1.0F);
    }

    @Override
    public void serverTick(ServerLevel serverLevel) {
        super.serverTick(serverLevel);
        entityData.set(VELOCIDADE_ATAQUE, (float) AceleracaoChefe.multiplicador(this, ConfiguracaoChefes.FLOR));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (level().isClientSide() && ehAtaque(id)) {
            VelocidadeEventos.executar(entityData.get(VELOCIDADE_ATAQUE), () -> super.handleEntityEvent(id));
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void registrarAtaque() {
        ChaoArmadilha.Armadilha musgo = new ChaoArmadilha.Armadilha(RegistroFlorVazio.MUSGO_FOFO.get().defaultBlockState(), ParticleTypes.COMPOSTER, SoundEvents.MOSS_PLACE);
        ataquesMusgoFofo = ChaoArmadilha.registrarAtaque(this, alvos, ConfiguracaoChefes.FLOR + ".musgo_fofo", ataquesMusgoFofo, musgo);
    }

    private static boolean ehAtaque(byte id) {
        return id == VoidBlossomAttacks.spikeAttack || id == VoidBlossomAttacks.spikeWaveAttack || id == VoidBlossomAttacks.sporeAttack
                || id == VoidBlossomAttacks.bladeAttack || id == VoidBlossomAttacks.blossomAction;
    }

    private void lookAtTarget(){
        LivingEntity target = getTarget();
        if (target != null) {
            lookControl.setLookAt(MobUtils.eyePos(target));
            lookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
        }
    }

    public AnimationHolder getAnimationHolder() {
        return animationHolder;
    }

    @Override
    public String chaveConfig() {
        return ConfiguracaoChefes.FLOR;
    }

    @Override
    public AlvosChefe getAlvos() {
        return alvos;
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 movement) {
        super.move(type, new Vec3(0.0, movement.y(), 0.0));
    }

    private boolean canContinueAttack(){
        return isAlive() && getTarget() != null;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public CompoundOrientedBox getCompoundBoundingBox(AABB bounds) {
        return hitboxHelper.getBounds().getBox(bounds);
    }

    @Override
    public EntityBounds getBounds() {
        return hitboxHelper.getBounds();
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return RegistroFlorVazio.VOID_BLOSSOM_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return RegistroFlorVazio.VOID_BLOSSOM_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public void checkDespawn() {
        MobUtils.preventDespawnExceptPeaceful(this, level());
    }

    @Override
    public int getArmorValue() {
        return getTarget() != null ? super.getArmorValue() : 20;
    }

    @Override
    public void onSetPos(double x, double y, double z) {
        if (hitboxHelper != null) hitboxHelper.updatePosition();
    }

    @Override
    public void setNextDamagedPart(@Nullable String part) {
        hitboxHelper.setNextDamagedPart(part);
    }
}
