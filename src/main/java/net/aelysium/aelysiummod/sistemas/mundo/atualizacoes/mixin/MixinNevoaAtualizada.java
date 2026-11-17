package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class MixinNevoaAtualizada {

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Unique
    private static boolean aelysium$terreno;
    @Unique
    private static boolean aelysium$atmosfera;
    @Unique
    private static boolean aelysium$nevoaMundo;
    @Unique
    private static float aelysium$distancia;
    @Unique
    private static float aelysium$chuva;
    @Unique
    private static float aelysium$banda;
    @Unique
    private static long aelysium$ultimoTempo;

    @Inject(method = "setupFog", at = @At("HEAD"))
    private static void aelysium$preparar(Camera camera, FogRenderer.FogMode modo, float distancia, boolean nevoaMundo, float parcial, CallbackInfo ci) {
        Entity entidade = camera.getEntity();
        boolean efeito = entidade instanceof LivingEntity vivo && (vivo.hasEffect(MobEffects.BLINDNESS) || vivo.hasEffect(MobEffects.DARKNESS));
        aelysium$terreno = modo == FogRenderer.FogMode.FOG_TERRAIN;
        aelysium$atmosfera = camera.getFluidInCamera() == FogType.NONE && !efeito;
        aelysium$nevoaMundo = nevoaMundo;
        aelysium$distancia = distancia;
        if (aelysium$atmosfera && aelysium$terreno && Minecraft.getInstance().level != null) {
            ClientLevel mundo = Minecraft.getInstance().level;
            BlockPos pos = camera.getBlockPosition();
            float luz = Mth.clamp((mundo.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) - 8.0F) / 7.0F, 0.0F, 1.0F);
            boolean chove = mundo.getBiome(pos).value().hasPrecipitation();
            float alvo = mundo.getRainLevel(parcial) * luz * (chove ? 1.0F : 0.5F);
            long agora = Util.getMillis();
            float passos = Math.min(5.0F, (agora - aelysium$ultimoTempo) / 50.0F);
            aelysium$ultimoTempo = agora;
            aelysium$chuva += (alvo - aelysium$chuva) * Math.min(1.0F, passos * 0.2F);
            float t = Mth.clamp((aelysium$chuva - 0.5F) * 2.0F, 0.0F, 1.0F);
            float alvoBanda = t * t * (3.0F - 2.0F * t);
            aelysium$banda += (alvoBanda - aelysium$banda) * Math.min(1.0F, passos * 0.06F);
        }
    }

    @Unique
    private static float aelysium$reducao() {
        return aelysium$banda;
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
    private static float aelysium$inicio(float inicio) {
        if (!aelysium$atmosfera || !aelysium$terreno) return inicio;
        if (aelysium$nevoaMundo) return Math.min(inicio, 10.0F);
        return inicio - 160.0F * aelysium$reducao();
    }

    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V"))
    private static float aelysium$fim(float fim) {
        if (!aelysium$atmosfera || !aelysium$terreno) return fim;
        if (aelysium$nevoaMundo) return Math.min(fim, Math.min(96.0F, aelysium$distancia));
        return Math.max(Math.min(96.0F, fim), fim - 256.0F * aelysium$reducao());
    }

    @Inject(method = "levelFogColor", at = @At("RETURN"))
    private static void aelysium$codificarChuva(CallbackInfo ci) {
        float multiplicador = aelysium$atmosfera ? Mth.clamp(aelysium$chuva, 0.0F, 1.0F) : 0.0F;
        RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue, 1.0F + multiplicador);
    }

    @Inject(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;getPriorityFogFunction(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/FogRenderer$MobEffectFogFunction;"))
    private static void aelysium$corAgua(Camera camera, float parcial, ClientLevel mundo, int distancia, float chefe, CallbackInfo ci) {
        if (camera.getFluidInCamera() != FogType.WATER) return;
        BiomeManager biomas = mundo.getBiomeManager();
        Vec3 origem = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
        Vec3 cor = CubicSampler.gaussianSampleVec3(origem, (x, y, z) -> Vec3.fromRGB24(biomas.getNoiseBiomeAtQuart(x, y, z).value().getWaterFogColor()));
        fogRed = (float) cor.x();
        fogGreen = (float) cor.y();
        fogBlue = (float) cor.z();
    }
}
