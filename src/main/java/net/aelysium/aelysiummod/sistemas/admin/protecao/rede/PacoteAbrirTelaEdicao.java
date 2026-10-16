package net.aelysium.aelysiummod.sistemas.admin.protecao.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public record PacoteAbrirTelaEdicao(
        String nome,
        BlockPos posicaoMinima,
        BlockPos posicaoMaxima,
        Map<FlagRegiao, Boolean> flags,
        List<UUID> donos,
        List<String> donosNomes,
        boolean donoOps,
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
        List<String> donosNomes = buf.readList(FriendlyByteBuf::readUtf);
        boolean donoOps = buf.readBoolean();
        return new PacoteAbrirTelaEdicao(nome, min, max, flags, donos, donosNomes, donoOps, buf.readUtf(), buf.readUtf());
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
        buf.writeCollection(donosNomes, FriendlyByteBuf::writeUtf);
        buf.writeBoolean(donoOps);
        buf.writeUtf(mensagemEntrada);
        buf.writeUtf(mensagemSaida);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static PacoteAbrirTelaEdicao deRegiao(MinecraftServer server, Regiao regiao) {
        List<UUID> donos = new ArrayList<>(regiao.getDonos());
        List<String> donosNomes = new ArrayList<>();
        for (UUID uuid : donos) donosNomes.add(PerfilJogador.apelido().nomeExibicao(server, uuid));

        return new PacoteAbrirTelaEdicao(
                regiao.getNome(), regiao.getPosicaoMinima(), regiao.getPosicaoMaxima(),
                new HashMap<>(regiao.getFlags()), donos, donosNomes, regiao.isDonoOps(),
                regiao.getMensagemEntrada(), regiao.getMensagemSaida()
        );
    }

    public static void handle(PacoteAbrirTelaEdicao packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.ManipuladoresProtecaoCliente");
                h.getMethod("handleAbrirTelaEdicao", PacoteAbrirTelaEdicao.class, IPayloadContext.class)
                        .invoke(null, packet, context);
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
