package net.aelysium.aelysiummod.sistemas.chefes.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record PacoteColocarFlor(Vec3 posicao) implements CustomPacketPayload {

    public static final Type<PacoteColocarFlor> TIPO = new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "chefe_colocar_flor"));
    public static final StreamCodec<FriendlyByteBuf, PacoteColocarFlor> CODEC = StreamCodec.of(
            (buf, p) -> PacketUtils.writeVec3(buf, p.posicao),
            buf -> new PacoteColocarFlor(PacketUtils.readVec3(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TIPO;
    }
}
