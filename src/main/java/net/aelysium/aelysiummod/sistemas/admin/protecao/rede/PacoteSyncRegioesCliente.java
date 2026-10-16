package net.aelysium.aelysiummod.sistemas.admin.protecao.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacoteSyncRegioesCliente(List<RegiaoMin> regioes) implements CustomPacketPayload {

    public record RegiaoMin(String nome, BlockPos min, BlockPos max, ResourceLocation dim,
                             boolean domoAtivo, int domoCor, float domoDistancia) {
        public static final StreamCodec<RegistryFriendlyByteBuf, RegiaoMin> STREAM_CODEC =
                StreamCodec.of(
                        (buf, val) -> {
                            buf.writeUtf(val.nome());
                            buf.writeBlockPos(val.min());
                            buf.writeBlockPos(val.max());
                            buf.writeResourceLocation(val.dim());
                            buf.writeBoolean(val.domoAtivo());
                            buf.writeInt(val.domoCor());
                            buf.writeFloat(val.domoDistancia());
                        },
                        buf -> new RegiaoMin(
                                buf.readUtf(),
                                buf.readBlockPos(),
                                buf.readBlockPos(),
                                buf.readResourceLocation(),
                                buf.readBoolean(),
                                buf.readInt(),
                                buf.readFloat()
                        )
                );
    }

    public static final Type<PacoteSyncRegioesCliente> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "sync_regioes_cliente"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteSyncRegioesCliente> CODEC =
            StreamCodec.of(
                    (buf, val) -> {
                        buf.writeVarInt(val.regioes().size());
                        for (RegiaoMin r : val.regioes()) {
                            RegiaoMin.STREAM_CODEC.encode(buf, r);
                        }
                    },
                    buf -> {
                        int n = buf.readVarInt();
                        List<RegiaoMin> lista = new ArrayList<>(n);
                        for (int i = 0; i < n; i++) {
                            lista.add(RegiaoMin.STREAM_CODEC.decode(buf));
                        }
                        return new PacoteSyncRegioesCliente(lista);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteSyncRegioesCliente packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.ManipuladoresProtecaoCliente");
                h.getMethod("handleSyncRegioes", PacoteSyncRegioesCliente.class, IPayloadContext.class)
                        .invoke(null, packet, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}