package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class EventosDepuracao {

    private static final List<String> PREFIXOS = List.of("Pools:", "Transfer Queue:", "Chunk Builder:", "Tasks:", "Chunk Sorting", "Off-Heap:");

    @SubscribeEvent
    public static void texto(CustomizeGuiOverlayEvent.DebugText evento) {
        if (!OpcoesAtualizadas.ESCONDER_SODIUM_F3.get()) return;
        List<String> direita = evento.getRight();
        int cabecalho = -1;
        for (int i = 0; i < direita.size(); i++) {
            if (direita.get(i).contains("Sodium Renderer")) {
                cabecalho = i;
                break;
            }
        }
        if (cabecalho < 0) return;
        direita.removeIf(linha -> linha.contains("Sodium") || PREFIXOS.stream().anyMatch(linha::startsWith));
        if (cabecalho > 0 && cabecalho - 1 < direita.size() && direita.get(cabecalho - 1).isEmpty()) direita.remove(cabecalho - 1);
    }
}
