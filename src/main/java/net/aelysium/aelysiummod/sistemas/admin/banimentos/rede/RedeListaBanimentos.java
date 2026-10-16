package net.aelysium.aelysiummod.sistemas.admin.banimentos.rede;

import com.mojang.logging.LogUtils;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.DadosBanimentoJogador;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class RedeListaBanimentos {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation SYNC_BANNED_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "banlist_sync_banned");
    public static final ResourceLocation SYNC_ALLOWED_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "banlist_sync_allowed");

    public record PacoteSincItensBanidos(Set<ResourceLocation> bannedItems) implements CustomPacketPayload {
        public static final Type<PacoteSincItensBanidos> TYPE = new Type<>(SYNC_BANNED_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSincItensBanidos> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PacoteSincItensBanidos decode(FriendlyByteBuf buf) {
                int size = buf.readInt();
                Set<ResourceLocation> items = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    items.add(buf.readResourceLocation());
                }
                return new PacoteSincItensBanidos(items);
            }
            @Override
            public void encode(FriendlyByteBuf buf, PacoteSincItensBanidos p) {
                buf.writeInt(p.bannedItems.size());
                for (ResourceLocation rl : p.bannedItems) {
                    buf.writeResourceLocation(rl);
                }
            }
        };
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteSincItensPermitidos(Set<ResourceLocation> allowedItems) implements CustomPacketPayload {
        public static final Type<PacoteSincItensPermitidos> TYPE = new Type<>(SYNC_ALLOWED_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSincItensPermitidos> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PacoteSincItensPermitidos decode(FriendlyByteBuf buf) {
                int size = buf.readInt();
                Set<ResourceLocation> items = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    items.add(buf.readResourceLocation());
                }
                return new PacoteSincItensPermitidos(items);
            }
            @Override
            public void encode(FriendlyByteBuf buf, PacoteSincItensPermitidos p) {
                buf.writeInt(p.allowedItems.size());
                for (ResourceLocation rl : p.allowedItems) {
                    buf.writeResourceLocation(rl);
                }
            }
        };
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void syncToPlayer(ServerPlayer player) {
        Set<ResourceLocation> banned = ConfiguracaoListaBanimentos.getBannedItems();
        PacketDistributor.sendToPlayer(player, new PacoteSincItensBanidos(banned));

        DadosBanimentoJogador data = DadosBanimentoJogador.get(player.getServer());
        Set<ResourceLocation> allowed = data.getAllowedItems(player.getUUID());
        PacketDistributor.sendToPlayer(player, new PacoteSincItensPermitidos(allowed));

        LOGGER.info("[Banlist] Enviando sync para {}: {} banidos, {} permitidos.",
                player.getGameProfile().getName(), banned.size(), allowed.size());
    }

    public static void syncAllPlayers(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }
}
