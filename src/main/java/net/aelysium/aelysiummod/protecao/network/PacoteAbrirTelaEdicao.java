package net.aelysium.aelysiummod.protecao.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public record PacoteAbrirTelaEdicao(
        String nome,
        BlockPos posicaoMinima,
        BlockPos posicaoMaxima,
        Map<FlagRegiao, Boolean> flags,
        List<UUID> donos,
        String mensagemEntrada,
        String mensagemSaida
) implements CustomPacketPayload {

    public static final Type<PacoteAbrirTelaEdicao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "protecao_abrir_tela_edicao"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAbrirTelaEdicao> CODEC =
            StreamCodec.of(
                    (buf, p) -> p.write(buf),
                    PacoteAbrirTelaEdicao::lerBuf
            );

    private static PacoteAbrirTelaEdicao lerBuf(FriendlyByteBuf buf) {
        String nome = buf.readUtf();
        BlockPos min = buf.readBlockPos();
        BlockPos max = buf.readBlockPos();

        int qtd = buf.readInt();
        Map<FlagRegiao, Boolean> flags = new HashMap<>();
        for (int i = 0; i < qtd; i++) {
            try {
                FlagRegiao flag = FlagRegiao.valueOf(buf.readUtf());
                flags.put(flag, buf.readBoolean());
            } catch (IllegalArgumentException ignored) { buf.readBoolean(); }
        }

        List<UUID> donos = buf.readList(b -> b.readUUID());
        return new PacoteAbrirTelaEdicao(nome, min, max, flags, donos, buf.readUtf(), buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(nome);
        buf.writeBlockPos(posicaoMinima);
        buf.writeBlockPos(posicaoMaxima);
        buf.writeInt(flags.size());
        for (var e : flags.entrySet()) {
            buf.writeUtf(e.getKey().name());
            buf.writeBoolean(e.getValue());
        }
        buf.writeCollection(donos, (b, uuid) -> b.writeUUID(uuid));
        buf.writeUtf(mensagemEntrada);
        buf.writeUtf(mensagemSaida);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static PacoteAbrirTelaEdicao deRegiao(Regiao regiao) {
        return new PacoteAbrirTelaEdicao(
                regiao.getNome(), regiao.getPosicaoMinima(), regiao.getPosicaoMaxima(),
                new HashMap<>(regiao.getFlags()), new ArrayList<>(regiao.getDonos()),
                regiao.getMensagemEntrada(), regiao.getMensagemSaida()
        );
    }

    public static void handle(PacoteAbrirTelaEdicao packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> h = Class.forName("net.aelysium.aelysiummod.protecao.network.ProtecaoClientHandlers");
                h.getMethod("abrirTelaEdicao", PacoteAbrirTelaEdicao.class).invoke(null, packet);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}