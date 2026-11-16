package net.aelysium.aelysiummod.nucleo.texto.mixin;

import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class PacoteTextoMixin {

    @ModifyVariable(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Packet<?> aelysium$converterCodigos(Packet<?> pacote) {
        return CodigosEstilo.converterPacote(pacote);
    }
}
