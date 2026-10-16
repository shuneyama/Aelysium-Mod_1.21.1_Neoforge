package net.aelysium.aelysiummod.sistemas.social.npc.rede;

import io.netty.buffer.ByteBuf;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModItens;
import net.aelysium.aelysiummod.sistemas.social.npc.ArquivoNpcMundo;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.DadosTrocaNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorRegistro;
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

public class PacotesNpc {

    public record AbrirEditorNpcS2C(int entityId, CompoundTag npcData) implements CustomPacketPayload {
        public static final Type<AbrirEditorNpcS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_open_editor"));
        public static final StreamCodec<FriendlyByteBuf, AbrirEditorNpcS2C> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, AbrirEditorNpcS2C::entityId,
                        ByteBufCodecs.COMPOUND_TAG, AbrirEditorNpcS2C::npcData,
                        AbrirEditorNpcS2C::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AtualizarTrocasNpcC2S(int entityId, CompoundTag tradesData) implements CustomPacketPayload {
        public static final Type<AtualizarTrocasNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_update_trades"));
        public static final StreamCodec<FriendlyByteBuf, AtualizarTrocasNpcC2S> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, AtualizarTrocasNpcC2S::entityId,
                        ByteBufCodecs.COMPOUND_TAG, AtualizarTrocasNpcC2S::tradesData,
                        AtualizarTrocasNpcC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AtualizarConfiguracoesNpcC2S(
            int entityId, String npcName, boolean invulnerable,
            boolean lookAtPlayers, float lookRadius,
            String skinMode, String modelType,
            float scaleX, float scaleY, float scaleZ,
            float posX, float posY, float posZ,
            float rotX, float rotY, float rotZ,
            boolean hasGravity, boolean nameVisible
    ) implements CustomPacketPayload {
        public static final Type<AtualizarConfiguracoesNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_update_settings"));
        public static final StreamCodec<FriendlyByteBuf, AtualizarConfiguracoesNpcC2S> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public AtualizarConfiguracoesNpcC2S decode(FriendlyByteBuf buf) {
                return new AtualizarConfiguracoesNpcC2S(
                        buf.readInt(), buf.readUtf(), buf.readBoolean(),
                        buf.readBoolean(), buf.readFloat(), buf.readUtf(), buf.readUtf(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readBoolean(), buf.readBoolean());
            }
            @Override
            public void encode(FriendlyByteBuf buf, AtualizarConfiguracoesNpcC2S pkt) {
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

    public record EnviarSkinNpcC2S(int entityId, byte[] skinBytes, String modelType) implements CustomPacketPayload {
        public static final Type<EnviarSkinNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_upload_skin"));
        public static final StreamCodec<FriendlyByteBuf, EnviarSkinNpcC2S> STREAM_CODEC = new StreamCodec<>() {
            @Override public EnviarSkinNpcC2S decode(FriendlyByteBuf buf) {
                return new EnviarSkinNpcC2S(buf.readInt(), buf.readByteArray(65536), buf.readUtf());
            }
            @Override public void encode(FriendlyByteBuf buf, EnviarSkinNpcC2S pkt) {
                buf.writeInt(pkt.entityId); buf.writeByteArray(pkt.skinBytes); buf.writeUtf(pkt.modelType);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record EnviarModeloCpmNpcC2S(int entityId, int indice, int total, byte[] parte) implements CustomPacketPayload {
        public static final Type<EnviarModeloCpmNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_upload_cpm"));
        public static final StreamCodec<FriendlyByteBuf, EnviarModeloCpmNpcC2S> STREAM_CODEC = new StreamCodec<>() {
            @Override public EnviarModeloCpmNpcC2S decode(FriendlyByteBuf buf) {
                return new EnviarModeloCpmNpcC2S(buf.readInt(), buf.readInt(), buf.readInt(), buf.readByteArray(TAMANHO_PARTE_CPM));
            }
            @Override public void encode(FriendlyByteBuf buf, EnviarModeloCpmNpcC2S pkt) {
                buf.writeInt(pkt.entityId); buf.writeInt(pkt.indice); buf.writeInt(pkt.total); buf.writeByteArray(pkt.parte);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PedirArquivoNpcC2S(int entityId) implements CustomPacketPayload {
        public static final Type<PedirArquivoNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_pedir_arquivo"));
        public static final StreamCodec<FriendlyByteBuf, PedirArquivoNpcC2S> STREAM_CODEC = new StreamCodec<>() {
            @Override public PedirArquivoNpcC2S decode(FriendlyByteBuf buf) { return new PedirArquivoNpcC2S(buf.readInt()); }
            @Override public void encode(FriendlyByteBuf buf, PedirArquivoNpcC2S pkt) { buf.writeInt(pkt.entityId); }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ArquivoNpcS2C(int entityId, String nome, byte[] dados) implements CustomPacketPayload {
        public static final Type<ArquivoNpcS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_arquivo"));
        public static final StreamCodec<FriendlyByteBuf, ArquivoNpcS2C> STREAM_CODEC = new StreamCodec<>() {
            @Override public ArquivoNpcS2C decode(FriendlyByteBuf buf) {
                return new ArquivoNpcS2C(buf.readInt(), buf.readUtf(), buf.readByteArray(1_040_000));
            }
            @Override public void encode(FriendlyByteBuf buf, ArquivoNpcS2C pkt) {
                buf.writeInt(pkt.entityId); buf.writeUtf(pkt.nome); buf.writeByteArray(pkt.dados);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SincSkinNpcS2C(int entityId, byte[] skinBytes, String skinMode, String modelType) implements CustomPacketPayload {
        public static final Type<SincSkinNpcS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_sync_skin"));
        public static final StreamCodec<FriendlyByteBuf, SincSkinNpcS2C> STREAM_CODEC = new StreamCodec<>() {
            @Override public SincSkinNpcS2C decode(FriendlyByteBuf buf) {
                return new SincSkinNpcS2C(buf.readInt(), buf.readByteArray(1_000_000), buf.readUtf(), buf.readUtf());
            }
            @Override public void encode(FriendlyByteBuf buf, SincSkinNpcS2C pkt) {
                buf.writeInt(pkt.entityId); buf.writeByteArray(pkt.skinBytes);
                buf.writeUtf(pkt.skinMode); buf.writeUtf(pkt.modelType);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record SincTrocasNpcS2C(int entityId, CompoundTag tradesData) implements CustomPacketPayload {
        public static final Type<SincTrocasNpcS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_sync_trades"));
        public static final StreamCodec<FriendlyByteBuf, SincTrocasNpcS2C> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, SincTrocasNpcS2C::entityId,
                        ByteBufCodecs.COMPOUND_TAG, SincTrocasNpcS2C::tradesData,
                        SincTrocasNpcS2C::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record RemoverNpcC2S(int entityId) implements CustomPacketPayload {
        public static final Type<RemoverNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_remove"));
        public static final StreamCodec<ByteBuf, RemoverNpcC2S> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, RemoverNpcC2S::entityId, RemoverNpcC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record VincularLivroRegistroC2S(int entityId) implements CustomPacketPayload {
        public static final Type<VincularLivroRegistroC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_link_book"));
        public static final StreamCodec<ByteBuf, VincularLivroRegistroC2S> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.INT, VincularLivroRegistroC2S::entityId, VincularLivroRegistroC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record AtualizarEquipamentoNpcC2S(int entityId, CompoundTag equipmentData) implements CustomPacketPayload {
        public static final Type<AtualizarEquipamentoNpcC2S> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "npc_update_equipment"));
        public static final StreamCodec<FriendlyByteBuf, AtualizarEquipamentoNpcC2S> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.INT, AtualizarEquipamentoNpcC2S::entityId,
                        ByteBufCodecs.COMPOUND_TAG, AtualizarEquipamentoNpcC2S::equipmentData,
                        AtualizarEquipamentoNpcC2S::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void handleUpdateTrades(AtualizarTrocasNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
            HolderLookup.Provider provider = player.registryAccess();
            List<DadosTrocaNpc> trades = new ArrayList<>();
            ListTag tradesTag = packet.tradesData().getList("Trades", 10);
            for (int i = 0; i < tradesTag.size(); i++)
                trades.add(DadosTrocaNpc.load(tradesTag.getCompound(i), provider));
            npc.setTrades(trades);
            CompoundTag syncData = new CompoundTag();
            syncData.put("Trades", tradesTag);
            PacketDistributor.sendToAllPlayers(new SincTrocasNpcS2C(npc.getId(), syncData));
            ArquivoNpcMundo.salvar(npc);
        });
    }

    public static void handleUpdateSettings(AtualizarConfiguracoesNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
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
            ArquivoNpcMundo.salvar(npc);
        });
    }

    public static void handleUploadSkin(EnviarSkinNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
            if (packet.skinBytes().length > 65536) return;
            npc.setSkinData(packet.skinBytes());
            npc.setSkinMode("vanilla");
            npc.setModelType(packet.modelType());
            saveSkinFile(player.serverLevel(), npc);
            broadcastSkin(npc);
            ArquivoNpcMundo.salvar(npc);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.npc.skin_do_npc_atualizada"), true);
        });
    }

    public static final int TAMANHO_PARTE_CPM = 30000;
    public static final int TAMANHO_MAXIMO_CPM = 700_000;
    public static final int PRIMEIRO_BYTE_CPM = 83;

    private static final Map<UUID, MontagemCpm> MONTAGENS_CPM = new HashMap<>();

    private static final class MontagemCpm {
        final int entityId;
        final int total;
        final java.io.ByteArrayOutputStream dados = new java.io.ByteArrayOutputStream();
        int proximo = 0;

        MontagemCpm(int entityId, int total) {
            this.entityId = entityId;
            this.total = total;
        }
    }

    public static void handleUploadCpm(EnviarModeloCpmNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
            int maxPartes = TAMANHO_MAXIMO_CPM / TAMANHO_PARTE_CPM + 1;
            if (packet.total() < 1 || packet.total() > maxPartes) return;
            if (packet.parte().length > TAMANHO_PARTE_CPM) return;

            UUID jogador = player.getUUID();
            MontagemCpm m = MONTAGENS_CPM.get(jogador);
            if (packet.indice() == 0) {
                m = new MontagemCpm(packet.entityId(), packet.total());
                MONTAGENS_CPM.put(jogador, m);
            }
            if (m == null || m.entityId != packet.entityId() || m.total != packet.total() || m.proximo != packet.indice()) {
                MONTAGENS_CPM.remove(jogador);
                return;
            }
            m.dados.writeBytes(packet.parte());
            m.proximo++;
            if (m.dados.size() > TAMANHO_MAXIMO_CPM) {
                MONTAGENS_CPM.remove(jogador);
                return;
            }
            if (m.proximo < m.total) return;

            MONTAGENS_CPM.remove(jogador);
            byte[] dados = m.dados.toByteArray();
            if (dados.length == 0 || (dados[0] & 0xFF) != PRIMEIRO_BYTE_CPM) return;
            npc.setSkinData(dados);
            npc.setSkinMode("vanilla");
            saveSkinFile(player.serverLevel(), npc);
            broadcastSkin(npc);
            ArquivoNpcMundo.salvar(npc);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.npc.skin_do_npc_atualizada"), true);
        });
    }

    public static void handlePedirArquivo(PedirArquivoNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
            try {
                ArquivoNpcMundo.salvar(npc);
                byte[] dados = ArquivoNpcMundo.construirBytes(npc);
                PacketDistributor.sendToPlayer(player, new ArquivoNpcS2C(npc.getId(), npc.getNpcName(), dados));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void handleUpdateEquipment(AtualizarEquipamentoNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
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
            ArquivoNpcMundo.salvar(npc);
        });
    }

    public static void handleRemoveNpc(RemoverNpcC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (entity instanceof EntidadeNpcPersonalizado npc) {
                npc.discard();
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.npc.npc_removido_2"), true);
            }
        });
    }

    public static void handleLinkLogBook(VincularLivroRegistroC2S packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!player.hasPermissions(2) || !player.isCreative()) return;
            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof EntidadeNpcPersonalizado npc)) return;
            net.minecraft.world.item.ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof CriadorRegistro
                    || mainHand.getItem() == net.minecraft.world.item.Items.WRITABLE_BOOK) {
                handleLinkLogBookDirect(player, npc);
            } else {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.npc.segure_um_book_quill_na_mao"), true);
            }
        });
    }

    public static void handleLinkLogBookDirect(ServerPlayer player, EntidadeNpcPersonalizado npc) {
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
                net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.npc.livro_de_registro_vinculado_ao_npc"), true);
    }

    public static void sendOpenEditor(ServerPlayer player, EntidadeNpcPersonalizado npc) {
        CompoundTag data = new CompoundTag();
        HolderLookup.Provider provider = player.registryAccess();
        data.putString("NpcUuid", npc.getUUID().toString());
        data.putDouble("PosX", npc.getX());
        data.putDouble("PosY", npc.getY());
        data.putDouble("PosZ", npc.getZ());
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
        for (DadosTrocaNpc trade : npc.getTrades()) tradesTag.add(trade.save(provider));
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
        byte[] skin = npc.getSkinData();
        if (skin != null && skin.length > 0) {
            PacketDistributor.sendToPlayer(player, new SincSkinNpcS2C(npc.getId(), skin, "vanilla", npc.getModelType()));
        }
        PacketDistributor.sendToPlayer(player, new AbrirEditorNpcS2C(npc.getId(), data));
    }

    public static void broadcastSkin(EntidadeNpcPersonalizado npc) {
        byte[] skinBytes = (npc.getSkinData() != null) ? npc.getSkinData() : new byte[0];
        PacketDistributor.sendToAllPlayers(new SincSkinNpcS2C(
                npc.getId(), skinBytes, "vanilla", npc.getModelType()));
    }

    private static void saveSkinFile(ServerLevel level, EntidadeNpcPersonalizado npc) {
        ArquivoNpcMundo.salvar(npc);
    }
}
