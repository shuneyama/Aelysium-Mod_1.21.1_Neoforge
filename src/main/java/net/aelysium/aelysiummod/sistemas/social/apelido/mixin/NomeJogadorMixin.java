package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class NomeJogadorMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void aelysium$getName(CallbackInfoReturnable<Component> cir) {
        if (!AelysiumGuarda.isAutorizado()) return;
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) return;

        MinecraftServer server = self.getServer();
        if (server == null) return;

        DadosApelido data = DadosApelido.get(server);
        MutableComponent styled = data.getStyledFullName(self.getUUID());
        if (styled != null) {
            cir.setReturnValue(styled);
        }
    }
}