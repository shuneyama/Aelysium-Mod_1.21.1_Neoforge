package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.CacheOpcoes;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.PredefinicaoGrafica;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ItemBlockRenderTypes.class)
public abstract class MixinTiposRenderBlocos {

    @Unique
    private static final Map<ChunkRenderTypeSet, ChunkRenderTypeSet> aelysium$cache = new ConcurrentHashMap<>();

    @Inject(method = "getChunkRenderType", at = @At("RETURN"), cancellable = true)
    private static void aelysium$tipo(BlockState estado, CallbackInfoReturnable<RenderType> cir) {
        if (estado.is(net.minecraft.world.level.block.Blocks.TRIPWIRE)) {
            cir.setReturnValue(RenderType.cutoutMipped());
        } else if (estado.getBlock() instanceof LeavesBlock && !PredefinicaoGrafica.sodium()) {
            cir.setReturnValue(CacheOpcoes.folhas() ? RenderType.cutoutMipped() : RenderType.solid());
        } else if (cir.getReturnValue() == RenderType.cutout() && CacheOpcoes.mipmapTodos()) {
            cir.setReturnValue(RenderType.cutoutMipped());
        }
    }

    @Inject(method = "getRenderLayers", at = @At("RETURN"), cancellable = true)
    private static void aelysium$camadas(BlockState estado, CallbackInfoReturnable<ChunkRenderTypeSet> cir) {
        if (estado.is(net.minecraft.world.level.block.Blocks.TRIPWIRE)) {
            cir.setReturnValue(aelysium$cutoutMipped());
            return;
        }
        if (estado.getBlock() instanceof LeavesBlock && !PredefinicaoGrafica.sodium()) {
            cir.setReturnValue(CacheOpcoes.folhas() ? aelysium$cutoutMipped() : aelysium$solido());
            return;
        }
        ChunkRenderTypeSet original = cir.getReturnValue();
        if (original == null || !CacheOpcoes.mipmapTodos() || !original.contains(RenderType.cutout())) return;
        cir.setReturnValue(aelysium$cache.computeIfAbsent(original, MixinTiposRenderBlocos::aelysium$mipmap));
    }

    @Unique
    private static ChunkRenderTypeSet aelysium$cutoutMippedCache;
    @Unique
    private static ChunkRenderTypeSet aelysium$solidoCache;

    @Unique
    private static ChunkRenderTypeSet aelysium$cutoutMipped() {
        ChunkRenderTypeSet conjunto = aelysium$cutoutMippedCache;
        if (conjunto == null) aelysium$cutoutMippedCache = conjunto = ChunkRenderTypeSet.of(RenderType.cutoutMipped());
        return conjunto;
    }

    @Unique
    private static ChunkRenderTypeSet aelysium$solido() {
        ChunkRenderTypeSet conjunto = aelysium$solidoCache;
        if (conjunto == null) aelysium$solidoCache = conjunto = ChunkRenderTypeSet.of(RenderType.solid());
        return conjunto;
    }

    @Unique
    private static ChunkRenderTypeSet aelysium$mipmap(ChunkRenderTypeSet original) {
        List<RenderType> tipos = new ArrayList<>();
        for (RenderType tipo : original) tipos.add(tipo == RenderType.cutout() ? RenderType.cutoutMipped() : tipo);
        return ChunkRenderTypeSet.of(tipos);
    }
}
