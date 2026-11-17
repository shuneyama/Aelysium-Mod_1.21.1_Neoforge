package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.CoresOvelha;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sheep.class)
public abstract class MixinOvelhaCor {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void aelysium$corPorBioma(ServerLevelAccessor mundo, DifficultyInstance dificuldade, MobSpawnType tipo, SpawnGroupData dados, CallbackInfoReturnable<SpawnGroupData> cir) {
        Sheep ovelha = (Sheep) (Object) this;
        ovelha.setColor(CoresOvelha.sortear(mundo, ovelha.blockPosition(), mundo.getRandom()));
    }
}
