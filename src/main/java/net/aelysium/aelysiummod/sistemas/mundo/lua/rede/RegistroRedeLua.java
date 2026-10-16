package net.aelysium.aelysiummod.sistemas.mundo.lua.rede;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.aelysium.aelysiummod.sistemas.mundo.lua.rede.RedeLua;

public class RegistroRedeLua {

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(RedeLua.PacoteSincLua.TYPE, RedeLua.PacoteSincLua.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.mundo.lua.cliente.ManipuladoresLuaCliente");
                        h.getMethod("handleSincLua", RedeLua.PacoteSincLua.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }
}
