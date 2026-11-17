package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAtualizados;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinBolhasAr {

    @Unique
    private static final ResourceLocation aelysium$AR = ResourceLocation.withDefaultNamespace("hud/air");
    @Unique
    private static final ResourceLocation aelysium$ESTOURANDO = ResourceLocation.withDefaultNamespace("hud/air_bursting");
    @Unique
    private static final ResourceLocation aelysium$VAZIO = ResourceLocation.withDefaultNamespace("hud/air_empty");

    @Unique
    private int aelysium$ultimaBolha;

    @Shadow
    public int rightHeight;
    @Shadow
    private int tickCount;
    @Shadow
    private RandomSource random;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Inject(method = "renderAirLevel", at = @At("HEAD"), cancellable = true)
    private void aelysium$bolhas(GuiGraphics gui, CallbackInfo ci) {
        ci.cancel();
        Player jogador = this.getCameraPlayer();
        if (jogador == null) return;
        int xDireita = gui.guiWidth() / 2 + 91;
        int maximo = jogador.getMaxAirSupply();
        int atual = Mth.clamp(jogador.getAirSupply(), 0, maximo);
        boolean naAgua = jogador.isEyeInFluid(FluidTags.WATER);
        if (!naAgua && atual >= maximo) return;
        int y = gui.guiHeight() - this.rightHeight;
        int atraso = atual != 0 && naAgua ? 1 : 0;
        int cheias = aelysium$bolha(atual, maximo, -2);
        int posicaoEstourando = aelysium$bolha(atual, maximo, 0);
        int vazias = 10 - aelysium$bolha(atual, maximo, atraso);
        boolean estourando = cheias != posicaoEstourando;
        if (!naAgua) this.aelysium$ultimaBolha = 0;
        RenderSystem.enableBlend();
        for (int bolha = 1; bolha <= 10; bolha++) {
            int x = xDireita - (bolha - 1) * 8 - 9;
            if (bolha <= cheias) {
                gui.blitSprite(aelysium$AR, x, y, 9, 9);
            } else if (estourando && bolha == posicaoEstourando && naAgua) {
                gui.blitSprite(aelysium$ESTOURANDO, x, y, 9, 9);
                if (this.aelysium$ultimaBolha != bolha) {
                    float volume = 0.5F + 0.1F * Math.max(0, vazias - 3 + 1);
                    float tom = 1.0F + 0.1F * Math.max(0, vazias - 5 + 1);
                    jogador.playSound(SonsAtualizados.BOLHA_ESTOURAR, volume, tom);
                    this.aelysium$ultimaBolha = bolha;
                }
            } else if (bolha > 10 - vazias) {
                int tremor = vazias == 10 && this.tickCount % 2 == 0 ? this.random.nextInt(2) : 0;
                gui.blitSprite(aelysium$VAZIO, x, y + tremor, 9, 9);
            }
        }
        RenderSystem.disableBlend();
        this.rightHeight += 10;
    }

    @Unique
    private static int aelysium$bolha(int atual, int maximo, int deslocamento) {
        return Mth.ceil((float) ((atual + deslocamento) * 10) / (float) maximo);
    }
}
