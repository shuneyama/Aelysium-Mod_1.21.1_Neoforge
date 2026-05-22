package net.aelysium.aelysiummod.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.chat.BalloonStyle;
import net.aelysium.aelysiummod.chat.PlayerChatData;
import net.aelysium.aelysiummod.util.ChatConfig;
import net.aelysium.aelysiummod.lua.TipoLua;
import net.aelysium.aelysiummod.comandos.NickCommand;
import net.aelysium.aelysiummod.nickname.NicknameData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class AelysiumNetwork {

    public static final ResourceLocation BALLOON_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "balloon");
    public static final ResourceLocation BALLOON_CONFIG_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "balloon_config");
    public static final ResourceLocation OPEN_BALLOON_GUI_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "open_balloon_gui");
    public static final ResourceLocation LUA_SYNC_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "lua_sync");
    public static final ResourceLocation NICK_SYNC_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_sync");
    public static final ResourceLocation NICK_EDITOR_OPEN_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_editor_open");
    public static final ResourceLocation NICK_EDITOR_SAVE_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_editor_save");

    public static void handleBalloonConfig(BalloonConfigPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerChatData data = PlayerChatData.get(player.getUUID());
                data.corTexto = payload.corTexto;
                data.corFundo = payload.corFundo;
                data.corBorda = payload.corBorda;
                data.estilo = payload.estilo;
                PlayerChatData.save(player.getUUID());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aConfigurações do balão salvas!"));
            }
        });
    }

    public static void enviarBalloonParaProximos(ServerPlayer remetente, String mensagem) {
        PlayerChatData data = PlayerChatData.get(remetente.getUUID());
        BalloonPacket packet = new BalloonPacket(remetente.getUUID(), mensagem, data.corTexto, data.corFundo, data.corBorda, data.altura, data.estilo);
        for (ServerPlayer player : remetente.serverLevel().players()) {
            if (player.distanceTo(remetente) <= ChatConfig.getLocalChatRadius()) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    public static void enviarAbrirBalloonGui(ServerPlayer player) {
        PlayerChatData data = PlayerChatData.get(player.getUUID());
        PacketDistributor.sendToPlayer(player, new OpenBalloonGuiPacket(data.corTexto, data.corFundo, data.corBorda, data.altura, data.estilo));
    }

    public static void enviarBalloonConfigParaServidor(int corTexto, int corFundo, int corBorda, BalloonStyle estilo) {
        PacketDistributor.sendToServer(new BalloonConfigPacket(corTexto, corFundo, corBorda, estilo));
    }

    public static void sincronizarLuaParaTodos(TipoLua lua) {
        PacketDistributor.sendToAllPlayers(new LuaSyncPacket(lua));
    }

    public static void sincronizarLuaParaJogador(ServerPlayer jogador, TipoLua lua) {
        PacketDistributor.sendToPlayer(jogador, new LuaSyncPacket(lua));
    }

    public static void handleNickEditorSave(NickEditorSavePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;

            ServerPlayer target = executor.getServer().getPlayerList().getPlayer(payload.targetUUID);
            if (target == null) return;

            NicknameData data = NicknameData.get(executor.getServer());

            if (payload.nick.isEmpty() && payload.prefix.isEmpty() && payload.suffix.isEmpty()) {
                data.removeNickname(payload.targetUUID);
            } else {
                String nick = payload.nick.isEmpty() ? target.getGameProfile().getName() : payload.nick;

                if (data.isNickTaken(nick, payload.targetUUID)) {
                    executor.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cO nickname §e" + nick + "§c já está em uso!"));
                    return;
                }

                data.setNickname(payload.targetUUID, new NicknameData.NicknameEntry(
                        payload.prefix, payload.prefixCor1, payload.prefixCor2, payload.prefixFormat,
                        nick, payload.nickCor, payload.nickFormat,
                        payload.suffix, payload.suffixCor1, payload.suffixCor2, payload.suffixFormat
                ));
            }

            data.syncToAll(executor.getServer(), payload.targetUUID);
            NickCommand.refreshTabList(target);
            NickCommand.refreshCommandTree(executor.getServer());

            executor.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aNickname atualizado com sucesso!"));
        });
    }

    public record BalloonPacket(UUID playerUUID, String message, int corTexto, int corFundo, int corBorda, float altura, BalloonStyle estilo) implements CustomPacketPayload {
        public static final Type<BalloonPacket> TYPE = new Type<>(BALLOON_ID);
        public static final StreamCodec<FriendlyByteBuf, BalloonPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override public BalloonPacket decode(FriendlyByteBuf buf) {
                return new BalloonPacket(UUID.fromString(buf.readUtf()), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), BalloonStyle.valueOf(buf.readUtf()));
            }
            @Override public void encode(FriendlyByteBuf buf, BalloonPacket p) {
                buf.writeUtf(p.playerUUID.toString()); buf.writeUtf(p.message); buf.writeInt(p.corTexto); buf.writeInt(p.corFundo); buf.writeInt(p.corBorda); buf.writeFloat(p.altura); buf.writeUtf(p.estilo.name());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record OpenBalloonGuiPacket(int corTexto, int corFundo, int corBorda, float altura, BalloonStyle estilo) implements CustomPacketPayload {
        public static final Type<OpenBalloonGuiPacket> TYPE = new Type<>(OPEN_BALLOON_GUI_ID);
        public static final StreamCodec<FriendlyByteBuf, OpenBalloonGuiPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override public OpenBalloonGuiPacket decode(FriendlyByteBuf buf) {
                return new OpenBalloonGuiPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat(), BalloonStyle.valueOf(buf.readUtf()));
            }
            @Override public void encode(FriendlyByteBuf buf, OpenBalloonGuiPacket p) {
                buf.writeInt(p.corTexto); buf.writeInt(p.corFundo); buf.writeInt(p.corBorda); buf.writeFloat(p.altura); buf.writeUtf(p.estilo.name());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record BalloonConfigPacket(int corTexto, int corFundo, int corBorda, BalloonStyle estilo) implements CustomPacketPayload {
        public static final Type<BalloonConfigPacket> TYPE = new Type<>(BALLOON_CONFIG_ID);
        public static final StreamCodec<FriendlyByteBuf, BalloonConfigPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override public BalloonConfigPacket decode(FriendlyByteBuf buf) {
                return new BalloonConfigPacket(buf.readInt(), buf.readInt(), buf.readInt(), BalloonStyle.valueOf(buf.readUtf()));
            }
            @Override public void encode(FriendlyByteBuf buf, BalloonConfigPacket p) {
                buf.writeInt(p.corTexto); buf.writeInt(p.corFundo); buf.writeInt(p.corBorda); buf.writeUtf(p.estilo.name());
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record LuaSyncPacket(TipoLua tipoLua) implements CustomPacketPayload {
        public static final Type<LuaSyncPacket> TYPE = new Type<>(LUA_SYNC_ID);
        public static final StreamCodec<FriendlyByteBuf, LuaSyncPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(s -> TipoLua.valueOf(s), Enum::name), LuaSyncPacket::tipoLua,
                LuaSyncPacket::new
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record NickSyncPacket(
            UUID targetUUID,
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat
    ) implements CustomPacketPayload {
        public static final Type<NickSyncPacket> TYPE = new Type<>(NICK_SYNC_ID);
        public static final StreamCodec<FriendlyByteBuf, NickSyncPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override public NickSyncPacket decode(FriendlyByteBuf buf) {
                return new NickSyncPacket(
                        buf.readUUID(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()
                );
            }
            @Override public void encode(FriendlyByteBuf buf, NickSyncPacket p) {
                buf.writeUUID(p.targetUUID);
                buf.writeUtf(p.prefix); buf.writeInt(p.prefixCor1); buf.writeInt(p.prefixCor2); buf.writeInt(p.prefixFormat);
                buf.writeUtf(p.nick); buf.writeInt(p.nickCor); buf.writeInt(p.nickFormat);
                buf.writeUtf(p.suffix); buf.writeInt(p.suffixCor1); buf.writeInt(p.suffixCor2); buf.writeInt(p.suffixFormat);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static NickSyncPacket fromEntry(UUID uuid, NicknameData.NicknameEntry e) {
            return new NickSyncPacket(uuid,
                    e.prefix(), e.prefixCor1(), e.prefixCor2(), e.prefixFormat(),
                    e.nick(), e.nickCor(), e.nickFormat(),
                    e.suffix(), e.suffixCor1(), e.suffixCor2(), e.suffixFormat());
        }

        public static NickSyncPacket removed(UUID uuid) {
            return new NickSyncPacket(uuid, "", 0, -1, 0, "", 0, 0, "", 0, -1, 0);
        }

        public boolean isRemoval() {
            return nick.isEmpty() && prefix.isEmpty() && suffix.isEmpty();
        }
    }

    public record NickEditorOpenPacket(
            UUID targetUUID, String realName,
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat
    ) implements CustomPacketPayload {
        public static final Type<NickEditorOpenPacket> TYPE = new Type<>(NICK_EDITOR_OPEN_ID);
        public static final StreamCodec<FriendlyByteBuf, NickEditorOpenPacket> STREAM_CODEC = new StreamCodec<>() {
            @Override public NickEditorOpenPacket decode(FriendlyByteBuf buf) {
                return new NickEditorOpenPacket(
                        buf.readUUID(), buf.readUtf(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()
                );
            }
            @Override public void encode(FriendlyByteBuf buf, NickEditorOpenPacket p) {
                buf.writeUUID(p.targetUUID); buf.writeUtf(p.realName);
                buf.writeUtf(p.prefix); buf.writeInt(p.prefixCor1); buf.writeInt(p.prefixCor2); buf.writeInt(p.prefixFormat);
                buf.writeUtf(p.nick); buf.writeInt(p.nickCor); buf.writeInt(p.nickFormat);
                buf.writeUtf(p.suffix); buf.writeInt(p.suffixCor1); buf.writeInt(p.suffixCor2); buf.writeInt(p.suffixFormat);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record NickEditorSavePacket(
            UUID targetUUID,
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat
    ) implements CustomPacketPayload {
        public static final Type<NickEditorSavePacket> TYPE = new Type<>(NICK_EDITOR_SAVE_ID);
        public static final StreamCodec<FriendlyByteBuf, NickEditorSavePacket> STREAM_CODEC = new StreamCodec<>() {
            @Override public NickEditorSavePacket decode(FriendlyByteBuf buf) {
                return new NickEditorSavePacket(
                        buf.readUUID(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()
                );
            }
            @Override public void encode(FriendlyByteBuf buf, NickEditorSavePacket p) {
                buf.writeUUID(p.targetUUID);
                buf.writeUtf(p.prefix); buf.writeInt(p.prefixCor1); buf.writeInt(p.prefixCor2); buf.writeInt(p.prefixFormat);
                buf.writeUtf(p.nick); buf.writeInt(p.nickCor); buf.writeInt(p.nickFormat);
                buf.writeUtf(p.suffix); buf.writeInt(p.suffixCor1); buf.writeInt(p.suffixCor2); buf.writeInt(p.suffixFormat);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}