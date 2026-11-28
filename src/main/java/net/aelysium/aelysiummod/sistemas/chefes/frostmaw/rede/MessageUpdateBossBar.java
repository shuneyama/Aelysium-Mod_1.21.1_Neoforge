package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.ComumFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.cliente.gui.CustomBossBar;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MessageUpdateBossBar(UUID bossId, boolean remove, ResourceLocation registryName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageUpdateBossBar> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ComumFrostmaw.MODID, "message_update_boss_bar"));
    public static final StreamCodec<ByteBuf, MessageUpdateBossBar> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            MessageUpdateBossBar::bossId,
            ByteBufCodecs.BOOL,
            MessageUpdateBossBar::remove,
            ResourceLocation.STREAM_CODEC,
            MessageUpdateBossBar::registryName,
            MessageUpdateBossBar::new
    );

    private static final ResourceLocation NULL = ResourceLocation.fromNamespaceAndPath(ComumFrostmaw.MODID, "null");

    public static MessageUpdateBossBar fromEntity(UUID bossId, LivingEntity entity) {
        if (entity == null) {
            return new MessageUpdateBossBar(bossId, true, NULL);
        }

        ResourceLocation registryName = entity.level().registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getKey(entity.getType());
        return new MessageUpdateBossBar(bossId, false, registryName);
    }

    public static void handleClient(final MessageUpdateBossBar packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.remove()) {
                CustomBossBar.bossBarRegistryNames.remove(packet.bossId());
            } else {
                CustomBossBar.bossBarRegistryNames.put(packet.bossId(), packet.registryName());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
