package net.aelysium.aelysiummod.sistemas.admin.atalhos.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PacoteAlternarEspiao() implements CustomPacketPayload {

    public static final Type<PacoteAlternarEspiao> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "espiao_alternar"));

    public static final StreamCodec<FriendlyByteBuf, PacoteAlternarEspiao> STREAM_CODEC =
            StreamCodec.of((buf, pkt) -> {}, buf -> new PacoteAlternarEspiao());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
