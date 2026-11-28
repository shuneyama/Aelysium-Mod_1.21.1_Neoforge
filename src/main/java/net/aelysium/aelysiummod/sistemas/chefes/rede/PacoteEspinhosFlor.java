package net.aelysium.aelysiummod.sistemas.chefes.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PacoteEspinhosFlor(int entidade, List<BlockPos> posicoes) implements CustomPacketPayload {

    public static final Type<PacoteEspinhosFlor> TIPO = new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "chefe_espinhos_flor"));
    public static final StreamCodec<FriendlyByteBuf, PacoteEspinhosFlor> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacoteEspinhosFlor::entidade,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), PacoteEspinhosFlor::posicoes,
            PacoteEspinhosFlor::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TIPO;
    }
}
