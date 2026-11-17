package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import net.minecraft.client.Minecraft;

public final class FiltroTexturas {

    private FiltroTexturas() {
    }

    public static void recarregarChunks() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.execute(() -> {
            if (cliente.levelRenderer != null) cliente.levelRenderer.allChanged();
        });
    }
}
