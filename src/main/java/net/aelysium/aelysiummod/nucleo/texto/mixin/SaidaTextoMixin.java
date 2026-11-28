package net.aelysium.aelysiummod.nucleo.texto.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.aelysium.aelysiummod.nucleo.texto.EfeitosTexto;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class SaidaTextoMixin {

    @Shadow
    float x;

    @Shadow
    float y;

    @Shadow
    @Final
    private float r;

    @Shadow
    @Final
    private float g;

    @Shadow
    @Final
    private float b;

    @Shadow
    @Final
    private float dimFactor;

    @Unique
    private boolean aelysium$deslocado;

    @Unique
    private float aelysium$dx;

    @Unique
    private float aelysium$yInicial;

    @ModifyVariable(method = "accept", at = @At("HEAD"), argsOnly = true)
    private Style aelysium$efeito(Style estilo) {
        ResourceLocation fonte = estilo.getFont();
        if (!"aelysiummod".equals(fonte.getNamespace())) return estilo;
        int efeito = CodigosEstilo.indiceEfeito(fonte);
        if (efeito < 0) return estilo;

        double t = EfeitosTexto.segundos();
        float xInicial = this.x;
        float dx = EfeitosTexto.deslocamentoX(efeito, t, xInicial);
        float dy = EfeitosTexto.deslocamentoY(efeito, t, xInicial);

        Style aplicado = estilo;
        if (EfeitosTexto.usaCor(efeito)) {
            TextColor atual = estilo.getColor();
            int base;
            if (atual != null) {
                base = atual.getValue();
            } else {
                float fator = this.dimFactor == 0.0f ? 1.0f : 1.0f / this.dimFactor;
                base = (Math.min(255, Math.round(this.r * fator * 255.0f)) << 16)
                        | (Math.min(255, Math.round(this.g * fator * 255.0f)) << 8)
                        | Math.min(255, Math.round(this.b * fator * 255.0f));
            }
            aplicado = estilo.withColor(EfeitosTexto.cor(efeito, t, xInicial, base));
        }

        aelysium$deslocado = true;
        aelysium$dx = dx;
        aelysium$yInicial = this.y;
        this.x = xInicial + dx;
        this.y = this.y + dy;
        return aplicado;
    }

    @ModifyReturnValue(method = "accept", at = @At("RETURN"))
    private boolean aelysium$restaurar(boolean continuar) {
        if (aelysium$deslocado) {
            aelysium$deslocado = false;
            this.x -= aelysium$dx;
            this.y = aelysium$yInicial;
        }
        return continuar;
    }
}
