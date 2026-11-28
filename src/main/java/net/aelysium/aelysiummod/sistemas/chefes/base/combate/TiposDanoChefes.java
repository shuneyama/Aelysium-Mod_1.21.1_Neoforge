package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class TiposDanoChefes {
    public static final ResourceKey<DamageType> VENENO = chave("veneno_chefe");
    public static final ResourceKey<DamageType> CONGELAMENTO = chave("congelamento_chefe");

    private TiposDanoChefes() {
    }

    private static ResourceKey<DamageType> chave(String nome) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome));
    }

    public static DamageSource veneno(Level nivel, Entity direto, Entity atacante) {
        return criar(nivel, VENENO, direto, atacante);
    }

    public static DamageSource congelamento(Level nivel, Entity direto, Entity atacante) {
        return criar(nivel, CONGELAMENTO, direto, atacante);
    }

    private static DamageSource criar(Level nivel, ResourceKey<DamageType> chave, Entity direto, Entity atacante) {
        return new DamageSource(nivel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(chave), direto, atacante);
    }
}
