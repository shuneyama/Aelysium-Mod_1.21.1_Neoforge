package net.aelysium.aelysiummod.sistemas.chefes.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record PacoteCuraFlor(Vec3 origem, Vec3 destino) implements CustomPacketPayload {

    public static final Type<PacoteCuraFlor> TIPO = new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "chefe_cura_flor"));
    public static final StreamCodec<FriendlyByteBuf, PacoteCuraFlor> CODEC = StreamCodec.of(
            (buf, p) -> {
                PacketUtils.writeVec3(buf, p.origem);
                PacketUtils.writeVec3(buf, p.destino);
            },
            buf -> new PacoteCuraFlor(PacketUtils.readVec3(buf), PacketUtils.readVec3(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TIPO;
    }
}
