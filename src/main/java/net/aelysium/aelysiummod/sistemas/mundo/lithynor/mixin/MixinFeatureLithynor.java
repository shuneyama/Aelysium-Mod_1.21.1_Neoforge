package net.aelysium.aelysiummod.sistemas.mundo.lithynor.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.lithynor.RestricoesLithynor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
public abstract class MixinFeatureLithynor {

    @Inject(method = "placeWithContext", at = @At("HEAD"), cancellable = true)
    private void aelysium$bloquearFeatureExcluidaLithynor(PlacementContext contexto, RandomSource aleatorio, BlockPos posicao,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (!RestricoesLithynor.ehLithynor(contexto.generator())) return;
        ResourceLocation chave = contexto.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getKey((PlacedFeature) (Object) this);
        if (chave != null && RestricoesLithynor.modExcluido(chave.getNamespace())) cir.setReturnValue(false);
    }
}
