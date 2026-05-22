package net.aelysium.aelysiummod.mixin.nick;

import net.aelysium.aelysiummod.client.ClientNicknameHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void aelysium$getDisplayName(CallbackInfoReturnable<Component> cir) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) {
            MutableComponent styled = ClientNicknameHandler.getStyledFullName(self.getUUID());
            if (styled != null) {
                cir.setReturnValue(styled);
            }
        }
    }
}