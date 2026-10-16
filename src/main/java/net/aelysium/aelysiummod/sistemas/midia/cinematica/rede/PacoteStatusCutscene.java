package net.aelysium.aelysiummod.sistemas.midia.cinematica.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.midia.cinematica.RelatorioCutscene;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteStatusCutscene(
        String nome,
        int fase,
        int percentual,
        String mensagem
) implements CustomPacketPayload {

    public static final int FASE_PREPARANDO = 0;
    public static final int FASE_PROGRESSO = 1;
    public static final int FASE_REPRODUZINDO = 2;
    public static final int FASE_FALHA = 3;

    public static final Type<PacoteStatusCutscene> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "status_cutscene"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteStatusCutscene> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PacoteStatusCutscene::nome,
                    ByteBufCodecs.VAR_INT, PacoteStatusCutscene::fase,
                    ByteBufCodecs.VAR_INT, PacoteStatusCutscene::percentual,
                    ByteBufCodecs.STRING_UTF8, PacoteStatusCutscene::mensagem,
                    PacoteStatusCutscene::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteStatusCutscene packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                RelatorioCutscene.aoStatusDoCliente(sp, packet);
            }
        });
    }
}
