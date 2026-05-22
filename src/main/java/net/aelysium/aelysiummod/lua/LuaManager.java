package net.aelysium.aelysiummod.lua;

import net.aelysium.aelysiummod.network.AelysiumNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class LuaManager {

    public static TipoLua luaAtual = TipoLua.NORMAL;

    public static void toggleLuaVermelha(MinecraftServer server) {
        if (luaAtual == TipoLua.VERMELHA) {
            luaAtual = TipoLua.NORMAL;
            AelysiumNetwork.sincronizarLuaParaTodos(TipoLua.NORMAL);
        } else {
            luaAtual = TipoLua.VERMELHA;
            AelysiumNetwork.sincronizarLuaParaTodos(TipoLua.VERMELHA);
        }
    }
}
