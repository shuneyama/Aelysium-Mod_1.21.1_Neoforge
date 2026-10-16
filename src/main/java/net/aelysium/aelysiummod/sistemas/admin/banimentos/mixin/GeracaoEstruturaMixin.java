package net.aelysium.aelysiummod.sistemas.admin.banimentos.mixin;

import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public class GeracaoEstruturaMixin {

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void aelysium$bloquearEstruturaDeModNerfado(StructureSet.StructureSelectionEntry entrada,
                                                        StructureManager gerenciador, RegistryAccess registros,
                                                        RandomState estado, StructureTemplateManager modelos, long semente,
                                                        ChunkAccess chunk, ChunkPos posChunk, SectionPos secao,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (!ConfiguracaoModsDesativados.haWorldgenCapado()) return;
        entrada.structure().unwrapKey().ifPresent(chave -> {
            if (ConfiguracaoModsDesativados.isWorldgenCapped(chave.location().getNamespace())) {
                cir.setReturnValue(false);
            }
        });
    }
}
