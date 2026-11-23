package net.aelysium.aelysiummod.nucleo.comando;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TextoComando {
    private TextoComando() {
    }

    public static MutableComponent traduzir(String chave, ChatFormatting base, Object... argumentos) {
        Object[] convertidos = new Object[argumentos.length];
        for (int i = 0; i < argumentos.length; i++) {
            Object argumento = argumentos[i];
            convertidos[i] = argumento instanceof Component
                    ? argumento
                    : Component.literal(String.valueOf(argumento)).withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable(chave, convertidos).withStyle(base);
    }
}
