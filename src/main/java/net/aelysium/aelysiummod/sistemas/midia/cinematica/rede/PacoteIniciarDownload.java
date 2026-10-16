package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteIniciarDownload(
        String nome,
        String tipo,
        long totalBytes,
        int totalChunks,
        String sha256
) implements CustomPacketPayload {

    public static final Type<PacoteIniciarDownload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "iniciar_download_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteIniciarDownload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PacoteIniciarDownload::nome,
                    ByteBufCodecs.STRING_UTF8, PacoteIniciarDownload::tipo,
                    ByteBufCodecs.VAR_LONG, PacoteIniciarDownload::totalBytes,
                    ByteBufCodecs.VAR_INT, PacoteIniciarDownload::totalChunks,
                    ByteBufCodecs.STRING_UTF8, PacoteIniciarDownload::sha256,
                    PacoteIniciarDownload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteIniciarDownload packet, IPayloadContext context) {
        try {
            Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente.ManipuladoresCinematicaCliente");
            h.getMethod("handleIniciarDownload", PacoteIniciarDownload.class, IPayloadContext.class)
                    .invoke(null, packet, context);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
