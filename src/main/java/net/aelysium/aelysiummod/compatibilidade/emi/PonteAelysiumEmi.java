package net.aelysium.aelysiummod.compatibilidade.emi;

import dev.emi.emi.runtime.EmiReloadManager;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.compatibilidade.jade.GerenciadorJogadoresOp;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class PonteAelysiumEmi {

    private static int ultimoEstado = -1;

    public static boolean guardOk() {
        return AelysiumGuarda.isAutorizadoAgora();
    }

    public static boolean isOp() {
        Player jogador = Minecraft.getInstance().player;
        return jogador != null && GerenciadorJogadoresOp.isOp(jogador.getUUID());
    }

    private static int estadoAtual() {
        if (!guardOk()) return 0;
        return isOp() ? 2 : 1;
    }

    public static void atualizarSeNecessario() {
        int estado = estadoAtual();
        if (estado != ultimoEstado) {
            ultimoEstado = estado;
            EmiReloadManager.reload();
        }
    }

    private PonteAelysiumEmi() {
    }
}
