package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacoteEstadoMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ManipuladoresMaozinhaCliente {

    public static void handleEstado(PacoteEstadoMaozinha payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            switch (payload.fase()) {
                case OFERTA -> CacheMaozinha.ofertar(payload.lider(), payload.alvo());
                case SEGURANDO -> CacheMaozinha.ligar(payload.lider(), payload.alvo());
                case DESFEITA -> {
                    soltar(payload.lider(), payload.alvo());
                    CacheMaozinha.desfazer(payload.lider(), payload.alvo());
                }
            }
        });
    }

    private static void soltar(UUID idLider, UUID idSeguidor) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) return;
        Player lider = nivel.getPlayerByUUID(idLider);
        Player seguidor = nivel.getPlayerByUUID(idSeguidor);
        if (lider != null && seguidor != null && seguidor.getVehicle() == lider) seguidor.stopRiding();
    }
}
