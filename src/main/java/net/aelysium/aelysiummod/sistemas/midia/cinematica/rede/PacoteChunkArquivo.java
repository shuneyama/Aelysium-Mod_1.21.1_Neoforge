package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteChunkArquivo(
        String nome,
        String tipo,
        int indice,
        byte[] dados
) implements CustomPacketPayload {

    public static final Type<PacoteChunkArquivo> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "chunk_arquivo_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteChunkArquivo> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PacoteChunkArquivo::nome,
                    ByteBufCodecs.STRING_UTF8, PacoteChunkArquivo::tipo,
                    ByteBufCodecs.VAR_INT, PacoteChunkArquivo::indice,
                    ByteBufCodecs.BYTE_ARRAY, PacoteChunkArquivo::dados,
                    PacoteChunkArquivo::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteChunkArquivo packet, IPayloadContext context) {
        try {
            Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente.ManipuladoresCinematicaCliente");
            h.getMethod("handleChunkArquivo", PacoteChunkArquivo.class, IPayloadContext.class)
                    .invoke(null, packet, context);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
