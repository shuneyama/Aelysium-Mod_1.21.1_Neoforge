package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocalPlayer.class)
public abstract class MixinJanelaCorrida {

    @ModifyConstant(method = "aiStep", constant = @Constant(intValue = 7, ordinal = 0))
    private int aelysium$janela(int original) {
        return OpcoesAtualizadas.JANELA_CORRIDA.get();
    }
}
