package net.aelysium.aelysiummod.sistemas.admin.protecao.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteDeletarRegiao(
        String nomeRegiao
) implements CustomPacketPayload {

    public static final Type<PacoteDeletarRegiao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "protecao_deletar_regiao"));

    public static final StreamCodec<FriendlyByteBuf, PacoteDeletarRegiao> CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeUtf(p.nomeRegiao()),
                    buf -> new PacoteDeletarRegiao(buf.readUtf())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PacoteDeletarRegiao packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer jogador = (ServerPlayer) context.player();

            if (!jogador.hasPermissions(2)) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_precisa_ser_op_para_deletar"));
                return;
            }

            GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
            if (!gerenciador.existeRegiao(packet.nomeRegiao())) {
                jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_nao_existe", packet.nomeRegiao()));
                return;
            }

            gerenciador.removerRegiao(packet.nomeRegiao());
            jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.regiao_deletada_com_sucesso", packet.nomeRegiao()));
        });
    }
}