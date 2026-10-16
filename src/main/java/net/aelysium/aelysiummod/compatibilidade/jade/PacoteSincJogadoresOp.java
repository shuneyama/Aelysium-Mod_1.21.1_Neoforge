package net.aelysium.aelysiummod.compatibilidade.jade;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record PacoteSincJogadoresOp(Set<UUID> opPlayers) implements CustomPacketPayload {

    public static final Type<PacoteSincJogadoresOp> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "op_players_sync"));

    public static final StreamCodec<ByteBuf, PacoteSincJogadoresOp> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.fromCodec(UUIDUtil.CODEC)),
            PacoteSincJogadoresOp::opPlayers,
            PacoteSincJogadoresOp::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteSincJogadoresOp packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            GerenciadorJogadoresOp.setOpPlayers(packet.opPlayers());
            if (net.neoforged.fml.ModList.get().isLoaded("emi")) {
                net.aelysium.aelysiummod.compatibilidade.emi.PonteAelysiumEmi.atualizarSeNecessario();
            }
            if (net.neoforged.fml.ModList.get().isLoaded("jei")) {
                net.aelysium.aelysiummod.compatibilidade.jei.PonteAelysiumJei.atualizarSeNecessario();
            }
        });
    }
}