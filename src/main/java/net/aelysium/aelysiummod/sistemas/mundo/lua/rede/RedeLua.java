package net.aelysium.aelysiummod.sistemas.mundo.lua.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.lua.TipoLua;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class RedeLua {

    public static final ResourceLocation LUA_SYNC_ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "lua_sync");

    public static void sincronizarLuaParaTodos(TipoLua lua) {
        PacketDistributor.sendToAllPlayers(new PacoteSincLua(lua));
    }

    public static void sincronizarLuaParaJogador(ServerPlayer jogador, TipoLua lua) {
        PacketDistributor.sendToPlayer(jogador, new PacoteSincLua(lua));
    }

    public record PacoteSincLua(TipoLua tipoLua) implements CustomPacketPayload {
        public static final Type<PacoteSincLua> TYPE = new Type<>(LUA_SYNC_ID);
        public static final StreamCodec<FriendlyByteBuf, PacoteSincLua> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.map(s -> TipoLua.valueOf(s), Enum::name), PacoteSincLua::tipoLua,
                PacoteSincLua::new
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
