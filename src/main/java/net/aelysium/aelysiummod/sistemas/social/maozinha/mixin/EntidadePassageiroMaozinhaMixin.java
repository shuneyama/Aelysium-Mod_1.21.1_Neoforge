package net.aelysium.aelysiummod.sistemas.social.maozinha.mixin;

import net.aelysium.aelysiummod.sistemas.social.maozinha.FormacaoMaozinha;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntidadePassageiroMaozinhaMixin {

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At("HEAD"), cancellable = true)
    private void aelysium_posicionarSeguidor(Entity passageiro, Entity.MoveFunction mover, CallbackInfo ci) {
        if ((Object) this instanceof Player lider && passageiro instanceof Player seguidor && FormacaoMaozinha.ehPar(lider, seguidor)) {
            FormacaoMaozinha.posicionar(lider, seguidor, mover);
            ci.cancel();
        }
    }

    @Inject(method = "getDismountLocationForPassenger(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"), cancellable = true)
    private void aelysium_desmontarSeguidor(LivingEntity passageiro, CallbackInfoReturnable<Vec3> cir) {
        if (!((Object) this instanceof Player lider) || !(passageiro instanceof Player)) return;
        boolean marcado = FormacaoMaozinha.consumirSoltura(passageiro.getUUID());
        if (marcado || FormacaoMaozinha.ehPar(lider, passageiro)) cir.setReturnValue(passageiro.position());
    }
}
