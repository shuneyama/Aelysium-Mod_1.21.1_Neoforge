package net.aelysium.aelysiummod.sistemas.mundo.minifarol.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.minifarol.EntidadeMiniFarol;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteConfigurarMiniFarol(BlockPos pos, boolean ativo, int cor) implements CustomPacketPayload {

    public static final Type<PacoteConfigurarMiniFarol> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "mini_farol_configurar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteConfigurarMiniFarol> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeBoolean(p.ativo);
                buf.writeInt(p.cor);
            },
            buf -> new PacoteConfigurarMiniFarol(buf.readBlockPos(), buf.readBoolean(), buf.readInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteConfigurarMiniFarol pacote, IPayloadContext contexto) {
        contexto.enqueueWork(() -> {
            if (!(contexto.player() instanceof ServerPlayer jogador)) return;
            if (!jogador.hasPermissions(2)) return;
            if (jogador.distanceToSqr(pacote.pos().getCenter()) > 64.0) return;
            if (jogador.level().getBlockEntity(pacote.pos()) instanceof EntidadeMiniFarol be) {
                be.configurar(pacote.ativo(), pacote.cor());
            }
        });
    }
}
