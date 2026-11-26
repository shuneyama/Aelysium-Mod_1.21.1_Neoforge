package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Enchantment.class)
public abstract class MixinNivelEncantamento {

    @Redirect(method = "getFullname", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private static MutableComponent aelysium$nivelRomano(String chave) {
        String nivel = chave.substring(chave.lastIndexOf('.') + 1);
        try {
            return Component.translatableWithFallback(chave, aelysium$romano(Integer.parseInt(nivel)));
        } catch (NumberFormatException e) {
            return Component.translatable(chave);
        }
    }

    private static String aelysium$romano(int numero) {
        if (numero <= 0) return String.valueOf(numero);
        int[] valores = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] simbolos = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            while (numero >= valores[i]) {
                numero -= valores[i];
                resultado.append(simbolos[i]);
            }
        }
        return resultado.toString();
    }
}
