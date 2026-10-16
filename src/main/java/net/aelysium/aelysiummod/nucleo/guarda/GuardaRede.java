package net.aelysium.aelysiummod.nucleo.guarda;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuardaRede {

    public static final ResourceLocation SYNC_ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "guard_sync");

    public record PacoteSincGuarda(boolean autorizado) implements CustomPacketPayload {
        public static final Type<PacoteSincGuarda> TYPE = new Type<>(SYNC_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSincGuarda> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, PacoteSincGuarda::autorizado,
                PacoteSincGuarda::new
        );
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new PacoteSincGuarda(AelysiumGuarda.isAutorizado()));
    }
}
