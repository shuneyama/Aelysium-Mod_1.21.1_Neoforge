package net.aelysium.aelysiummod.nucleo.texto.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.aelysium.aelysiummod.nucleo.texto.EfeitosTexto;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

    @WrapMethod(method = "accept")
    private boolean aelysium$efeito(int posicao, Style estilo, int codigo, Operation<Boolean> original) {
        ResourceLocation fonte = estilo.getFont();
        if (fonte == null || !"aelysiummod".equals(fonte.getNamespace())) {
            return original.call(posicao, estilo, codigo);
        }
        int efeito = CodigosEstilo.indiceEfeito(fonte);
        if (efeito < 0) return original.call(posicao, estilo, codigo);

        double t = EfeitosTexto.segundos();
        float xInicial = this.x;
        float yInicial = this.y;
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

        this.x = xInicial + dx;
        this.y = yInicial + dy;
        boolean continuar;
        try {
            continuar = original.call(posicao, aplicado, codigo);
        } finally {
            this.x -= dx;
            this.y = yInicial;
        }
        return continuar;
    }
}
