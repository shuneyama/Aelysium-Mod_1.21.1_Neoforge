package net.aelysium.aelysiummod.sistemas.rp.skin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.aelysium.aelysiummod.sistemas.rp.skin.cliente.SkinsJogadoresCliente;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinSkinJogador {

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin aelysium$skinRp(PlayerSkin original) {
        return SkinsJogadoresCliente.sobrescrever(((AbstractClientPlayer) (Object) this).getUUID(), original);
    }
}
