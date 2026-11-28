package net.aelysium.aelysiummod.sistemas.mundo.lithynor.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.FonteBiomasLithynor;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.RestricoesLithynor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ChunkGenerator.class)
public abstract class MixinGeradorChunkLithynor {

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void aelysium$filtrarEstruturaLithynor(StructureSet.StructureSelectionEntry entrada, StructureManager gerenciador,
                                                    RegistryAccess registros, RandomState estado, StructureTemplateManager modelos,
                                                    long semente, ChunkAccess chunk, ChunkPos posChunk, SectionPos secao,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (!RestricoesLithynor.ehLithynor((ChunkGenerator) (Object) this)) return;
        entrada.structure().unwrapKey().ifPresent(chave -> {
            if (!RestricoesLithynor.estruturaPermitida(chave.location(), posChunk)) cir.setReturnValue(false);
        });
    }

    @ModifyArg(method = "tryGenerateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/Structure;generate(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/LevelHeightAccessor;Ljava/util/function/Predicate;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;"), index = 9)
    private Predicate<Holder<Biome>> aelysium$biomasEquivalentesLithynor(Predicate<Holder<Biome>> original,
                                                                          @Local(argsOnly = true) StructureSet.StructureSelectionEntry entrada,
                                                                          @Local(argsOnly = true) ChunkPos posChunk) {
        if (!(((ChunkGenerator) (Object) this).getBiomeSource() instanceof FonteBiomasLithynor fonte)) return original;
        return entrada.structure().unwrapKey()
                .map(chave -> RestricoesLithynor.biomasValidos(original, chave.location(), posChunk, fonte))
                .orElse(original);
    }

    @ModifyVariable(method = "findNearestMapStructure", at = @At("HEAD"), argsOnly = true)
    private HolderSet<Structure> aelysium$semEstruturasExcluidasLithynor(HolderSet<Structure> estruturas) {
        if (!RestricoesLithynor.ehLithynor((ChunkGenerator) (Object) this)) return estruturas;
        return HolderSet.direct(estruturas.stream()
                .filter(estrutura -> estrutura.unwrapKey().map(chave -> !RestricoesLithynor.estruturaExcluida(chave.location())).orElse(true))
                .toList());
    }
}
