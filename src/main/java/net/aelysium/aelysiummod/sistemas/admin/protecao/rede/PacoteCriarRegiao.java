package net.aelysium.aelysiummod.sistemas.admin.protecao.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public record PacoteCriarRegiao(
        String nome,
        BlockPos posicaoMinima,
        BlockPos posicaoMaxima,
        Map<FlagRegiao, Boolean> flags,
        List<String> nomeDonos,
        boolean donoOps,
        String mensagemEntrada,
        String mensagemSaida
) implements CustomPacketPayload {

    public static final Type<PacoteCriarRegiao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "protecao_criar_regiao"));

    public static final StreamCodec<FriendlyByteBuf, PacoteCriarRegiao> CODEC =
            StreamCodec.of((buf, p) -> p.write(buf), PacoteCriarRegiao::lerBuf);

    private static PacoteCriarRegiao lerBuf(FriendlyByteBuf buf) {
        return new PacoteCriarRegiao(
                buf.readUtf(), buf.readBlockPos(), buf.readBlockPos(),
                lerFlags(buf), buf.readList(FriendlyByteBuf::readUtf),
                buf.readBoolean(),
                buf.readUtf(), buf.readUtf()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(nome);
        buf.writeBlockPos(posicaoMinima);
        buf.writeBlockPos(posicaoMaxima);
        escreverFlags(buf, flags);
        buf.writeCollection(nomeDonos, FriendlyByteBuf::writeUtf);
        buf.writeBoolean(donoOps);
        buf.writeUtf(mensagemEntrada);
        buf.writeUtf(mensagemSaida);
    }

    private static void escreverFlags(FriendlyByteBuf buf, Map<FlagRegiao, Boolean> flags) {
        buf.writeInt(flags.size());
        for (var e : flags.entrySet()) { buf.writeUtf(e.getKey().name()); buf.writeBoolean(e.getValue()); }
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

    public static void handle(PacoteCriarRegiao packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer jogador = (ServerPlayer) context.player();
            if (!jogador.hasPermissions(2)) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_precisa_ser_op_para_criar_regioes"));
                return;
            }

            GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
            if (gerenciador.existeRegiao(packet.nome)) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.ja_existe_uma_regiao_com_esse_nome"));
                return;
            }

            Regiao regiao = new Regiao(packet.nome, packet.posicaoMinima, packet.posicaoMaxima,
                    jogador.level().dimension().location());

            for (var e : packet.flags.entrySet()) regiao.setFlag(e.getKey(), e.getValue());
            regiao.setDonoOps(packet.donoOps);

            List<String> naoEncontrados = new ArrayList<>();
            for (String nomeDono : packet.nomeDonos) {
                UUID uuid = PerfilJogador.apelido().uuidPorNomeOuApelido(jogador.server, nomeDono);
                if (uuid != null) {
                    regiao.adicionarDono(uuid);
                } else {
                    naoEncontrados.add(nomeDono);
                }
            }
            if (!naoEncontrados.isEmpty()) {
                jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.nao_foi_possivel_encontrar", String.join(", ", naoEncontrados)));
            }

            regiao.setMensagemEntrada(packet.mensagemEntrada);
            regiao.setMensagemSaida(packet.mensagemSaida);
            gerenciador.adicionarRegiao(regiao);
            jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_criada_com_sucesso", packet.nome));
        });
    }
}
