package net.aelysium.aelysiummod.nucleo.texto.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StringDecomposer.class)
public abstract class CodigoEComercialMixin {

    @WrapMethod(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z")
    private static boolean aelysium$codigos(String texto, int pular, Style inicio, Style reinicio, FormattedCharSink destino, Operation<Boolean> original) {
        CodigosEstilo.Preparado preparado = CodigosEstilo.preparar(texto);
        return original.call(preparado.texto, pular, inicio, reinicio, preparado.envolver(destino));
    }
}
