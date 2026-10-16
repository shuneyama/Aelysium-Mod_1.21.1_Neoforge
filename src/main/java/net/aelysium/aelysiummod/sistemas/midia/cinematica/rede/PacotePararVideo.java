package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacotePararVideo() implements CustomPacketPayload {

    public static final Type<PacotePararVideo> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "parar_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacotePararVideo> CODEC =
            StreamCodec.unit(new PacotePararVideo());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacotePararVideo packet, IPayloadContext context) {
        try {
            Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente.ManipuladoresCinematicaCliente");
            h.getMethod("handlePararVideo", PacotePararVideo.class, IPayloadContext.class)
                    .invoke(null, packet, context);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
