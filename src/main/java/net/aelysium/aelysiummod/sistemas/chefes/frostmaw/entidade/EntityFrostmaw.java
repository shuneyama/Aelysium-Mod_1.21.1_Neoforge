package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosFlocoNeve;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosAnel;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula.DadosNuvem;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AceleracaoChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AlvosChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ChaoArmadilha;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.AtaqueChefe;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ChefeAelysium;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.ConfiguracaoChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EfeitosAtaque;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.EventosChefes;
import net.aelysium.aelysiummod.sistemas.chefes.base.combate.StatusChefe;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.MMEntityMoveHelper;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.MMPathNavigateGround;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ia.animacao.*;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.LegSolverQuadruped;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.MowzieEntity;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.MowzieLLibraryEntity;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityCameraShake;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityIceBall;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.efeitos.EntityIceBreath;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.Animation;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.animacao.AnimationHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class EntityFrostmaw extends MowzieLLibraryEntity implements Enemy, ChefeAelysium {
    public static final Animation DIE_ANIMATION = Animation.create(94);
    public static final Animation HURT_ANIMATION = Animation.create(0);
    public static final Animation ROAR_ANIMATION = Animation.create(76);
    public static final Animation SWIPE_ANIMATION = Animation.create(28);
    public static final Animation SWIPE_TWICE_ANIMATION = Animation.create(57);
    public static final Animation ICE_BREATH_ANIMATION = Animation.create(92);
    public static final Animation ICE_BALL_ANIMATION = Animation.create(50);
    public static final Animation ACTIVATE_ANIMATION = Animation.create(118);
    public static final Animation ACTIVATE_NO_CRYSTAL_ANIMATION = Animation.create(100);
    public static final Animation DEACTIVATE_ANIMATION = Animation.create(25);
    public static final Animation DODGE_ANIMATION = Animation.create(15);
    public static final Animation LAND_ANIMATION = Animation.create(14);
    public static final Animation SLAM_ANIMATION = Animation.create(113);

    private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(EntityFrostmaw.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_CRYSTAL = SynchedEntityData.defineId(EntityFrostmaw.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALWAYS_ACTIVE = SynchedEntityData.defineId(EntityFrostmaw.class, EntityDataSerializers.BOOLEAN);

    public static final int DODGE_COOLDOWN = 200;

    public EntityIceBreath iceBreath;

    public boolean swingWhichArm = false;
    private Vec3 prevRightHandPos = new Vec3(0, 0, 0);
    private Vec3 prevLeftHandPos = new Vec3(0, 0, 0);
    private int iceBreathCooldown = 0;
    private int iceBallCooldown = 0;
    private int slamCooldown = 0;
    private int timeWithoutTarget;
    private int shouldDodgeMeasure = 0;
    private int dodgeCooldown = 0;
    private int avancoCooldown = 0;
    private boolean avancando = false;
    private int ataquesNeveFofa = 0;
    private static final ChaoArmadilha.Armadilha NEVE_FOFA = new ChaoArmadilha.Armadilha(Blocks.POWDER_SNOW.defaultBlockState(), ParticleTypes.SNOWFLAKE, SoundEvents.POWDER_SNOW_PLACE);
    private int proximaTrocaAlvo = 0;
    private final AlvosChefe alvos = new AlvosChefe(this);
    private boolean shouldDodge;
    private float dodgeYaw = 0;
    private boolean wantsToIceBreathAfterDodging = false;

    private Vec3 prevTargetPos = new Vec3(0, 0, 0);

    private boolean shouldPlayLandAnimation = false;

    public LegSolverQuadruped legSolver;

    public EntityFrostmaw(EntityType<? extends EntityFrostmaw> type, Level world) {
        super(type, world);
        frame += random.nextInt(50);
        legSolver = new LegSolverQuadruped(1f, 2f, -1, 1.5f);
        if (world.isClientSide)
            socketPosArray = new Vec3[] {new Vec3(0, 0, 0), new Vec3(0, 0, 0), new Vec3(0, 0, 0)};
        active = false;
        playsHurtAnimation = false;
        setYRot(yBodyRot = random.nextFloat() * 360);
        xpReward = 60;

        moveControl = new MMEntityMoveHelper(this, 7);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new AnimationAreaAttackAI<EntityFrostmaw>(this, SWIPE_ANIMATION, null, null, 2, 6.5f, 6, 135, 1, 9) {
            @Override
            protected void atingir(LivingEntity alvo) {
                golpear(alvo, ConfiguracaoChefes.FROSTMAW_GARRA, applyKnockbackMultiplier);
                onAttack(alvo, damageMultiplier, applyKnockbackMultiplier);
            }
        });
        this.goalSelector.addGoal(2, new AnimationAreaAttackAI<EntityFrostmaw>(this, SWIPE_TWICE_ANIMATION, null, null, 1, 6.5f, 6, 135, 1, 9) {
            @Override
            public void start() {
                super.start();
            }

            @Override
            protected void atingir(LivingEntity alvo) {
                golpear(alvo, ConfiguracaoChefes.FROSTMAW_GARRA_DUPLA, applyKnockbackMultiplier);
                onAttack(alvo, damageMultiplier, applyKnockbackMultiplier);
            }

            @Override
            public void tick() {
                super.tick();
                if (getAnimationTick() == 21) {
                    hitEntities();
                }
                if (getAnimationTick() == 16) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_WHOOSH.get(), 2, 0.7f);
                }
                if (getAnimationTick() == 6) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_WHOOSH.get(), 2, 0.8f);
                }
                if (getTarget() != null) lookControl.setLookAt(getTarget(), 30, 30);
            }

            @Override
            protected void onAttack(LivingEntity entityTarget, float damageMultiplier, float applyKnockbackMultiplier) {
                super.onAttack(entityTarget, damageMultiplier, applyKnockbackMultiplier);
                if (getAnimationTick() == 21 && entityTarget instanceof Player player) {
                    if (player.isBlocking()) player.disableShield();
                }
            }
        });
        this.goalSelector.addGoal(2, new SimpleAnimationAI<>(this, ICE_BREATH_ANIMATION, true));
        this.goalSelector.addGoal(2, new SimpleAnimationAI<EntityFrostmaw>(this, ICE_BALL_ANIMATION, true) {
            @Override
            public void start() {
                super.start();
                playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ICEBALL_CHARGE.get(), 2, 0.9f);
            }
        });
        this.goalSelector.addGoal(2, new SimpleAnimationAI<>(this, ROAR_ANIMATION, false));
        this.goalSelector.addGoal(2, new AnimationActivateAI<EntityFrostmaw>(this, ACTIVATE_ANIMATION) {
            @Override
            public void start() {
                super.start();
                playSound(RegistroFrostmaw.ENTITY_FROSTMAW_WAKEUP.get(), 1, 1);
            }
        });
        this.goalSelector.addGoal(2, new AnimationActivateAI<EntityFrostmaw>(this, ACTIVATE_NO_CRYSTAL_ANIMATION) {
            @Override
            public void start() {
                super.start();
                playSound(RegistroFrostmaw.ENTITY_FROSTMAW_WAKEUP.get(), 1, 1);
            }
        });
        this.goalSelector.addGoal(2, new AnimationDeactivateAI<>(this, DEACTIVATE_ANIMATION));
        this.goalSelector.addGoal(2, new SimpleAnimationAI<>(this, LAND_ANIMATION, false));
        this.goalSelector.addGoal(2, new SimpleAnimationAI<>(this, SLAM_ANIMATION, EnumSet.of(Goal.Flag.LOOK)));
        this.goalSelector.addGoal(2, new SimpleAnimationAI<>(this, DODGE_ANIMATION, EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP)));
        this.goalSelector.addGoal(3, new AnimationTakeDamage<>(this));
        this.goalSelector.addGoal(1, new AnimationDieAI<>(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new MMPathNavigateGround(this, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MowzieEntity.createAttributes().add(Attributes.ATTACK_DAMAGE, 11)
                .add(Attributes.MAX_HEALTH, 750)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.FOLLOW_RANGE, 50)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.STEP_HEIGHT, 1)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.9);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVE, false);
        builder.define(HAS_CRYSTAL, true);
        builder.define(ALWAYS_ACTIVE, false);
    }

    @Override
    public void playAmbientSound() {
        if (!active) return;
        int i = random.nextInt(4);
        super.playAmbientSound();
        if (i == 0 && getAnimation() == NO_ANIMATION) {
            AnimationHandler.INSTANCE.sendAnimationMessage(this, ROAR_ANIMATION);
            return;
        }
        if (i < RegistroFrostmaw.ENTITY_FROSTMAW_LIVING.size()) playSound(RegistroFrostmaw.ENTITY_FROSTMAW_LIVING.get(i).get(), 2, 0.8f + random.nextFloat() * 0.3f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    @Override
    public boolean canBePushedByEntity(Entity entity) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void repelEntities(float x, float y, float z, float radius) {
        List<Player> nearbyEntities = getPlayersNearby(x, y, z, radius);
        for (Entity entity : nearbyEntities) {
            double angle = (getAngleBetweenEntities(this, entity) + 90) * Math.PI / 180;
            entity.setDeltaMovement(-0.1 * Math.cos(angle), entity.getDeltaMovement().y,-0.1 * Math.sin(angle));
        }
    }

    @Override
    public void tick() {
        setYRot(yBodyRot);
        super.tick();
        this.repelEntities(3.8f, 3.8f, 3.8f, 3.8f);

        if (getTarget() != null && (!getTarget().isAlive() || getTarget().getHealth() <= 0)) setTarget(null);

        if (isAlwaysActive()) {
            setActive(true);
        }

        if (getActive() && getAnimation() != ACTIVATE_ANIMATION && getAnimation() != ACTIVATE_NO_CRYSTAL_ANIMATION) {
            legSolver.update(this);

            if (getAnimation() == SWIPE_ANIMATION || getAnimation() == SWIPE_TWICE_ANIMATION) {
                if (getAnimationTick() == 3) {
                    int i = Mth.nextInt(random, 0, RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.size());
                    if (i < RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.size()) {
                        playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.get(i).get(), 2, 0.9f + random.nextFloat() * 0.2f);
                    }
                }
            }

            if (getAnimation() == SWIPE_ANIMATION) {
                if (getAnimationTick() == 6) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_WHOOSH.get(), 2, 0.8f);
                }
                if (getTarget() != null) lookControl.setLookAt(getTarget(), 30, 30);
            }

            if (getAnimation() == ROAR_ANIMATION) {
                if (getAnimationTick() == 10) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ROAR.get(), 4, 1);
                    EntityCameraShake.cameraShake(level(), position(), 45, 0.03f, 60, 20);
                }
                if (getAnimationTick() >= 8 && getAnimationTick() < 65) {
                    doRoarEffects();
                }
            }

            if (getAnimation() == LAND_ANIMATION) {
                if (getAnimationTick() == 3) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_LAND.get(), 3, 0.9f);
                }
            }

            if (getAnimation() == SLAM_ANIMATION) {
                if (getAnimationTick() == 82) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_LIVING.get(0).get(), 2, 1);
                }
                if (getTarget() != null) lookControl.setLookAt(getTarget(), 30, 30);
                if (getAnimationTick() == 82) {
                    int i = Mth.nextInt(random, 0, RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.size() - 1);
                    if (i < RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.size()) {
                        playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.get(i).get(), 2, 0.9f + random.nextFloat() * 0.2f);
                    }
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_WHOOSH.get(), 2, 0.7f);
                }
                if (getAnimationTick() == 87) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_LAND.get(), 3, 1f);
                    float radius = 4;
                    float slamPosX = (float) (getX() + radius * Math.cos(Math.toRadians(getYRot() + 90)));
                    float slamPosZ = (float) (getZ() + radius * Math.sin(Math.toRadians(getYRot() + 90)));
                    if (level().isClientSide) level().addParticle(new DadosAnel(0f, (float)Math.PI/2f, 17, 1f, 1f, 1f, 1f, 60, false, DadosAnel.EnumRingBehavior.GROW), slamPosX, getY() + 0.2f, slamPosZ, 0, 0, 0);
                    AABB hitBox = new AABB(slamPosX - 0.5f, getY() - 0.5f,  slamPosZ - 0.5f,slamPosX + 0.5f, getY() + 0.5f, slamPosZ + 0.5f).inflate(3, 3, 3);
                    List<LivingEntity> entitiesHit = level().getEntitiesOfClass(LivingEntity.class, hitBox);
                    for (LivingEntity entity: entitiesHit) {
                        if (entity != this && entity.position().distanceToSqr(slamPosX, getY(), slamPosZ) <= 9) {
                            golpear(entity, ConfiguracaoChefes.FROSTMAW_ESMAGAR, 1);
                            if (entity.isBlocking()) entity.getUseItem().hurtAndBreak(400, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
                        }
                    }
                    EntityCameraShake.cameraShake(level(), new Vec3(slamPosX, getY(), slamPosZ), 30, 0.1f, 0, 20);
                }
            }
            if (getAnimation() == DODGE_ANIMATION && !level().isClientSide) {
                getNavigation().stop();
                if (getAnimationTick() == 2 && !avancando) {
                    dodgeYaw = (float) Math.toRadians(targetAngle + 90 + random.nextFloat() * 150 - 75);
                }
                if (getAnimationTick() == 6 && (onGround() || isInLava() || isInWater())) {
                    if (avancando) {
                        saltarNoAlvo();
                    } else {
                        float speed = 1.7f;
                        Vec3 m = getDeltaMovement().add(speed * Math.cos(dodgeYaw), 0, speed * Math.sin(dodgeYaw));
                        setDeltaMovement(m.x, 0.6, m.z);
                    }
                }
                if (getAnimationTick() >= 6) avancando = false;
                if (getTarget() != null) lookControl.setLookAt(getTarget(), 30, 30);
            }
            if (avancando && getAnimation() != DODGE_ANIMATION) avancando = false;

            if (getAnimation() == ICE_BREATH_ANIMATION) {
                if (getTarget() != null) {
                    lookControl.setLookAt(getTarget(), 30, 30);
                    lookAt(getTarget(), 30, 30);
                    yHeadRot = yBodyRot = getYRot();
                }
                Vec3 mouthPos = new Vec3(2.3, 2.65, 0);
                mouthPos = mouthPos.yRot((float)Math.toRadians(-getYRot() - 90));
                mouthPos = mouthPos.add(position());
                mouthPos = mouthPos.add(new Vec3(0, 0, 1).xRot((float)Math.toRadians(-getXRot())).yRot((float)Math.toRadians(-yHeadRot)));
                if (getAnimationTick() == 13) {
                    iceBreath = new EntityIceBreath(RegistroFrostmaw.ICE_BREATH.get(), level(), this);
                    iceBreath.absMoveTo(mouthPos.x, mouthPos.y, mouthPos.z, yHeadRot, getXRot() + 10);
                    if (!level().isClientSide) level().addFreshEntity(iceBreath);
                }
                if (iceBreath != null)
                    iceBreath.absMoveTo(mouthPos.x, mouthPos.y, mouthPos.z, yHeadRot, getXRot() + 10);
            }

            if (getAnimation() == ICE_BALL_ANIMATION) {
                if (getTarget() != null) lookControl.setLookAt(getTarget(), 15, 15);
                Vec3 projectilePos = new Vec3(2.0, 1.9, 0);
                projectilePos = projectilePos.yRot((float)Math.toRadians(-getYRot()- 90));
                projectilePos = projectilePos.add(position());
                projectilePos = projectilePos.add(new Vec3(0, 0, 1).xRot((float)Math.toRadians(-getXRot())).yRot((float)Math.toRadians(-yHeadRot)));
                if (level().isClientSide) {
                    Vec3 mouthPos = socketPosArray[2];
                    if (getAnimationTick() < 12) {
                        for (int i = 0; i < 6; i++) {
                            Vec3 particlePos = new Vec3(3.5, 0, 0);
                            particlePos = particlePos.yRot((float) (random.nextFloat() * 2 * Math.PI));
                            particlePos = particlePos.xRot((float) (random.nextFloat() * 2 * Math.PI));
                            float value = random.nextFloat() * 0.15f;
                            level().addParticle(new DadosNuvem(0.75f + value, 0.75f + value, 1f, 5f + random.nextFloat() * 15f, 30, DadosNuvem.EnumCloudBehavior.CONSTANT, 1f), mouthPos.x + particlePos.x, mouthPos.y + particlePos.y, mouthPos.z + particlePos.z, -0.1 * particlePos.x, -0.1 * particlePos.y, -0.1 * particlePos.z);
                        }
                        for (int i = 0; i < 8; i++) {
                            Vec3 particlePos = new Vec3(3.5, 0, 0);
                            particlePos = particlePos.yRot((float) (random.nextFloat() * 2 * Math.PI));
                            particlePos = particlePos.xRot((float) (random.nextFloat() * 2 * Math.PI));
                            level().addParticle(new DadosFlocoNeve(40, false), mouthPos.x + particlePos.x, mouthPos.y + particlePos.y, mouthPos.z + particlePos.z, -0.07 * particlePos.x, -0.07 * particlePos.y, -0.07 * particlePos.z);
                        }
                    }
                }

                if (getAnimationTick() == 32) {
                    if (getTarget() != null) prevTargetPos = getTarget().position().add(new Vec3(0f, getTarget().getBbHeight() / 2.0, 0f));
                }
                if (getAnimationTick() == 33) {
                    playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ICEBALL_SHOOT.get(), 2, 0.7f);

                    EntityIceBall iceBall = new EntityIceBall(RegistroFrostmaw.ICE_BALL.get(), level(), this);
                    iceBall.absMoveTo(projectilePos.x, projectilePos.y, projectilePos.z, yHeadRot, getXRot() + 10);
                    float projSpeed = 1.6f;
                    if (getTarget() != null) {
                        float ticksUntilHit = targetDistance / projSpeed;
                        Vec3 targetPos = getTarget().position().add(new Vec3(0f, getTarget().getBbHeight() / 2.0, 0f));
                        Vec3 targetMovement = targetPos.subtract(prevTargetPos).scale(ticksUntilHit * 0.95);
                        targetMovement = targetMovement.subtract(0, targetMovement.y, 0);
                        Vec3 futureTargetPos = targetPos.add(targetMovement);
                        Vec3 projectileMid = projectilePos.add(new Vec3(0, iceBall.getBbHeight() / 2.0, 0));
                        Vec3 shootVec = futureTargetPos.subtract(projectileMid).normalize();
                        iceBall.shoot(shootVec.x, shootVec.y, shootVec.z, projSpeed, 0);
                    }
                    else {
                        iceBall.shoot(getLookAngle().x, getLookAngle().y, getLookAngle().z, projSpeed, 0);
                    }
                    if (!level().isClientSide) level().addFreshEntity(iceBall);
                    if (!level().isClientSide) {
                        for (LivingEntity extra : alvos.alvos(ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_BOLA).alvos(), ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_BOLA).miraVoadores())) {
                            if (extra == getTarget()) continue;
                            EntityIceBall bola = new EntityIceBall(RegistroFrostmaw.ICE_BALL.get(), level(), this);
                            bola.absMoveTo(projectilePos.x, projectilePos.y, projectilePos.z, yHeadRot, getXRot() + 10);
                            Vec3 direcao = extra.position().add(0, extra.getBbHeight() / 2.0, 0).subtract(projectilePos.add(0, bola.getBbHeight() / 2.0, 0)).normalize();
                            bola.shoot(direcao.x, direcao.y, direcao.z, projSpeed, 0);
                            level().addFreshEntity(bola);
                        }
                    }
                }
            }

            spawnSwipeParticles();

            if (fallDistance > 0.2 && !onGround() && getLastDamageSource() != null && !getLastDamageSource().is(DamageTypes.LAVA)) shouldPlayLandAnimation = true;
            if (onGround() && shouldPlayLandAnimation && getAnimation() != DODGE_ANIMATION) {
                if (!level().isClientSide && getAnimation() == NO_ANIMATION) {
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, LAND_ANIMATION);
                }
                shouldPlayLandAnimation = false;
            }

            if (!level().isClientSide && getAnimation() == NO_ANIMATION && tickCount >= proximaTrocaAlvo) {
                proximaTrocaAlvo = tickCount + ConfiguracaoChefes.status(ConfiguracaoChefes.FROSTMAW).trocaAlvoTicks();
                LivingEntity novo = alvos.escolher();
                if (novo != null) setTarget(novo);
            }

            if (getTarget() != null) {
                timeWithoutTarget = 0;

                float entityHitAngle = (float) ((Math.atan2(getTarget().getZ() - getZ(), getTarget().getX() - getX()) * (180 / Math.PI) - 90) % 360);
                float entityAttackingAngle = yBodyRot % 360;
                if (entityHitAngle < 0) {
                    entityHitAngle += 360;
                }
                if (entityAttackingAngle < 0) {
                    entityAttackingAngle += 360;
                }
                float entityRelativeAngle = entityHitAngle - entityAttackingAngle;
                if (getNavigation().isDone() && !((entityRelativeAngle <= 30 / 2f && entityRelativeAngle >= -30 / 2f) || (entityRelativeAngle >= 360 - 30 / 2f || entityRelativeAngle <= -360 + 30 / 2f))) {
                    getNavigation().moveTo(getTarget(), 0.85);
                }

                if (shouldDodgeMeasure >= 16) shouldDodge = true;
                if (getTarget().hasEffect(RegistroFrostmaw.FROZEN)) shouldDodge = false;
                if (targetDistance < 4 && shouldDodge && getAnimation() == NO_ANIMATION) {
                    shouldDodge = false;
                    dodgeCooldown = DODGE_COOLDOWN;
                    shouldDodgeMeasure = 0;
                    if (getHasCrystal()) {
                        wantsToIceBreathAfterDodging = true;
                    }
                    iceBreathCooldown = 0;
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, DODGE_ANIMATION);
                }

                if (targetDistance > 5.5 && !(getAnimation() == ICE_BREATH_ANIMATION && targetDistance < 7.5)) {
                    if (getAnimation() != SLAM_ANIMATION) getNavigation().moveTo(getTarget(), 1);
                    else getNavigation().moveTo(getTarget(), 0.95);
                }
                else getNavigation().stop();
                if (targetDistance <= 8.5 && getAnimation() == NO_ANIMATION && slamCooldown <= 0 && random.nextInt(4) == 0 && getHealthRatio() < 0.6) {
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, SLAM_ANIMATION);
                    registrarAtaque();
                    slamCooldown = AceleracaoChefe.recarga(this, ConfiguracaoChefes.FROSTMAW, ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_ESMAGAR).recarga(), 0);
                }
                if (podeAvancar(targetDistance)) {
                    iniciarAvanco();
                }
                if (targetDistance <= 6.5 && getAnimation() == NO_ANIMATION && !wantsToIceBreathAfterDodging) {
                    if (random.nextDouble() < ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_GARRA_DUPLA).peso())
                        AnimationHandler.INSTANCE.sendAnimationMessage(this, SWIPE_TWICE_ANIMATION);
                    else AnimationHandler.INSTANCE.sendAnimationMessage(this, SWIPE_ANIMATION);
                    registrarAtaque();
                }
                if (targetDistance <= 13.5 && getAnimation() == NO_ANIMATION && iceBreathCooldown <= 0 && getHasCrystal() && (onGround() || wasTouchingWater)) {
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, ICE_BREATH_ANIMATION);
                    registrarAtaque();
                    iceBreathCooldown = AceleracaoChefe.recarga(this, ConfiguracaoChefes.FROSTMAW, ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_SOPRO).recarga(), 0);
                    wantsToIceBreathAfterDodging = false;
                }
                if (getAnimation() == NO_ANIMATION && iceBallCooldown <= 0 && getHasCrystal() && (onGround() || wasTouchingWater) && (targetDistance >= 14.5 || alvos.alguemVoando())) {
                    AnimationHandler.INSTANCE.sendAnimationMessage(this, ICE_BALL_ANIMATION);
                    registrarAtaque();
                    iceBallCooldown = AceleracaoChefe.recarga(this, ConfiguracaoChefes.FROSTMAW, ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_BOLA).recarga(), 0);
                }
            }
            else if (!level().isClientSide) {
                if (!isAlwaysActive()) {
                    timeWithoutTarget++;
                    if (timeWithoutTarget > 1200 || level().getDifficulty() == Difficulty.PEACEFUL) {
                        timeWithoutTarget = 0;
                        if (getAnimation() == NO_ANIMATION) {
                            AnimationHandler.INSTANCE.sendAnimationMessage(this, DEACTIVATE_ANIMATION);
                            setActive(false);
                        }
                    }
                }
            }
        }
        else {
            getNavigation().stop();
            setDeltaMovement(0, getDeltaMovement().y, 0);
            yBodyRot = yBodyRotO;
            if (!level().isClientSide && getAnimation() != ACTIVATE_ANIMATION) {
                StatusChefe status = ConfiguracaoChefes.status(ConfiguracaoChefes.FROSTMAW);
                if (EventosChefes.podeCurar(this, status)) heal(status.curaPorTick());
            }
            if (getTarget() != null && getTarget().hasEffect(MobEffects.INVISIBILITY)) {
                setTarget(null);
            }
            if (getTarget() != null && getAnimation() == NO_ANIMATION && !getAttackableEntityLivingBaseNearby(8, 8, 8, 8).isEmpty()) {
                if (level().getDifficulty() != Difficulty.PEACEFUL) {
                    if (getHasCrystal()) AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_ANIMATION);
                    else AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_NO_CRYSTAL_ANIMATION);
                    setActive(true);
                }
            }

        }

        if (getAnimation() == ACTIVATE_ANIMATION || getAnimation() == ACTIVATE_NO_CRYSTAL_ANIMATION) {
            if (getAnimation() == ACTIVATE_ANIMATION && getAnimationTick() == 18) playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ATTACK.get(0).get(), 1.5f, 1);
            if ((getAnimation() == ACTIVATE_ANIMATION && getAnimationTick() == 52) || (getAnimation() == ACTIVATE_NO_CRYSTAL_ANIMATION && getAnimationTick() == 34)) {
                playSound(RegistroFrostmaw.ENTITY_FROSTMAW_ROAR.get(), 4, 1);
                EntityCameraShake.cameraShake(level(), position(), 45, 0.03f, 60, 20);
            }
            if ((getAnimation() == ACTIVATE_ANIMATION && getAnimationTick() >= 51 && getAnimationTick() < 108) || (getAnimation() == ACTIVATE_NO_CRYSTAL_ANIMATION && getAnimationTick() >= 33 && getAnimationTick() < 90)) {
                doRoarEffects();
            }
        }

        if (level().isClientSide) {
            if ((getAnimation() == SWIPE_ANIMATION || getAnimation() == SWIPE_TWICE_ANIMATION) && getAnimationTick() == 1) {
                swingWhichArm = random.nextBoolean();
            }
        }

        float moveX = (float) (getX() - xo);
        float moveZ = (float) (getZ() - zo);
        float speed = Mth.sqrt(moveX * moveX + moveZ * moveZ);
        if (frame % 16 == 5 && speed > 0.05 && active) {
            playSound(RegistroFrostmaw.ENTITY_FROSTMAW_STEP.get(), 3F, 0.8F + random.nextFloat() * 0.2f);
            EntityCameraShake.cameraShake(level(), position(), 20, 0.03f, 0, 10);
        }

        if (frame % 118 == 1 && !active) {
            int i = Mth.nextInt(random, 0, 1);
            playSound(RegistroFrostmaw.ENTITY_FROSTMAW_BREATH.get(i).get(), 1.5F, 1.1F + random.nextFloat() * 0.1f);
        }

        if (iceBreathCooldown > 0) iceBreathCooldown--;
        if (iceBallCooldown > 0) iceBallCooldown--;
        if (slamCooldown > 0) slamCooldown--;
        if (shouldDodgeMeasure > 0 && tickCount % 7 == 0) shouldDodgeMeasure--;
        if (dodgeCooldown > 0) dodgeCooldown--;
        if (avancoCooldown > 0) avancoCooldown--;
        yRotO = getYRot();
    }

    private void doRoarEffects() {
        if (getHasCrystal()) {
            List<LivingEntity> entities = getEntityLivingBaseNearby(10, 3, 10, 10);
            for (LivingEntity entity : entities) {
                if (entity == this) continue;
                double angle = (getAngleBetweenEntities(this, entity) + 90) * Math.PI / 180;
                double distance = distanceTo(entity) - 4;
                entity.setDeltaMovement(entity.getDeltaMovement().add(Math.min(1 / (distance * distance), 1) * -1 * Math.cos(angle), 0, Math.min(1 / (distance * distance), 1) * -1 * Math.sin(angle)));
            }
            if (getAnimationTick() % 12 == 0 && level().isClientSide) {
                int particleCount = 15;
                for (int i = 1; i <= particleCount; i++) {
                    double yaw = i * 360.f / particleCount;
                    double speed = 0.9;
                    double xSpeed = speed * Math.cos(Math.toRadians(yaw));
                    double zSpeed = speed * Math.sin(Math.toRadians(yaw));
                    level().addParticle(new DadosNuvem(0.75f, 0.75f, 1f, 40f, 22, DadosNuvem.EnumCloudBehavior.GROW, 1f), getX(), getY() + 1f, getZ(), xSpeed, 0, zSpeed);
                }
                for (int i = 1; i <= particleCount; i++) {
                    double yaw = i * 360.f / particleCount;
                    double speed = 0.65;
                    double xSpeed = speed * Math.cos(Math.toRadians(yaw));
                    double zSpeed = speed * Math.sin(Math.toRadians(yaw));
                    level().addParticle(new DadosNuvem(0.75f, 0.75f, 1f, 35f, 22, DadosNuvem.EnumCloudBehavior.GROW, 1f), getX(), getY() + 1f, getZ(), xSpeed, 0, zSpeed);
                }
            }
        }
    }

    private static boolean isInventoryFull(Inventory inventory) {
        for(ItemStack itemstack : inventory.items) {
            if (itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason) {
        List<LivingEntity> nearby = getEntityLivingBaseNearby(100, 100, 100, 100);
        for (LivingEntity nearbyEntity : nearby) {
            if (nearbyEntity instanceof EntityFrostmaw || nearbyEntity instanceof Villager) {
                return false;
            }
        }
        return super.checkSpawnRules(world, reason);
    }

    public int getMaxSpawnClusterSize()
    {
        return 1;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingData) {
        setHasCrystal(true);
        return super.finalizeSpawn(world, difficulty, reason, livingData);
    }

    private void spawnSwipeParticles() {
        if (level().isClientSide && getHasCrystal()) {
            double motionX = getDeltaMovement().x();
            double motionY = getDeltaMovement().y();
            double motionZ = getDeltaMovement().z();

            int snowflakeDensity = 4;
            float snowflakeRandomness = 0.5f;
            int cloudDensity = 2;
            float cloudRandomness = 0.5f;
            if (getAnimation() == SWIPE_ANIMATION || getAnimation() == SWIPE_TWICE_ANIMATION) {
                Vec3 rightHandPos = socketPosArray[0];
                Vec3 leftHandPos = socketPosArray[1];
                if (getAnimation() == SWIPE_ANIMATION) {
                    if (getAnimationTick() > 8 && getAnimationTick() < 14) {
                        if (swingWhichArm) {
                            double length = prevRightHandPos.subtract(rightHandPos).length();
                            int numClouds = (int) Math.floor(2 * length);
                            for (int i = 0; i < numClouds; i++) {
                                double x = prevRightHandPos.x + i * (rightHandPos.x - prevRightHandPos.x) / numClouds;
                                double y = prevRightHandPos.y + i * (rightHandPos.y - prevRightHandPos.y) / numClouds;
                                double z = prevRightHandPos.z + i * (rightHandPos.z - prevRightHandPos.z) / numClouds;
                                for (int j = 0; j < snowflakeDensity; j++) {
                                    float xOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                    float yOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                    float zOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                    level().addParticle(new DadosFlocoNeve(40, false), x + xOffset, y + yOffset, z + zOffset, motionX, motionY - 0.01f, motionZ);
                                }
                                for (int j = 0; j < cloudDensity; j++) {
                                    float xOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                    float yOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                    float zOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                    float value = random.nextFloat() * 0.1f;
                                    level().addParticle(new DadosNuvem(0.8f + value, 0.8f + value, 1f, (float) (10d + random.nextDouble() * 10d), 40, DadosNuvem.EnumCloudBehavior.SHRINK, 1f), x + xOffset, y + yOffset, z + zOffset, motionX, motionY, motionZ);
                                }
                            }
                        } else {
                            double length = prevLeftHandPos.subtract(leftHandPos).length();
                            int numClouds = (int) Math.floor(2.5 * length);
                            for (int i = 0; i < numClouds; i++) {
                                double x = prevLeftHandPos.x + i * (leftHandPos.x - prevLeftHandPos.x) / numClouds;
                                double y = prevLeftHandPos.y + i * (leftHandPos.y - prevLeftHandPos.y) / numClouds;
                                double z = prevLeftHandPos.z + i * (leftHandPos.z - prevLeftHandPos.z) / numClouds;
                                for (int j = 0; j < snowflakeDensity; j++) {
                                    float xOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                    float yOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                    float zOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                    level().addParticle(new DadosFlocoNeve(40, false), x + xOffset, y + yOffset, z + zOffset, motionX, motionY - 0.01f, motionZ);
                                }
                                for (int j = 0; j < cloudDensity; j++) {
                                    float xOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                    float yOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                    float zOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                    float value = random.nextFloat() * 0.1f;
                                    level().addParticle(new DadosNuvem(0.8f + value, 0.8f + value, 1f, (float) (10d + random.nextDouble() * 10d), 40, DadosNuvem.EnumCloudBehavior.SHRINK, 1f), x + xOffset, y + yOffset, z + zOffset, motionX, motionY, motionZ);
                                }
                            }
                        }
                    }
                } else {
                    if ((swingWhichArm && getAnimationTick() > 8 && getAnimationTick() < 14) || (!swingWhichArm && getAnimationTick() > 19 && getAnimationTick() < 25)) {
                        double length = prevRightHandPos.subtract(rightHandPos).length();
                        int numClouds = (int) Math.floor(2 * length);
                        for (int i = 0; i < numClouds; i++) {
                            double x = prevRightHandPos.x + i * (rightHandPos.x - prevRightHandPos.x) / numClouds;
                            double y = prevRightHandPos.y + i * (rightHandPos.y - prevRightHandPos.y) / numClouds;
                            double z = prevRightHandPos.z + i * (rightHandPos.z - prevRightHandPos.z) / numClouds;
                            for (int j = 0; j < snowflakeDensity; j++) {
                                float xOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                float yOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                float zOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                level().addParticle(new DadosFlocoNeve(40, false), x + xOffset, y + yOffset, z + zOffset, motionX, motionY - 0.01f, motionZ);
                            }
                            for (int j = 0; j < cloudDensity; j++) {
                                float xOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                float yOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                float zOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                float value = random.nextFloat() * 0.1f;
                                level().addParticle(new DadosNuvem(0.8f + value, 0.8f + value, 1f, (float) (10d + random.nextDouble() * 10d), 40, DadosNuvem.EnumCloudBehavior.SHRINK, 1f), x + xOffset, y + yOffset, z + zOffset, motionX, motionY, motionZ);
                            }
                        }
                    } else if ((!swingWhichArm && getAnimationTick() > 8 && getAnimationTick() < 14) || (swingWhichArm && getAnimationTick() > 19 && getAnimationTick() < 25)) {
                        double length = prevLeftHandPos.subtract(leftHandPos).length();
                        int numClouds = (int) Math.floor(2.5 * length);
                        for (int i = 0; i < numClouds; i++) {
                            double x = prevLeftHandPos.x + i * (leftHandPos.x - prevLeftHandPos.x) / numClouds;
                            double y = prevLeftHandPos.y + i * (leftHandPos.y - prevLeftHandPos.y) / numClouds;
                            double z = prevLeftHandPos.z + i * (leftHandPos.z - prevLeftHandPos.z) / numClouds;
                            for (int j = 0; j < snowflakeDensity; j++) {
                                float xOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                float yOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                float zOffset = snowflakeRandomness * (2 * random.nextFloat() - 1);
                                level().addParticle(new DadosFlocoNeve(40, false), x + xOffset, y + yOffset, z + zOffset, motionX, motionY - 0.01f, motionZ);
                            }
                            for (int j = 0; j < cloudDensity; j++) {
                                float xOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                float yOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                float zOffset = cloudRandomness * (2 * random.nextFloat() - 1);
                                float value = random.nextFloat() * 0.1f;
                                level().addParticle(new DadosNuvem(0.8f + value, 0.8f + value, 1f, (float) (10d + random.nextDouble() * 10d), 40, DadosNuvem.EnumCloudBehavior.SHRINK, 1f), x + xOffset, y + yOffset, z + zOffset, motionX, motionY, motionZ);
                            }
                        }
                    }
                }
                prevLeftHandPos = leftHandPos;
                prevRightHandPos = rightHandPos;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        if (source.is(DamageTypes.LAVA) && getAnimation() == NO_ANIMATION) {
            AnimationHandler.INSTANCE.sendAnimationMessage(this, DODGE_ANIMATION);
        }

        if (source.is(DamageTypeTags.IS_FIRE)) damage *= 1.25;

        if (source.getDirectEntity() instanceof AbstractArrow) {
            playSound(RegistroFrostmaw.ENTITY_FROSTMAW_UNDAMAGED.get(), 0.4F, 2);
            Entity entity = source.getEntity();
            if (entity != null && entity instanceof LivingEntity && (!(entity instanceof Player) || !((Player)entity).isCreative()) && getTarget() == null && !(entity instanceof EntityFrostmaw)) setTarget((LivingEntity) entity);
            if (!getActive()) {
                if (getAnimation() != DIE_ANIMATION) {
                    if (level().getDifficulty() != Difficulty.PEACEFUL) {
                        if (getHasCrystal()) AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_ANIMATION);
                        else AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_NO_CRYSTAL_ANIMATION);
                    }
                }
                if (level().getDifficulty() != Difficulty.PEACEFUL) setActive(true);
            }
            return false;
        }

        boolean attack = super.hurt(source, damage);

        if (attack) {
            shouldDodgeMeasure += damage;
            Entity entity = source.getEntity();
            if (entity != null && entity instanceof LivingEntity && (!(entity instanceof Player) || !((Player)entity).isCreative()) && getTarget() == null && !(entity instanceof EntityFrostmaw)) setTarget((LivingEntity) entity);
            if (!getActive()) {
                if (getAnimation() != DIE_ANIMATION && level().getDifficulty() != Difficulty.PEACEFUL) {
                    if (getHasCrystal()) AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_ANIMATION);
                    else AnimationHandler.INSTANCE.sendAnimationMessage(this, ACTIVATE_NO_CRYSTAL_ANIMATION);
                }
                if (level().getDifficulty() != Difficulty.PEACEFUL) setActive(true);
            }
        }

        return attack;
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (getAnimationTick() == 5) {
            playSound(RegistroFrostmaw.ENTITY_FROSTMAW_DIE.get(), 2.5f, 1);
        } else if (getAnimationTick() == 53) {
            playSound(RegistroFrostmaw.ENTITY_FROSTMAW_LAND.get(), 2.5f, 1);
        }
    }

    @Override
    public String chaveConfig() {
        return ConfiguracaoChefes.FROSTMAW;
    }

    @Override
    public float multiplicadorDano(AtaqueChefe ataque) {
        if (ataque.equals(ConfiguracaoChefes.ataque(ConfiguracaoChefes.FROSTMAW_ESMAGAR))) return 1.0F;
        double extra = Math.max(0.0, ConfiguracaoChefes.numero(ConfiguracaoChefes.FROSTMAW + ".dano_extra_por_fase"));
        return (float) (1.0 + extra * AceleracaoChefe.fasesPerdidas(this));
    }

    private boolean podeAvancar(double distancia) {
        if (avancoCooldown > 0 || getAnimation() != NO_ANIMATION || !onGround()) return false;
        if (AceleracaoChefe.fasesPerdidas(this) < ConfiguracaoChefes.inteiro(ConfiguracaoChefes.FROSTMAW + ".avanco.fase_minima")) return false;
        return distancia >= ConfiguracaoChefes.numero(ConfiguracaoChefes.FROSTMAW + ".avanco.distancia_minima")
                && distancia <= ConfiguracaoChefes.numero(ConfiguracaoChefes.FROSTMAW + ".avanco.distancia_maxima");
    }

    private void saltarNoAlvo() {
        LivingEntity alvo = getTarget();
        if (alvo == null) return;
        double dx = alvo.getX() - getX();
        double dz = alvo.getZ() - getZ();
        double distancia = Math.sqrt(dx * dx + dz * dz);
        if (distancia < 1.0E-3) return;
        double velocidade = Math.min(Math.max(distancia - 3.0, 0.0) / 8.4, 2.2);
        setDeltaMovement(dx / distancia * velocidade, 0.6, dz / distancia * velocidade);
    }

    private void iniciarAvanco() {
        avancando = true;
        avancoCooldown = AceleracaoChefe.recarga(this, ConfiguracaoChefes.FROSTMAW, ConfiguracaoChefes.inteiro(ConfiguracaoChefes.FROSTMAW + ".avanco.recarga_ticks"), 20);
        AnimationHandler.INSTANCE.sendAnimationMessage(this, DODGE_ANIMATION);
        registrarAtaque();
    }

    private void registrarAtaque() {
        ataquesNeveFofa = ChaoArmadilha.registrarAtaque(this, alvos, ConfiguracaoChefes.FROSTMAW + ".neve_fofa", ataquesNeveFofa, NEVE_FOFA);
    }

    @Override
    public AlvosChefe getAlvos() {
        return alvos;
    }

    private void golpear(LivingEntity alvo, String chave, float empurrao) {
        AtaqueChefe ataque = ConfiguracaoChefes.ataque(chave);
        if (causarDano(alvo, ataque.dano() * multiplicadorDano(ataque), empurrao)) EfeitosAtaque.aplicar(this, alvo, ataque);
    }

    @Override
    public Animation getDeathAnimation() {
        return DIE_ANIMATION;
    }

    @Override
    public Animation getHurtAnimation() {
        return HURT_ANIMATION;
    }

    public void setActive(boolean active) {
        getEntityData().set(ACTIVE, active);
    }

    public boolean getActive() {
        this.active = getEntityData().get(ACTIVE);
        return active;
    }

    public void setHasCrystal(boolean hasCrystal) {
        getEntityData().set(HAS_CRYSTAL, hasCrystal);
    }

    public boolean getHasCrystal() {
        return getEntityData().get(HAS_CRYSTAL);
    }

    public boolean isAlwaysActive() {
        return getEntityData().get(ALWAYS_ACTIVE);
    }

    public void setAlwaysActive(boolean isAlwaysActive) {
        getEntityData().set(ALWAYS_ACTIVE, isAlwaysActive);
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[] {DIE_ANIMATION, HURT_ANIMATION, ROAR_ANIMATION, SWIPE_ANIMATION, SWIPE_TWICE_ANIMATION, ICE_BREATH_ANIMATION, ICE_BALL_ANIMATION, ACTIVATE_ANIMATION, ACTIVATE_NO_CRYSTAL_ANIMATION, DEACTIVATE_ANIMATION, SLAM_ANIMATION, LAND_ANIMATION, DODGE_ANIMATION};
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("has_crystal")) {
            setHasCrystal(compound.getBoolean("has_crystal"));
        }
        setActive(compound.getBoolean("active"));
        setAlwaysActive(compound.getBoolean("alwaysActive"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("has_crystal", getHasCrystal());
        compound.putBoolean("active", getActive());
        compound.putBoolean("alwaysActive", isAlwaysActive());
    }

    @Override
    public boolean requiresCustomPersistence() {
        return getHasCrystal();
    }

    @Override
    public boolean hasBossBar() {
        return RegistroFrostmaw.TEM_BARRA_CHEFE;
    }

    @Override
    public BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.WHITE;
    }

    @Override
    public Vec3 getDeltaMovement() {
        if (!getActive()) return super.getDeltaMovement().multiply(0, 1, 0);
        return super.getDeltaMovement();
    }

    @Override
    public boolean resetHealthOnPlayerRespawn() {
        return RegistroFrostmaw.RESETA_VIDA_AO_RENASCER;
    }
}
