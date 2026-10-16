package net.aelysium.aelysiummod.conteudo.entidade;

import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import net.aelysium.aelysiummod.registro.ModEntidades;

public class EntidadeTridenteConchas extends AbstractArrow {

    private static final EntityDataAccessor<Byte> ID_LEALDADE = SynchedEntityData.defineId(EntidadeTridenteConchas.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_BRILHO = SynchedEntityData.defineId(EntidadeTridenteConchas.class, EntityDataSerializers.BOOLEAN);

    private boolean danoCausado;
    public int retornoTicksCliente;

    public EntidadeTridenteConchas(EntityType<? extends EntidadeTridenteConchas> type, Level level) {
        super(type, level);
    }

    public EntidadeTridenteConchas(Level level, LivingEntity dono, ItemStack stack) {
        super(ModEntidades.TRIDENTE_CONCHAS.get(), dono, level, stack, null);
        this.entityData.set(ID_LEALDADE, lealdadeDoItem(stack));
        this.entityData.set(ID_BRILHO, stack.hasFoil());
    }

    public EntidadeTridenteConchas(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntidades.TRIDENTE_CONCHAS.get(), x, y, z, level, stack, stack);
        this.entityData.set(ID_LEALDADE, lealdadeDoItem(stack));
        this.entityData.set(ID_BRILHO, stack.hasFoil());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LEALDADE, (byte) 0);
        builder.define(ID_BRILHO, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.danoCausado = true;
        }

        Entity dono = this.getOwner();
        int lealdade = this.entityData.get(ID_LEALDADE);
        if (lealdade > 0 && (this.danoCausado || this.isNoPhysics()) && dono != null) {
            if (!donoValidoParaRetorno()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 ate = dono.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + ate.y * 0.015 * lealdade, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double aceleracao = 0.05 * lealdade;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(ate.normalize().scale(aceleracao)));
                if (this.retornoTicksCliente == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.retornoTicksCliente++;
            }
        }

        super.tick();
    }

    private boolean donoValidoParaRetorno() {
        Entity dono = this.getOwner();
        return dono != null && dono.isAlive() && (!(dono instanceof ServerPlayer) || !dono.isSpectator());
    }

    public boolean isFoil() {
        return this.entityData.get(ID_BRILHO);
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 inicio, Vec3 fim) {
        return this.danoCausado ? null : super.findHitEntity(inicio, fim);
    }

    @Override
    protected void onHitEntity(EntityHitResult resultado) {
        Entity alvo = resultado.getEntity();
        float dano = 8.0F;
        Entity dono = this.getOwner();
        DamageSource fonte = this.damageSources().trident(this, dono == null ? this : dono);
        if (this.level() instanceof ServerLevel serverLevel) {
            dano = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), alvo, fonte, dano);
        }

        this.danoCausado = true;
        if (alvo.hurt(fonte, dano)) {
            if (alvo.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, alvo, fonte, this.getWeaponItem());
            }

            if (alvo instanceof LivingEntity vivo) {
                this.doKnockback(vivo, fonte);
                this.doPostHurtEffects(vivo);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void hitBlockEnchantmentEffects(ServerLevel level, BlockHitResult resultado, ItemStack arma) {
        Vec3 ponto = resultado.getBlockPos().clampLocationWithin(resultado.getLocation());
        EnchantmentHelper.onHitBlock(
                level,
                arma,
                this.getOwner() instanceof LivingEntity vivo ? vivo : null,
                this,
                null,
                ponto,
                level.getBlockState(resultado.getBlockPos()),
                item -> this.kill()
        );
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected boolean tryPickup(Player jogador) {
        return super.tryPickup(jogador) || this.isNoPhysics() && this.ownedBy(jogador) && jogador.getInventory().add(this.getPickupItem());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItens.TRIDENTE_CORAL.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player jogador) {
        if (this.ownedBy(jogador) || this.getOwner() == null) {
            super.playerTouch(jogador);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.danoCausado = tag.getBoolean("DealtDamage");
        this.entityData.set(ID_LEALDADE, lealdadeDoItem(this.getPickupItemStackOrigin()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", this.danoCausado);
    }

    private byte lealdadeDoItem(ItemStack stack) {
        return this.level() instanceof ServerLevel serverLevel
                ? (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, this), 0, 127)
                : 0;
    }

    @Override
    public void tickDespawn() {
        int lealdade = this.entityData.get(ID_LEALDADE);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || lealdade <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
