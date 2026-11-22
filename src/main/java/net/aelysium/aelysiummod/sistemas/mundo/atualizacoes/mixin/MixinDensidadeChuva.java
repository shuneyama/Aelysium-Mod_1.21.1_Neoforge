package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.CacheOpcoes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class MixinDensidadeChuva {

    @WrapOperation(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
    private int aelysium$densidade(Level nivel, Heightmap.Types tipo, int x, int z, Operation<Integer> original) {
        int altura = original.call(nivel, tipo, x, z);
        int chuva = CacheOpcoes.densidadeChuva();
        int neve = CacheOpcoes.densidadeNeve();
        if (chuva >= 100 && neve >= 100) return altura;
        BlockPos pos = new BlockPos(x, altura, z);
        boolean ehNeve = nivel.getBiome(pos).value().getPrecipitationAt(pos) == Biome.Precipitation.SNOW;
        int porcentagem = ehNeve ? neve : chuva;
        if (porcentagem >= 100) return altura;
        int hash = (x * 73856093) ^ (z * 19349663);
        hash ^= hash >>> 13;
        hash *= 0x5bd1e995;
        hash ^= hash >>> 15;
        if (Math.floorMod(hash, 100) >= porcentagem) return Integer.MAX_VALUE / 2;
        return altura;
    }
}
