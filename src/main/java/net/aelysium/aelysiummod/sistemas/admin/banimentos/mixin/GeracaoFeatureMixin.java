package net.aelysium.aelysiummod.sistemas.admin.banimentos.mixin;

import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
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
public class GeracaoFeatureMixin {

    @Inject(method = "placeWithContext", at = @At("HEAD"), cancellable = true)
    private void aelysium$bloquearFeatureDeModNerfado(PlacementContext contexto, RandomSource random, BlockPos pos,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (!ConfiguracaoModsDesativados.haWorldgenCapado()) return;
        try {
            var registro = contexto.getLevel().getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            ResourceLocation id = registro.getKey((PlacedFeature) (Object) this);
            if (id != null && ConfiguracaoModsDesativados.isWorldgenCapped(id.getNamespace())) {
                cir.setReturnValue(false);
            }
        } catch (Exception ignored) {}
    }
}
