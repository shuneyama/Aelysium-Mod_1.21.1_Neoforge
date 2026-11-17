package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public abstract class MixinRaioClima {

    @ModifyVariable(method = "renderSnowAndRain", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    private int aelysium$raioBase(int raio) {
        return OpcoesAtualizadas.RAIO_CLIMA.get();
    }

    @ModifyVariable(method = "renderSnowAndRain", at = @At(value = "STORE", ordinal = 1), ordinal = 3)
    private int aelysium$raioDetalhado(int raio) {
        return OpcoesAtualizadas.RAIO_CLIMA.get();
    }
}
