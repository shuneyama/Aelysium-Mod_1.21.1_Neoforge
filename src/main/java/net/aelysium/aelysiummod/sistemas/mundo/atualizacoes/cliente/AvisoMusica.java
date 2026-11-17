package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;

public final class AvisoMusica {

    private static final SystemToast.SystemToastId ID = new SystemToast.SystemToastId(5000L);

    private AvisoMusica() {
    }

    public static void mostrar(SoundInstance musica) {
        if (musica.getSound() == null) return;
        String caminho = musica.getSound().getLocation().getPath();
        String nome = caminho.substring(caminho.lastIndexOf('/') + 1).replace('_', ' ');
        if (!nome.isEmpty()) nome = Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
        SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), ID, Component.literal("♪"), Component.literal(nome));
    }
}
