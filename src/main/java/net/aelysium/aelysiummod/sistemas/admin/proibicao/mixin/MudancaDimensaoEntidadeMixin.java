package net.aelysium.aelysiummod.sistemas.admin.proibicao.mixin;

import net.aelysium.aelysiummod.sistemas.admin.proibicao.DadosProibicao;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MudancaDimensaoEntidadeMixin {

    @Inject(
            method = "canChangeDimensions(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void aelysium$bloquearDimensaoProibida(Level oldLevel, Level newLevel, CallbackInfoReturnable<Boolean> cir) {
        if (!(newLevel instanceof ServerLevel serverLevel)) return;

        String id = serverLevel.dimension().location().toString();
        if (DadosProibicao.get(serverLevel.getServer()).dimensaoProibida(id)) {
            cir.setReturnValue(false);
        }
    }
}
