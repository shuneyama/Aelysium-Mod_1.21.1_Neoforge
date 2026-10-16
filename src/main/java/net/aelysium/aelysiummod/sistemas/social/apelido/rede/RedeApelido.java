package net.aelysium.aelysiummod.sistemas.social.apelido.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.apelido.comando.ComandoApelido;
import net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;

public class RedeApelido {

    public static final ResourceLocation NICK_SYNC_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_sync");
    public static final ResourceLocation NICK_EDITOR_OPEN_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_editor_open");
    public static final ResourceLocation NICK_EDITOR_SAVE_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "nick_editor_save");
    private static boolean temEspaco(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isWhitespace(texto.charAt(i))) return true;
        }
        return false;
    }

    public static void handleNickEditorSave(PacoteSalvarEditorApelido payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer executor)) return;
            if (!executor.hasPermissions(2)) return;

            ServerPlayer target = executor.getServer().getPlayerList().getPlayer(payload.targetUUID);
            if (target == null) return;

            if (temEspaco(payload.prefix) || temEspaco(payload.nick) || temEspaco(payload.suffix)) {
                executor.sendSystemMessage(net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.apelido.sem_espacos"));
                return;
            }

            String pronomes = DadosApelido.normalizarPronomes(payload.pronomes);
            if (pronomes == null) {
                executor.sendSystemMessage(net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.apelido.pronomes_invalidos"));
                return;
            }

            DadosApelido data = DadosApelido.get(executor.getServer());
            data.setPronomes(payload.targetUUID, pronomes);

            if (payload.nick.isEmpty() && payload.prefix.isEmpty() && payload.suffix.isEmpty()) {
                data.removeNickname(payload.targetUUID);
            } else {
                String nick = payload.nick.isEmpty() ? target.getGameProfile().getName() : payload.nick;

                java.util.UUID previousOwner = data.releasePreviousOwner(nick, payload.targetUUID);
                if (previousOwner != null) {
                    data.syncToAll(executor.getServer(), previousOwner);
                    ServerPlayer previousPlayer = executor.getServer().getPlayerList().getPlayer(previousOwner);
                    if (previousPlayer != null) ComandoApelido.refreshTabList(previousPlayer);
                }

                data.setNickname(payload.targetUUID, new DadosApelido.EntradaApelido(
                        payload.prefix, payload.prefixCor1, payload.prefixCor2, payload.prefixFormat,
                        nick, payload.nickCor, payload.nickFormat,
                        payload.suffix, payload.suffixCor1, payload.suffixCor2, payload.suffixFormat
                ));
            }

            if (payload.badgeOverride.equals(ConsultaInsignia.AUTOMATICA)) {
                PerfilJogador.insignia().limparSubstituicao(executor.getServer(), payload.targetUUID);
            } else {
                PerfilJogador.insignia().definirSubstituicao(executor.getServer(), payload.targetUUID, payload.badgeOverride);
            }

            data.syncToAll(executor.getServer(), payload.targetUUID);
            ComandoApelido.refreshTabList(target);
            ComandoApelido.refreshCommandTree(executor.getServer());

            executor.sendSystemMessage(net.minecraft.network.chat.Component.translatable("mensagem.aelysiummod.apelido.nickname_atualizado_com_sucesso"));
        });
    }

    public record PacoteSincApelido(
            UUID targetUUID,
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat
    ) implements CustomPacketPayload {
        public static final Type<PacoteSincApelido> TYPE = new Type<>(NICK_SYNC_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSincApelido> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteSincApelido decode(FriendlyByteBuf buf) {
                return new PacoteSincApelido(
                        buf.readUUID(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt()
                );
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteSincApelido p) {
                buf.writeUUID(p.targetUUID);
                buf.writeUtf(p.prefix); buf.writeInt(p.prefixCor1); buf.writeInt(p.prefixCor2); buf.writeInt(p.prefixFormat);
                buf.writeUtf(p.nick); buf.writeInt(p.nickCor); buf.writeInt(p.nickFormat);
                buf.writeUtf(p.suffix); buf.writeInt(p.suffixCor1); buf.writeInt(p.suffixCor2); buf.writeInt(p.suffixFormat);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static PacoteSincApelido fromEntry(UUID uuid, DadosApelido.EntradaApelido e) {
            return new PacoteSincApelido(uuid,
                    e.prefix(), e.prefixCor1(), e.prefixCor2(), e.prefixFormat(),
                    e.nick(), e.nickCor(), e.nickFormat(),
                    e.suffix(), e.suffixCor1(), e.suffixCor2(), e.suffixFormat());
        }

        public static PacoteSincApelido removed(UUID uuid) {
            return new PacoteSincApelido(uuid, "", 0, -1, 0, "", 0, 0, "", 0, -1, 0);
        }

        public boolean isRemoval() {
            return nick.isEmpty() && prefix.isEmpty() && suffix.isEmpty();
        }
    }

    public record PacoteAbrirEditorApelido(
            UUID targetUUID, String realName,
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat,
            String badgeOverride, String pronomes
    ) implements CustomPacketPayload {
        public static final Type<PacoteAbrirEditorApelido> TYPE = new Type<>(NICK_EDITOR_OPEN_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteAbrirEditorApelido> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteAbrirEditorApelido decode(FriendlyByteBuf buf) {
                return new PacoteAbrirEditorApelido(
                        buf.readUUID(), buf.readUtf(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readUtf()
                );
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteAbrirEditorApelido p) {
                buf.writeUUID(p.targetUUID); buf.writeUtf(p.realName);
                buf.writeUtf(p.prefix); buf.writeInt(p.prefixCor1); buf.writeInt(p.prefixCor2); buf.writeInt(p.prefixFormat);
                buf.writeUtf(p.nick); buf.writeInt(p.nickCor); buf.writeInt(p.nickFormat);
                buf.writeUtf(p.suffix); buf.writeInt(p.suffixCor1); buf.writeInt(p.suffixCor2); buf.writeInt(p.suffixFormat);
                buf.writeUtf(p.badgeOverride); buf.writeUtf(p.pronomes);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record PacoteSalvarEditorApelido(
            UUID targetUUID,
            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
            String nick, int nickCor, int nickFormat,
            String suffix, int suffixCor1, int suffixCor2, int suffixFormat,
            String badgeOverride, String pronomes
    ) implements CustomPacketPayload {
        public static final Type<PacoteSalvarEditorApelido> TYPE = new Type<>(NICK_EDITOR_SAVE_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSalvarEditorApelido> STREAM_CODEC = new StreamCodec<>() {
            @Override public PacoteSalvarEditorApelido decode(FriendlyByteBuf buf) {
                return new PacoteSalvarEditorApelido(
                        buf.readUUID(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
                        buf.readUtf(), buf.readUtf()
                );
            }
            @Override public void encode(FriendlyByteBuf buf, PacoteSalvarEditorApelido p) {
                buf.writeUUID(p.targetUUID);
                buf.writeUtf(p.prefix); buf.writeInt(p.prefixCor1); buf.writeInt(p.prefixCor2); buf.writeInt(p.prefixFormat);
                buf.writeUtf(p.nick); buf.writeInt(p.nickCor); buf.writeInt(p.nickFormat);
                buf.writeUtf(p.suffix); buf.writeInt(p.suffixCor1); buf.writeInt(p.suffixCor2); buf.writeInt(p.suffixFormat);
                buf.writeUtf(p.badgeOverride); buf.writeUtf(p.pronomes);
            }
        };
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
