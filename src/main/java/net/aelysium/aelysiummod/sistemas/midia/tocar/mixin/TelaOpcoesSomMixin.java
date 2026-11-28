package net.aelysium.aelysiummod.sistemas.midia.tocar.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.aelysium.aelysiummod.sistemas.midia.tocar.cliente.ConfiguracaoSomClienteAelysium;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.EventosOpcoesCliente;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
@Mixin(SoundOptionsScreen.class)
public class TelaOpcoesSomMixin {

    @ModifyExpressionValue(method = "addOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/options/SoundOptionsScreen;getAllSoundOptionsExceptMaster()[Lnet/minecraft/client/OptionInstance;"))
    private OptionInstance<?>[] aelysium$adicionarFaixas(OptionInstance<?>[] originais) {
        OptionInstance<?>[] faixas = Arrays.copyOf(originais, originais.length + 2);
        faixas[originais.length] = aelysium$volumeServidor();
        faixas[originais.length + 1] = EventosOpcoesCliente.volumeInterface();
        return faixas;
    }

    @Unique
    private static OptionInstance<Double> aelysium$volumeServidor() {
        return new OptionInstance<>(
                "options.aelysiummod.som",
                OptionInstance.noTooltip(),
                (caption, value) -> Component.translatableEscape("mensagem.aelysiummod.tocar.sons_do_servidor", Math.round(value * 100)),
                OptionInstance.UnitDouble.INSTANCE,
                ConfiguracaoSomClienteAelysium.getVolumeNormalizado(),
                ConfiguracaoSomClienteAelysium::setVolumeNormalizado
        );
    }
}
