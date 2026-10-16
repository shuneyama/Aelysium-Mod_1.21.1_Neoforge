package net.aelysium.aelysiummod.sistemas.mundo.lua.cliente;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.aelysium.aelysiummod.sistemas.mundo.lua.rede.RedeLua;

public class ManipuladoresLuaCliente {

    public static void handleSincLua(RedeLua.PacoteSincLua payload, IPayloadContext context) {
        context.enqueueWork(() -> RenderizadorLuaCliente.luaCliente = payload.tipoLua());
    }
}
