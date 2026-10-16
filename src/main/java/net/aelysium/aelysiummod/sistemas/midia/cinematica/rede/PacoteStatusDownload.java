package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.GerenciadorVideoServidor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteStatusDownload(
        String nome,
        String tipo,
        int status,
        String mensagem
) implements CustomPacketPayload {

    public static final int STATUS_JA_TEM = 0;
    public static final int STATUS_SUCESSO = 1;
    public static final int STATUS_FALHA_HASH = 2;
    public static final int STATUS_FALHA_IO = 3;
    public static final int STATUS_CANCELADO = 4;

    public static final Type<PacoteStatusDownload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "status_download_video"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteStatusDownload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PacoteStatusDownload::nome,
                    ByteBufCodecs.STRING_UTF8, PacoteStatusDownload::tipo,
                    ByteBufCodecs.VAR_INT, PacoteStatusDownload::status,
                    ByteBufCodecs.STRING_UTF8, PacoteStatusDownload::mensagem,
                    PacoteStatusDownload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteStatusDownload packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                GerenciadorVideoServidor.registrarStatus(sp, packet.nome(), packet.tipo(), packet.status(), packet.mensagem());
            }
        });
    }
}
