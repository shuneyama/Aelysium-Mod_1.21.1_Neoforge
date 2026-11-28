package net.aelysium.aelysiummod.sistemas.mundo.lithynor.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.FonteBiomasLithynor;
import net.aelysium.aelysiummod.sistemas.mundo.lithynor.RestricoesLithynor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureCheck.class)
public abstract class MixinChecagemEstruturaLithynor {

    @Shadow @Final private ChunkGenerator chunkGenerator;
    @Shadow @Final private RegistryAccess registryAccess;

    @Inject(method = "canCreateStructure", at = @At("HEAD"), cancellable = true)
    private void aelysium$filtrarEstruturaLithynor(ChunkPos posChunk, Structure estrutura, CallbackInfoReturnable<Boolean> cir) {
        if (!RestricoesLithynor.ehLithynor(chunkGenerator)) return;
        ResourceLocation chave = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(estrutura);
        if (chave != null && !RestricoesLithynor.estruturaPermitida(chave, posChunk)) cir.setReturnValue(false);
    }

    @ModifyArg(method = "canCreateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/Structure;findValidGenerationPoint(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;)Ljava/util/Optional;"))
    private Structure.GenerationContext aelysium$biomasEquivalentesLithynor(Structure.GenerationContext contexto,
                                                                             @Local(argsOnly = true) ChunkPos posChunk,
                                                                             @Local(argsOnly = true) Structure estrutura) {
        if (!(chunkGenerator.getBiomeSource() instanceof FonteBiomasLithynor fonte)) return contexto;
        ResourceLocation chave = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(estrutura);
        if (chave == null) return contexto;
        return new Structure.GenerationContext(contexto.registryAccess(), contexto.chunkGenerator(), contexto.biomeSource(),
                contexto.randomState(), contexto.structureTemplateManager(), contexto.random(), contexto.seed(),
                contexto.chunkPos(), contexto.heightAccessor(),
                RestricoesLithynor.biomasValidos(contexto.validBiome(), chave, posChunk, fonte));
    }
}
