package net.aelysium.aelysiummod.protecao.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PacoteEditarRegiao(
        String nomeAtual,
        String nomeNovo,
        BlockPos posicaoMinima,
        BlockPos posicaoMaxima,
        Map<FlagRegiao, Boolean> flags,
        List<String> nomeDonos,
        String mensagemEntrada,
        String mensagemSaida
) implements CustomPacketPayload {

    public static final Type<PacoteEditarRegiao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "protecao_editar_regiao"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEditarRegiao> CODEC =
            StreamCodec.of((buf, p) -> p.write(buf), PacoteEditarRegiao::lerBuf);

    private static PacoteEditarRegiao lerBuf(FriendlyByteBuf buf) {
        return new PacoteEditarRegiao(
                buf.readUtf(), buf.readUtf(), buf.readBlockPos(), buf.readBlockPos(),
                lerFlags(buf), buf.readList(FriendlyByteBuf::readUtf),
                buf.readUtf(), buf.readUtf()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(nomeAtual);
        buf.writeUtf(nomeNovo);
        buf.writeBlockPos(posicaoMinima);
        buf.writeBlockPos(posicaoMaxima);
        buf.writeInt(flags.size());
        for (var e : flags.entrySet()) { buf.writeUtf(e.getKey().name()); buf.writeBoolean(e.getValue()); }
        buf.writeCollection(nomeDonos, FriendlyByteBuf::writeUtf);
        buf.writeUtf(mensagemEntrada);
        buf.writeUtf(mensagemSaida);
    }

    private static Map<FlagRegiao, Boolean> lerFlags(FriendlyByteBuf buf) {
        Map<FlagRegiao, Boolean> flags = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            String n = buf.readUtf(); boolean v = buf.readBoolean();
            try { flags.put(FlagRegiao.valueOf(n), v); } catch (IllegalArgumentException ignored) {}
        }
        return flags;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacoteEditarRegiao packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer jogador = (ServerPlayer) context.player();
            if (!jogador.hasPermissions(2)) {
                jogador.sendSystemMessage(Component.literal("§cVocê precisa ser OP para editar regiões!"));
                return;
            }

            GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
            if (!gerenciador.existeRegiao(packet.nomeAtual)) {
                jogador.sendSystemMessage(Component.literal("§cRegião '" + packet.nomeAtual + "' não existe!"));
                return;
            }
            if (!packet.nomeAtual.equals(packet.nomeNovo) && gerenciador.existeRegiao(packet.nomeNovo)) {
                jogador.sendSystemMessage(Component.literal("§cJá existe uma região com o nome '" + packet.nomeNovo + "'!"));
                return;
            }

            gerenciador.removerRegiao(packet.nomeAtual);

            Regiao regiao = new Regiao(packet.nomeNovo, packet.posicaoMinima, packet.posicaoMaxima,
                    jogador.level().dimension().location());

            for (var e : packet.flags.entrySet()) regiao.setFlag(e.getKey(), e.getValue());
            for (String nomeDono : packet.nomeDonos) {
                ServerPlayer dono = jogador.server.getPlayerList().getPlayerByName(nomeDono);
                if (dono != null) regiao.adicionarDono(dono.getUUID());
            }
            regiao.setMensagemEntrada(packet.mensagemEntrada);
            regiao.setMensagemSaida(packet.mensagemSaida);
            gerenciador.adicionarRegiao(regiao);
            jogador.sendSystemMessage(Component.literal("§aRegião '" + packet.nomeNovo + "' atualizada com sucesso!"));
        });
    }
}
