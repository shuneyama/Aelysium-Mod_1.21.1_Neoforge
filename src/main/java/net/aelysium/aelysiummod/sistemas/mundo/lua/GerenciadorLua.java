package net.aelysium.aelysiummod.sistemas.mundo.lua;

import net.minecraft.server.MinecraftServer;
import net.aelysium.aelysiummod.sistemas.mundo.lua.rede.RedeLua;

public class GerenciadorLua {

    public static TipoLua luaAtual = TipoLua.NORMAL;

    public static void toggleLuaVermelha(MinecraftServer server) {
        if (luaAtual == TipoLua.VERMELHA) {
            luaAtual = TipoLua.NORMAL;
            RedeLua.sincronizarLuaParaTodos(TipoLua.NORMAL);
        } else {
            luaAtual = TipoLua.VERMELHA;
            RedeLua.sincronizarLuaParaTodos(TipoLua.VERMELHA);
        }
        net.aelysium.aelysiummod.conteudo.bloco.SincronizadorDamas.aoMudarLua(server);
    }
}
