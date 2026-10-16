package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuavizadorPosicaoMaozinha {

    private static final Map<UUID, Vec3> recebidos = new HashMap<>();
    private static final Map<UUID, Vec3> suavizados = new HashMap<>();
    private static final Map<UUID, Vec3> deslocamentos = new HashMap<>();
    private long ultimoQuadro = 0L;

    public static void receber(UUID seguidor, Vec3 relativo) {
        recebidos.put(seguidor, relativo);
    }

    public static Vec3 deslocamento(Player jogador) {
        if (jogador.isLocalPlayer()) return Vec3.ZERO;
        return deslocamentos.getOrDefault(jogador.getUUID(), Vec3.ZERO);
    }

    public static void limpar() {
        recebidos.clear();
        suavizados.clear();
        deslocamentos.clear();
    }

    @SubscribeEvent
    public void antesDoQuadro(RenderFrameEvent.Pre event) {
        long agora = System.nanoTime();
        double segundos = ultimoQuadro == 0L ? 0.0 : Math.min((agora - ultimoQuadro) / 1.0E9, 0.25);
        ultimoQuadro = agora;

        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) {
            limpar();
            return;
        }

        float parcial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double fator = 1.0 - Math.exp(-ConstantesMaozinha.SUAVIZACAO_VISUAL_POR_SEGUNDO * segundos);
        Map<UUID, Vec3> novosSuavizados = new HashMap<>();
        Map<UUID, Vec3> novosDeslocamentos = new HashMap<>();

        for (Player seguidor : nivel.players()) {
            if (seguidor.isLocalPlayer()) continue;
            UUID id = seguidor.getUUID();
            UUID idLider = CacheMaozinha.liderDe(id);
            Vec3 relativo = recebidos.get(id);
            if (idLider == null || relativo == null) continue;
            Player lider = nivel.getPlayerByUUID(idLider);
            if (lider == null) continue;

            Vec3 anterior = suavizados.get(id);
            Vec3 suavizado = anterior == null ? relativo : anterior.lerp(relativo, fator);
            novosSuavizados.put(id, suavizado);

            Vec3 real = seguidor.getPosition(parcial);
            Vec3 desejado = GeometriaMaozinha.baseVisual(lider, parcial).add(suavizado);
            if (desejado.distanceTo(real) > ConstantesMaozinha.DIVERGENCIA_MAXIMA_VISUAL) continue;

            novosDeslocamentos.put(id, semColisao(seguidor, real, desejado).subtract(real));
        }

        recebidos.keySet().removeIf(id -> CacheMaozinha.liderDe(id) == null);
        suavizados.clear();
        suavizados.putAll(novosSuavizados);
        deslocamentos.clear();
        deslocamentos.putAll(novosDeslocamentos);
    }

    private static Vec3 semColisao(Player seguidor, Vec3 real, Vec3 desejado) {
        Vec3 tentativa = desejado;
        for (int i = 0; i <= ConstantesMaozinha.TENTATIVAS_SUBIDA_VISUAL; i++) {
            Vec3 deslocamento = tentativa.subtract(seguidor.position());
            if (seguidor.level().noCollision(seguidor, seguidor.getBoundingBox().move(deslocamento))) return tentativa;
            tentativa = tentativa.add(0.0, ConstantesMaozinha.PASSO_SUBIDA_VISUAL, 0.0);
        }
        return real;
    }
}
