package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteTocarVideo(
        String nome,
        int fadeInTicks,
        int fadeOutTicks,
        long startDelayMillis
) implements CustomPacketPayload {

    public static final Type<PacoteTocarVideo> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "tocar_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteTocarVideo> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PacoteTocarVideo::nome,
                    ByteBufCodecs.VAR_INT, PacoteTocarVideo::fadeInTicks,
                    ByteBufCodecs.VAR_INT, PacoteTocarVideo::fadeOutTicks,
                    ByteBufCodecs.VAR_LONG, PacoteTocarVideo::startDelayMillis,
                    PacoteTocarVideo::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteTocarVideo packet, IPayloadContext context) {
        try {
            Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente.ManipuladoresCinematicaCliente");
            h.getMethod("handleTocarVideo", PacoteTocarVideo.class, IPayloadContext.class)
                    .invoke(null, packet, context);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
