package net.aelysium.aelysiummod.protecao.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public record PacoteListaRegioes(
        List<RegiaoResumo> regioes
) implements CustomPacketPayload {

    public static final Type<PacoteListaRegioes> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "protecao_lista_regioes"));

    public static final StreamCodec<FriendlyByteBuf, PacoteListaRegioes> CODEC =
            StreamCodec.of(
                    (buf, p) -> p.write(buf),
                    PacoteListaRegioes::lerBuf
            );

    public record RegiaoResumo(
            String nome,
            BlockPos posicaoMinima,
            BlockPos posicaoMaxima,
            Map<FlagRegiao, Boolean> flags,
            List<UUID> donos,
            String mensagemEntrada,
            String mensagemSaida
    ) {}

    private static PacoteListaRegioes lerBuf(FriendlyByteBuf buf) {
        int qtd = buf.readInt();
        List<RegiaoResumo> lista = new ArrayList<>();
        for (int i = 0; i < qtd; i++) {
            String nome = buf.readUtf();
            BlockPos min = buf.readBlockPos();
            BlockPos max = buf.readBlockPos();

            int qtdFlags = buf.readInt();
            Map<FlagRegiao, Boolean> flags = new HashMap<>();
            for (int j = 0; j < qtdFlags; j++) {
                try {
                    FlagRegiao flag = FlagRegiao.valueOf(buf.readUtf());
                    flags.put(flag, buf.readBoolean());
                } catch (IllegalArgumentException ignored) { buf.readBoolean(); }
            }

            List<UUID> donos = buf.readList(b -> b.readUUID());
            String entrada = buf.readUtf();
            String saida = buf.readUtf();

            lista.add(new RegiaoResumo(nome, min, max, flags, donos, entrada, saida));
        }
        return new PacoteListaRegioes(lista);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(regioes.size());
        for (RegiaoResumo r : regioes) {
            buf.writeUtf(r.nome());
            buf.writeBlockPos(r.posicaoMinima());
            buf.writeBlockPos(r.posicaoMaxima());
            buf.writeInt(r.flags().size());
            for (var e : r.flags().entrySet()) {
                buf.writeUtf(e.getKey().name());
                buf.writeBoolean(e.getValue());
            }
            buf.writeCollection(r.donos(), (b, uuid) -> b.writeUUID(uuid));
            buf.writeUtf(r.mensagemEntrada());
            buf.writeUtf(r.mensagemSaida());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacoteListaRegioes packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> h = Class.forName("net.aelysium.aelysiummod.protecao.network.ProtecaoClientHandlers");
                h.getMethod("handleListaRegioes", PacoteListaRegioes.class, IPayloadContext.class)
                        .invoke(null, packet, context);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}