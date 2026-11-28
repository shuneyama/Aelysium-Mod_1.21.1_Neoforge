package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.entidade.base.ILinkedEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MessageLinkEntities(int sourceId, int targetId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageLinkEntities> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ComumFrostmaw.MODID, "message_link_entities"));
    public static final StreamCodec<ByteBuf, MessageLinkEntities> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MessageLinkEntities::sourceId,
            ByteBufCodecs.INT,
            MessageLinkEntities::targetId,
            MessageLinkEntities::new
    );

    public static MessageLinkEntities fromEntity(Entity source, Entity target) {
        if (source instanceof ILinkedEntity) {
            return new MessageLinkEntities(source.getId(), target.getId());
        }

        return new MessageLinkEntities(-1, -1);
    }

    public static void handleClient(final MessageLinkEntities packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = ComumFrostmaw.PROXY.getClientLevel();

            if (level != null) {
                Entity entitySource = level.getEntity(packet.sourceId());
                Entity entityTarget = level.getEntity(packet.targetId());

                if (entitySource instanceof ILinkedEntity linked && entityTarget != null) {
                    linked.link(entityTarget);
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
