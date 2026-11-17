package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bat.class)
public abstract class MixinMorcego {

    private static final TagKey<Block> aelysium$MORCEGOS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "bats_spawnable_on"));

    @Inject(method = "checkBatSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void aelysium$regrasSpawn(EntityType<Bat> tipo, LevelAccessor nivel, MobSpawnType razao, BlockPos pos, RandomSource aleatorio, CallbackInfoReturnable<Boolean> cir) {
        boolean resultado;
        if (pos.getY() >= nivel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY()) {
            resultado = false;
        } else if (aleatorio.nextBoolean()) {
            resultado = false;
        } else if (nivel.getMaxLocalRawBrightness(pos) > aleatorio.nextInt(4)) {
            resultado = false;
        } else if (!nivel.getBlockState(pos.below()).is(aelysium$MORCEGOS)) {
            resultado = false;
        } else {
            resultado = Mob.checkMobSpawnRules(tipo, nivel, razao, pos, aleatorio);
        }
        cir.setReturnValue(resultado);
    }
}
