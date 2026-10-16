package net.aelysium.aelysiummod.conteudo.entidade;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.aelysium.aelysiummod.registro.ModEntidades;
import net.aelysium.aelysiummod.registro.ModTiposDano;

public class EntidadeBalaGlock extends ThrowableProjectile {

    private static final float DANO = 6.0f;

    public EntidadeBalaGlock(EntityType<? extends EntidadeBalaGlock> type, Level level) {
        super(type, level);
    }

    public EntidadeBalaGlock(Level level, LivingEntity atirador) {
        super(ModEntidades.GLOCK_BALA.get(), atirador, level);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        Holder<DamageType> tipo = this.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModTiposDano.GLOCK);

        DamageSource fonte = new DamageSource(tipo, this, this.getOwner());
        result.getEntity().hurt(fonte, DANO);

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }
}
