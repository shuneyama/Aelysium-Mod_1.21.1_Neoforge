package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteFinalizarDownload(String nome, String tipo) implements CustomPacketPayload {

    public static final Type<PacoteFinalizarDownload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "finalizar_download_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteFinalizarDownload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PacoteFinalizarDownload::nome,
                    ByteBufCodecs.STRING_UTF8, PacoteFinalizarDownload::tipo,
                    PacoteFinalizarDownload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteFinalizarDownload packet, IPayloadContext context) {
        try {
            Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente.ManipuladoresCinematicaCliente");
            h.getMethod("handleFinalizarDownload", PacoteFinalizarDownload.class, IPayloadContext.class)
                    .invoke(null, packet, context);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
