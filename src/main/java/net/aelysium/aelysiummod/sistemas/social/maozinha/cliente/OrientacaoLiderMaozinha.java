package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrientacaoLiderMaozinha {

    private record Orientacao(float anterior, float atual) {}

    private static final Map<UUID, Orientacao> orientacoes = new HashMap<>();

    public static float yaw(Player lider, float parcial) {
        Orientacao orientacao = orientacoes.get(lider.getUUID());
        if (orientacao == null) return GeometriaMaozinha.corpo(lider, parcial);
        return Mth.rotLerp(parcial, orientacao.anterior(), orientacao.atual());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void aoTick(ClientTickEvent.Pre event) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) {
            orientacoes.clear();
            return;
        }

        Map<UUID, Orientacao> novas = new HashMap<>();
        for (Player lider : nivel.players()) {
            if (!CacheMaozinha.ehLider(lider.getUUID())) continue;

            Orientacao antiga = orientacoes.get(lider.getUUID());
            float anterior = antiga == null ? lider.yBodyRot : antiga.atual();
            float diferenca = Mth.wrapDegrees(lider.yBodyRot - anterior);
            boolean parado = GeometriaMaozinha.velocidade(lider).length() < ConstantesMaozinha.VELOCIDADE_PARADO;
            if (parado) {
                float zona = ConstantesMaozinha.ZONA_MORTA_PARADO_GRAUS;
                diferenca = Math.abs(diferenca) <= zona ? 0.0F : diferenca - Math.signum(diferenca) * zona;
            }
            float giro = Mth.clamp(diferenca, -ConstantesMaozinha.GIRO_MAXIMO_POR_TICK, ConstantesMaozinha.GIRO_MAXIMO_POR_TICK);

            novas.put(lider.getUUID(), new Orientacao(anterior, anterior + giro));
        }

        orientacoes.clear();
        orientacoes.putAll(novas);
    }

    @SubscribeEvent
    public void alinharSeguidores(RenderFrameEvent.Pre event) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) return;

        for (Player seguidor : nivel.players()) {
            UUID idLider = CacheMaozinha.liderDe(seguidor.getUUID());
            if (idLider == null) continue;
            Orientacao orientacao = orientacoes.get(idLider);
            if (orientacao == null) continue;

            seguidor.yBodyRotO = orientacao.anterior();
            seguidor.yBodyRot = orientacao.atual();
        }
    }
}
