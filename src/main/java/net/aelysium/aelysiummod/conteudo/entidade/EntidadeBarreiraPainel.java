package net.aelysium.aelysiummod.conteudo.entidade;

import net.aelysium.aelysiummod.conteudo.item.escudo.GerenciadorBarreira;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class EntidadeBarreiraPainel extends Entity {

    private static final EntityDataAccessor<Byte> FACE = SynchedEntityData.defineId(EntidadeBarreiraPainel.class, EntityDataSerializers.BYTE);

    private static final double METADE_ESPESSURA = 0.15;
    private static final double METADE_LADO = 2.65;

    private UUID dono;

    public EntidadeBarreiraPainel(EntityType<? extends EntidadeBarreiraPainel> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    public void configurar(UUID dono, int face) {
        this.dono = dono;
        this.entityData.set(FACE, (byte) face);
        this.setPos(this.getX(), this.getY(), this.getZ());
    }

    public int getFace() {
        return this.entityData.get(FACE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FACE, (byte) 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (FACE.equals(accessor)) {
            this.setPos(this.getX(), this.getY(), this.getZ());
        }
    }

    @Override
    protected AABB makeBoundingBox() {
        int face = this.entityData == null ? 0 : this.entityData.get(FACE);
        double hx = face < 2 ? METADE_ESPESSURA : METADE_LADO;
        double hy = face >= 2 && face < 4 ? METADE_ESPESSURA : METADE_LADO;
        double hz = face >= 4 ? METADE_ESPESSURA : METADE_LADO;
        Vec3 p = this.position();
        return new AABB(p.x - hx, p.y - hy, p.z - hz, p.x + hx, p.y + hy, p.z + hz);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && (this.dono == null || !GerenciadorBarreira.existe(this.dono))) {
            this.discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.dono == null) {
            return false;
        }
        return GerenciadorBarreira.danificar(this.dono, source, amount);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
