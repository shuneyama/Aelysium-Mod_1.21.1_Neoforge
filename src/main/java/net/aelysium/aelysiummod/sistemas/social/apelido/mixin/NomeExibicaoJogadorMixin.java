package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladorApelidoCliente;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class NomeExibicaoJogadorMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void aelysium$getDisplayName(CallbackInfoReturnable<Component> cir) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) {
            MutableComponent styled = ManipuladorApelidoCliente.getStyledFullName(self.getUUID());
            if (styled != null) {
                cir.setReturnValue(styled);
            }
        }
    }
}