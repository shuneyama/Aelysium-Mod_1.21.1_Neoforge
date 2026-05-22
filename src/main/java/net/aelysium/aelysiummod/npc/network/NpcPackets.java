package net.aelysium.aelysiummod.npc.network;

import io.netty.buffer.ByteBuf;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.item.ModItens;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.entity.NpcTradeData;
import net.aelysium.aelysiummod.item.custom.CriadorRegistro;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public class NpcPackets {

    public record OpenNpcEditorS2C(int entityId, CompoundTag npcData) implements CustomPacketPayload {
        public static final Type<OpenNpcEditorS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_open_editor"));
        public static final StreamCodec<FriendlyByteBuf, OpenNpcEditorS2C> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, OpenNpcEditorS2C::entityId,
                        ByteBufCodecs.COMPOUND_TAG, OpenNpcEditorS2C::npcData,
                        OpenNpcEditorS2C::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record UpdateNpcTradesC2S(int entityId, CompoundTag tradesData) implements CustomPacketPayload {
        public static final Type<UpdateNpcTradesC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_update_trades"));
        public static final StreamCodec<FriendlyByteBuf, UpdateNpcTradesC2S> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, UpdateNpcTradesC2S::entityId,
                        ByteBufCodecs.COMPOUND_TAG, UpdateNpcTradesC2S::tradesData,
                        UpdateNpcTradesC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record UpdateNpcSettingsC2S(
            int entityId, String npcName, boolean invulnerable,
            boolean lookAtPlayers, float lookRadius,
            String skinMode, String modelType,
            float scaleX, float scaleY, float scaleZ,
            float posX, float posY, float posZ,
            float rotX, float rotY, float rotZ,
            boolean hasGravity, boolean nameVisible
    ) implements CustomPacketPayload {
        public static final Type<UpdateNpcSettingsC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_update_settings"));
        public static final StreamCodec<FriendlyByteBuf, UpdateNpcSettingsC2S> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public UpdateNpcSettingsC2S decode(FriendlyByteBuf buf) {
                return new UpdateNpcSettingsC2S(
                        buf.readInt(), buf.readUtf(), buf.readBoolean(),
                        buf.readBoolean(), buf.readFloat(), buf.readUtf(), buf.readUtf(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readBoolean(), buf.readBoolean());
            }
            @Override
            public void encode(FriendlyByteBuf buf, UpdateNpcSettingsC2S pkt) {
                buf.writeInt(pkt.entityId); buf.writeUtf(pkt.npcName); buf.writeBoolean(pkt.invulnerable);
                buf.writeBoolean(pkt.lookAtPlayers); buf.writeFloat(pkt.lookRadius);
                buf.writeUtf(pkt.skinMode); buf.writeUtf(pkt.modelType);
                buf.writeFloat(pkt.scaleX); buf.writeFloat(pkt.scaleY); buf.writeFloat(pkt.scaleZ);
                buf.writeFloat(pkt.posX); buf.writeFloat(pkt.posY); buf.writeFloat(pkt.posZ);
                buf.writeFloat(pkt.rotX); buf.writeFloat(pkt.rotY); buf.writeFloat(pkt.rotZ);
                buf.writeBoolean(pkt.hasGravity); buf.writeBoolean(pkt.nameVisible);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record UploadNpcSkinC2S(int entityId, byte[] skinBytes, String modelType) implements CustomPacketPayload {
        public static final Type<UploadNpcSkinC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_upload_skin"));
        public static final StreamCodec<FriendlyByteBuf, UploadNpcSkinC2S> STREAM_CODEC = new StreamCodec<>() {
            @Override public UploadNpcSkinC2S decode(FriendlyByteBuf buf) {
                return new UploadNpcSkinC2S(buf.readInt(), buf.readByteArray(65536), buf.readUtf());
            }
            @Override public void encode(FriendlyByteBuf buf, UploadNpcSkinC2S pkt) {
                buf.writeInt(pkt.entityId); buf.writeByteArray(pkt.skinBytes); buf.writeUtf(pkt.modelType);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SyncNpcSkinS2C(int entityId, byte[] skinBytes, String skinMode, String modelType) implements CustomPacketPayload {
        public static final Type<SyncNpcSkinS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_sync_skin"));
        public static final StreamCodec<FriendlyByteBuf, SyncNpcSkinS2C> STREAM_CODEC = new StreamCodec<>() {
            @Override public SyncNpcSkinS2C decode(FriendlyByteBuf buf) {
                return new SyncNpcSkinS2C(buf.readInt(), buf.readByteArray(65536), buf.readUtf(), buf.readUtf());
            }
            @Override public void encode(FriendlyByteBuf buf, SyncNpcSkinS2C pkt) {
                buf.writeInt(pkt.entityId); buf.writeByteArray(pkt.skinBytes);
                buf.writeUtf(pkt.skinMode); buf.writeUtf(pkt.modelType);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SyncNpcTradesS2C(int entityId, CompoundTag tradesData) implements CustomPacketPayload {
        public static final Type<SyncNpcTradesS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_sync_trades"));
        public static final StreamCodec<FriendlyByteBuf, SyncNpcTradesS2C> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, SyncNpcTradesS2C::entityId,
                        ByteBufCodecs.COMPOUND_TAG, SyncNpcTradesS2C::tradesData,
                        SyncNpcTradesS2C::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RemoveNpcC2S(int entityId) implements CustomPacketPayload {
        public static final Type<RemoveNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_remove"));
        public static final StreamCodec<ByteBuf, RemoveNpcC2S> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, RemoveNpcC2S::entityId, RemoveNpcC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record LinkLogBookC2S(int entityId) implements CustomPacketPayload {
        public static final Type<LinkLogBookC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_link_book"));
        public static final StreamCodec<ByteBuf, LinkLogBookC2S> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, LinkLogBookC2S::entityId, LinkLogBookC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record UpdateNpcEquipmentC2S(int entityId, CompoundTag equipmentData) implements CustomPacketPayload {
        public static final Type<UpdateNpcEquipmentC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_update_equipment"));
        public static final StreamCodec<FriendlyByteBuf, UpdateNpcEquipmentC2S> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, UpdateNpcEquipmentC2S::entityId,
                        ByteBufCodecs.COMPOUND_TAG, UpdateNpcEquipmentC2S::equipmentData,
                        UpdateNpcEquipmentC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void handleUpdateTrades(UpdateNpcTradesC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof CustomNpcEntity npc)) return;
            HolderLookup.Provider provider = player.registryAccess();
            List<NpcTradeData> trades = new ArrayList<>();
            ListTag tradesTag = packet.tradesData().getList("Trades", 10);
            for (int i = 0; i < tradesTag.size(); i++)
                trades.add(NpcTradeData.load(tradesTag.getCompound(i), provider));
            npc.setTrades(trades);
            CompoundTag syncData = new CompoundTag();
            syncData.put("Trades", tradesTag);
            PacketDistributor.sendToAllPlayers(new SyncNpcTradesS2C(npc.getId(), syncData));
        });
    }

    public static void handleUpdateSettings(UpdateNpcSettingsC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof CustomNpcEntity npc)) return;
            npc.setNpcName(packet.npcName());
            npc.setNpcInvulnerable(packet.invulnerable());
            npc.setLookAtPlayers(packet.lookAtPlayers());
            npc.setLookRadius(Math.min(packet.lookRadius(), 64.0f));
            npc.setSkinMode(packet.skinMode());
            npc.setModelType(packet.modelType());
            npc.setScaleX(packet.scaleX());
            npc.setScaleY(packet.scaleY());
            npc.setScaleZ(packet.scaleZ());
            npc.setPos(packet.posX(), packet.posY(), packet.posZ());
            npc.setRotationX(packet.rotX());
            npc.setRotationY(packet.rotY());
            npc.setRotationZ(packet.rotZ());
            npc.setHasGravity(packet.hasGravity());
            npc.setNameVisible(packet.nameVisible());
        });
    }

    public static void handleUploadSkin(UploadNpcSkinC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof CustomNpcEntity npc)) return;
            if (packet.skinBytes().length > 65536) return;
            npc.setSkinData(packet.skinBytes());
            npc.setSkinMode("vanilla");
            npc.setModelType(packet.modelType());
            saveSkinFile(player.serverLevel(), npc);
            broadcastSkin(npc);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§aSkin do NPC atualizada!"), true);
        });
    }

    public static void handleUpdateEquipment(UpdateNpcEquipmentC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof CustomNpcEntity npc)) return;
            HolderLookup.Provider provider = player.registryAccess();
            if (packet.equipmentData().contains("Equipment")) {
                ListTag equipTag = packet.equipmentData().getList("Equipment", Tag.TAG_COMPOUND);
                for (int i = 0; i < equipTag.size(); i++) {
                    CompoundTag slotTag = equipTag.getCompound(i);
                    int slot = slotTag.getInt("Slot");
                    if (slot >= 0 && slot < 6) {
                        if (slotTag.contains("Item")) {
                            npc.setNpcEquipment(slot, ItemStack.parse(provider, slotTag.getCompound("Item")).orElse(ItemStack.EMPTY));
                        } else {
                            npc.setNpcEquipment(slot, ItemStack.EMPTY);
                        }
                    }
                }
            }
        });
    }

    public static void handleRemoveNpc(RemoveNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (entity instanceof CustomNpcEntity npc) {
                npc.discard();
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§c✦ NPC removido!"), true);
            }
        });
    }

    public static void handleLinkLogBook(LinkLogBookC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof CustomNpcEntity npc)) return;
            net.minecraft.world.item.ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof CriadorRegistro
                    || mainHand.getItem() == net.minecraft.world.item.Items.WRITABLE_BOOK) {
                handleLinkLogBookDirect(player, npc);
            } else {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§cSegure um Book & Quill na mao!"), true);
            }
        });
    }

    public static void handleLinkLogBookDirect(ServerPlayer player, CustomNpcEntity npc) {
        net.minecraft.world.item.ItemStack logBook =
                new net.minecraft.world.item.ItemStack(
                        ModItens.REGISTRO_NPC.get());
        CompoundTag bookTag = new CompoundTag();
        bookTag.putUUID("LinkedNpcUUID", npc.getUUID());
        bookTag.putString("NpcName", npc.getNpcName());
        logBook.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(bookTag));
        npc.setLinkedBookUUID(npc.getUUID());
        if (!player.getInventory().add(logBook)) player.drop(logBook, false);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§a\uD83D\uDCD6 Livro de registro vinculado ao NPC!"), true);
    }

    public static void sendOpenEditor(ServerPlayer player, CustomNpcEntity npc) {
        CompoundTag data = new CompoundTag();
        HolderLookup.Provider provider = player.registryAccess();
        data.putString("NpcName", npc.getNpcName());
        data.putString("SkinMode", npc.getSkinMode());
        data.putString("ModelType", npc.getModelType());
        data.putBoolean("Invulnerable", npc.isNpcInvulnerable());
        data.putBoolean("LookAtPlayers", npc.getLookAtPlayers());
        data.putFloat("LookRadius", npc.getLookRadius());
        data.putFloat("ScaleX", npc.getScaleX());
        data.putFloat("ScaleY", npc.getScaleY());
        data.putFloat("ScaleZ", npc.getScaleZ());
        data.putFloat("RotationX", npc.getRotationX());
        data.putFloat("RotationY", npc.getRotationY());
        data.putFloat("RotationZ", npc.getRotationZ());
        data.putBoolean("HasGravity", npc.getHasGravity());
        data.putBoolean("NameVisible", npc.getNameVisible());
        ListTag tradesTag = new ListTag();
        for (NpcTradeData trade : npc.getTrades()) tradesTag.add(trade.save(provider));
        data.put("Trades", tradesTag);
        data.putBoolean("HasSkin", npc.getSkinData() != null);
        ListTag equipTag = new ListTag();
        for (int i = 0; i < 6; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            ItemStack equip = npc.getNpcEquipment(i);
            if (!equip.isEmpty()) slotTag.put("Item", equip.save(provider));
            equipTag.add(slotTag);
        }
        data.put("Equipment", equipTag);
        PacketDistributor.sendToPlayer(player, new OpenNpcEditorS2C(npc.getId(), data));
    }

    public static void broadcastSkin(CustomNpcEntity npc) {
        byte[] skinBytes = (npc.getSkinData() != null) ? npc.getSkinData() : new byte[0];
        PacketDistributor.sendToAllPlayers(new SyncNpcSkinS2C(
                npc.getId(), skinBytes, "vanilla", npc.getModelType()));
    }

    private static void saveSkinFile(ServerLevel level, CustomNpcEntity npc) {
        try {
            java.nio.file.Path worldDir = level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT);
            java.nio.file.Path skinDir = worldDir.resolve("data/customnpc/skins");
            java.nio.file.Files.createDirectories(skinDir);
            java.nio.file.Path skinFile = skinDir.resolve(npc.getUUID().toString() + ".png");
            java.nio.file.Files.write(skinFile, npc.getSkinData());
        } catch (Exception e) { e.printStackTrace(); }
    }
}
