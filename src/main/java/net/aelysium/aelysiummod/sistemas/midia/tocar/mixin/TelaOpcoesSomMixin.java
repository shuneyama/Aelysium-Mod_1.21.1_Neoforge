package net.aelysium.aelysiummod.sistemas.midia.tocar.mixin;

import net.aelysium.aelysiummod.sistemas.midia.tocar.cliente.ConfiguracaoSomClienteAelysium;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(SoundOptionsScreen.class)
public class TelaOpcoesSomMixin {

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void aelysium$adicionarSliderVolume(CallbackInfo ci) {
        OptionsList list = ((SubTelaOpcoesAccessor) (Object) this).aelysium$getList();
        if (list == null) return;

        OptionInstance<Double> opcaoVolume = new OptionInstance<>(
                "options.aelysiummod.som",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.translatableEscape("mensagem.aelysiummod.tocar.sons_do_servidor", Math.round(value * 100)),
                OptionInstance.UnitDouble.INSTANCE,
                ConfiguracaoSomClienteAelysium.getVolumeNormalizado(),
                ConfiguracaoSomClienteAelysium::setVolumeNormalizado
        );
        list.addSmall(opcaoVolume);
    }
}
