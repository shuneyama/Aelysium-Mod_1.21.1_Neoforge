package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class MixinMapaLuz {

    @Shadow
    private boolean updateLightTexture;
    @Shadow
    private float blockLightRedFlicker;
    @Shadow
    @Final
    private NativeImage lightPixels;
    @Shadow
    @Final
    private DynamicTexture lightTexture;
    @Shadow
    @Final
    private GameRenderer renderer;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private native float getDarknessGamma(float parcial);

    @Shadow
    private native float calculateDarknessScale(LivingEntity entidade, float gama, float parcial);

    private static float aelysium$fatorCeu(long tempo) {
        float t = tempo % 24000L;
        if (t < 730.0F) t += 24000.0F;
        if (t <= 11270.0F) return 1.0F;
        if (t < 13140.0F) return Mth.lerp((t - 11270.0F) / 1870.0F, 1.0F, 0.24F);
        if (t <= 22860.0F) return 0.24F;
        return Mth.lerp((t - 22860.0F) / 1870.0F, 0.24F, 1.0F);
    }

    private static float aelysium$mistura(long tempo) {
        float t = tempo % 24000L;
        if (t < 730.0F) t += 24000.0F;
        if (t <= 11270.0F) return 0.0F;
        if (t < 13140.0F) return (t - 11270.0F) / 1870.0F;
        if (t <= 22860.0F) return 1.0F;
        return 1.0F - (t - 22860.0F) / 1870.0F;
    }

    private static float aelysium$brilho(float nivel) {
        return nivel / (4.0F - 3.0F * nivel);
    }

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void aelysium$novoAlgoritmo(float parcial, CallbackInfo ci) {
        ci.cancel();
        if (!this.updateLightTexture) return;
        ClientLevel nivel = this.minecraft.level;
        LocalPlayer jogador = this.minecraft.player;
        if (nivel == null || jogador == null) return;
        this.updateLightTexture = false;

        String efeitos = nivel.dimensionType().effectsLocation().getPath();
        boolean inferno = efeitos.equals("the_nether");
        boolean fim = efeitos.equals("the_end");
        float ambienteR = inferno ? 0x30 / 255.0F : fim ? 0x3F / 255.0F : 0x0A / 255.0F;
        float ambienteG = inferno ? 0x28 / 255.0F : fim ? 0x47 / 255.0F : 0x0A / 255.0F;
        float ambienteB = inferno ? 0x21 / 255.0F : fim ? 0x3F / 255.0F : 0x0A / 255.0F;

        float fatorCeu;
        float ceuR = 1.0F;
        float ceuG = 1.0F;
        float ceuB = 1.0F;
        long tempo = nivel.getDayTime();
        if (inferno) {
            fatorCeu = 0.0F;
            ceuR = 0x7A / 255.0F;
            ceuG = 0x7A / 255.0F;
        } else if (fim) {
            fatorCeu = 0.0F;
            ceuR = 0xAC / 255.0F;
            ceuG = 0x60 / 255.0F;
            ceuB = 0xCD / 255.0F;
        } else if (!nivel.dimensionType().hasSkyLight()) {
            fatorCeu = 0.0F;
        } else {
            fatorCeu = aelysium$fatorCeu(tempo);
            float noite = aelysium$mistura(tempo);
            ceuR = Mth.lerp(noite, 1.0F, 0x7A / 255.0F);
            ceuG = Mth.lerp(noite, 1.0F, 0x7A / 255.0F);
            float chuva = nivel.getRainLevel(1.0F);
            float trovao = nivel.getThunderLevel(1.0F);
            fatorCeu *= (1.0F - chuva * 5.0F / 16.0F) * (1.0F - trovao * 5.0F / 16.0F);
            if (nivel.getSkyFlashTime() > 0) fatorCeu = 1.0F;
        }

        float escalaEscuridao = this.minecraft.options.darknessEffectScale().get().floatValue();
        float gamaEscuridao = this.getDarknessGamma(parcial) * escalaEscuridao;
        float subtracao = this.calculateDarknessScale(jogador, gamaEscuridao, parcial) * escalaEscuridao;
        float visaoAgua = jogador.getWaterVision();
        float visaoNoturna;
        if (jogador.hasEffect(MobEffects.NIGHT_VISION)) {
            visaoNoturna = GameRenderer.getNightVisionScale(jogador, parcial);
        } else if (visaoAgua > 0.0F && jogador.hasEffect(MobEffects.CONDUIT_POWER)) {
            visaoNoturna = visaoAgua;
        } else {
            visaoNoturna = 0.0F;
        }
        float visaoCor = 0.6F * visaoNoturna;
        float baseR = Math.max(ambienteR, visaoCor);
        float baseG = Math.max(ambienteG, visaoCor);
        float baseB = Math.max(ambienteB, visaoCor);
        float fatorBloco = this.blockLightRedFlicker + 1.4F;
        float escurecer = this.renderer.getDarkenWorldAmount(parcial);
        float brilho = Math.max(0.0F, this.minecraft.options.gamma().get().floatValue() - gamaEscuridao);

        for (int ceu = 0; ceu < 16; ceu++) {
            for (int bloco = 0; bloco < 16; bloco++) {
                float nivelCeu = ceu / 15.0F;
                float nivelBloco = bloco / 15.0F;
                float brilhoCeu = aelysium$brilho(nivelCeu) * fatorCeu;
                float brilhoBloco = aelysium$brilho(nivelBloco) * fatorBloco;
                float parabola = (2.0F * nivelBloco - 1.0F) * (2.0F * nivelBloco - 1.0F);
                float mistura = 0.9F * parabola;
                float tomR = Mth.lerp(mistura, 1.0F, 1.0F);
                float tomG = Mth.lerp(mistura, 0.847F, 1.0F);
                float tomB = Mth.lerp(mistura, 0.298F, 1.0F);
                float r = baseR + ceuR * brilhoCeu + tomR * brilhoBloco;
                float g = baseG + ceuG * brilhoCeu + tomG * brilhoBloco;
                float b = baseB + ceuB * brilhoCeu + tomB * brilhoBloco;
                if (escurecer > 0.0F) {
                    r = Mth.lerp(escurecer, r, r * 0.7F);
                    g = Mth.lerp(escurecer, g, g * 0.6F);
                    b = Mth.lerp(escurecer, b, b * 0.6F);
                }
                r = Mth.clamp(r - subtracao, 0.0F, 1.0F);
                g = Mth.clamp(g - subtracao, 0.0F, 1.0F);
                b = Mth.clamp(b - subtracao, 0.0F, 1.0F);
                float maior = Math.max(r, Math.max(g, b));
                if (maior > 0.0F && brilho > 0.0F) {
                    float invertido = 1.0F - maior;
                    float escalado = 1.0F - invertido * invertido * invertido * invertido;
                    float fator = escalado / maior;
                    r = Mth.lerp(brilho, r, r * fator);
                    g = Mth.lerp(brilho, g, g * fator);
                    b = Mth.lerp(brilho, b, b * fator);
                }
                int vr = (int) (Mth.clamp(r, 0.0F, 1.0F) * 255.0F);
                int vg = (int) (Mth.clamp(g, 0.0F, 1.0F) * 255.0F);
                int vb = (int) (Mth.clamp(b, 0.0F, 1.0F) * 255.0F);
                this.lightPixels.setPixelRGBA(bloco, ceu, 0xFF000000 | vb << 16 | vg << 8 | vr);
            }
        }
        this.lightTexture.upload();
    }
}
