// ========================================
// 1. BloodMoonPacket.java
// ========================================
package net.aelysium.aelysiummod.network;

import io.netty.buffer.ByteBuf;
import net.aelysium.aelysiummod.system.LuaEstado;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LuaVemelhaServidor(boolean isBloodMoon) implements CustomPacketPayload {

    public static final Type<LuaVemelhaServidor> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "blood_moon"));

    public static final StreamCodec<ByteBuf, LuaVemelhaServidor> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(
                            (buf, value) -> buf.writeBoolean(value),
                            ByteBuf::readBoolean
                    ),
                    LuaVemelhaServidor::isBloodMoon,
                    LuaVemelhaServidor::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LuaVemelhaServidor packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Atualiza o estado no cliente
            LuaEstado.bloodMoon = packet.isBloodMoon();
        });
    }
}