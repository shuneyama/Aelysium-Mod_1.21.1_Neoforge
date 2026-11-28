package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ItemBlockRenderTypes.class)
public abstract class MixinTiposRenderBlocos {

    @Unique
    private static final Map<ChunkRenderTypeSet, ChunkRenderTypeSet> aelysium$cache = new ConcurrentHashMap<>();

    @ModifyReturnValue(method = "getChunkRenderType", at = @At("RETURN"))
    private static RenderType aelysium$tipo(RenderType original, @Local(argsOnly = true) BlockState estado) {
        if (estado.is(net.minecraft.world.level.block.Blocks.TRIPWIRE)) return RenderType.cutoutMipped();
        if (estado.getBlock() instanceof LeavesBlock && !PredefinicaoGrafica.sodium()) return CacheOpcoes.folhas() ? RenderType.cutoutMipped() : RenderType.solid();
        if (original == RenderType.cutout() && CacheOpcoes.mipmapTodos()) return RenderType.cutoutMipped();
        return original;
    }

    @ModifyReturnValue(method = "getRenderLayers", at = @At("RETURN"))
    private static ChunkRenderTypeSet aelysium$camadas(ChunkRenderTypeSet original, @Local(argsOnly = true) BlockState estado) {
        if (estado.is(net.minecraft.world.level.block.Blocks.TRIPWIRE)) return aelysium$cutoutMipped();
        if (estado.getBlock() instanceof LeavesBlock && !PredefinicaoGrafica.sodium()) return CacheOpcoes.folhas() ? aelysium$cutoutMipped() : aelysium$solido();
        if (original == null || !CacheOpcoes.mipmapTodos() || !original.contains(RenderType.cutout())) return original;
        return aelysium$cache.computeIfAbsent(original, MixinTiposRenderBlocos::aelysium$mipmap);
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
