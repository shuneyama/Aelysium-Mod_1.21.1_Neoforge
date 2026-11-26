package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.FormacaoMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.UUID;

public class OrientacaoLiderMaozinha {

    public static float yaw(Player lider, float parcial) {
        return FormacaoMaozinha.yaw(lider, parcial);
    }

    @SubscribeEvent
    public void alinharSeguidores(RenderFrameEvent.Pre event) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) return;

        for (Player seguidor : nivel.players()) {
            UUID idLider = CacheMaozinha.liderDe(seguidor.getUUID());
            if (idLider == null) continue;
            Player lider = nivel.getPlayerByUUID(idLider);
            if (lider == null) continue;
            float[] yaws = FormacaoMaozinha.yaws(lider);
            if (yaws == null) continue;

            seguidor.yBodyRotO = yaws[0];
            seguidor.yBodyRot = yaws[1];
        }
    }
}
