package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.dados.FrozenData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MessageFreezeEffect(int entityId, boolean isFrozen) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageFreezeEffect> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ComumFrostmaw.MODID, "message_freeze_effect"));
    public static final StreamCodec<ByteBuf, MessageFreezeEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MessageFreezeEffect::entityId,
            ByteBufCodecs.BOOL,
            MessageFreezeEffect::isFrozen,
            MessageFreezeEffect::new
    );

    public static void handleClient(final MessageFreezeEffect packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = ComumFrostmaw.PROXY.getClientLevel();

            if (level != null && level.getEntity(packet.entityId()) instanceof LivingEntity living) {
                FrozenData data = living.getData(RegistroFrostmaw.FROZEN_DATA);

                if (packet.isFrozen()) {
                    data.onFreeze(living);
                } else {
                    data.onUnfreeze(living);
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
