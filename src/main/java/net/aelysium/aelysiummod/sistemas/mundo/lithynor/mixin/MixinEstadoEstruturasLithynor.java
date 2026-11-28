package net.aelysium.aelysiummod.sistemas.mundo.lithynor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.FonteBiomasLithynor;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(ChunkGeneratorStructureState.class)
public abstract class MixinEstadoEstruturasLithynor {

    @WrapOperation(method = "hasBiomesForStructureSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/BiomeSource;possibleBiomes()Ljava/util/Set;"))
    private static Set<Holder<Biome>> aelysium$biomasConjuntoLithynor(BiomeSource fonte, Operation<Set<Holder<Biome>>> original) {
        return fonte instanceof FonteBiomasLithynor lithynor ? lithynor.biomasParaEstruturas() : original.call(fonte);
    }

    @WrapOperation(method = "generatePositions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/BiomeSource;possibleBiomes()Ljava/util/Set;"))
    private Set<Holder<Biome>> aelysium$biomasPosicoesLithynor(BiomeSource fonte, Operation<Set<Holder<Biome>>> original) {
        return fonte instanceof FonteBiomasLithynor lithynor ? lithynor.biomasParaEstruturas() : original.call(fonte);
    }
}
