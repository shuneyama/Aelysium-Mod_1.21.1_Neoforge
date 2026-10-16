package net.aelysium.aelysiummod.sistemas.mundo.porta.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem;
import net.aelysium.aelysiummod.sistemas.mundo.porta.TrancavelPorSenha;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteVincularCartao(
        BlockPos pos
) implements CustomPacketPayload {

    public static final Type<PacoteVincularCartao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "porta_senha_vincular_cartao"));

    public static final StreamCodec<FriendlyByteBuf, PacoteVincularCartao> CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeBlockPos(p.pos),
                    buf -> new PacoteVincularCartao(buf.readBlockPos())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteVincularCartao packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (!(player.level().getBlockEntity(packet.pos()) instanceof TrancavelPorSenha be)) return;
            if (!be.getSenha().isEmpty()) return;

            ItemStack maoPrincipal = player.getMainHandItem();
            if (!(maoPrincipal.getItem() instanceof CartaoItem) || CartaoItem.estaVinculado(maoPrincipal)) return;

            CartaoItem.vincular(maoPrincipal, be.getId(), be.getTipoNome());
        });
    }
}
