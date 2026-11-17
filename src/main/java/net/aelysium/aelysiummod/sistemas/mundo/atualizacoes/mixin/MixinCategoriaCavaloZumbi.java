package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class MixinCategoriaCavaloZumbi {

    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private void aelysium$categoria(CallbackInfoReturnable<MobCategory> cir) {
        if ((Object) this == EntityType.ZOMBIE_HORSE) cir.setReturnValue(MobCategory.MONSTER);
    }
}
