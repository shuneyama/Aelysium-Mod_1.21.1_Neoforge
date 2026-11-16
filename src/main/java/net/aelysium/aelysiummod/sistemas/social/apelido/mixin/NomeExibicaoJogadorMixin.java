package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladorApelidoCliente;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class NomeExibicaoJogadorMixin {

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component aelysium$getDisplayName(Component original) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) {
            MutableComponent styled = ManipuladorApelidoCliente.getStyledFullName(self.getUUID());
            if (styled != null) {
                return styled;
            }
        }
        return original;
    }
}
