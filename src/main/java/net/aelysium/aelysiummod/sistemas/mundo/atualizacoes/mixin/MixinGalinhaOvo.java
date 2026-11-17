package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.conteudo.filhotes.clima.ClimaAnimal;
import net.aelysium.aelysiummod.sistemas.mundo.flora.ModFlora;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Chicken.class)
public abstract class MixinGalinhaOvo {

    @ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemLike aelysium$ovo(ItemLike item) {
        if (item != Items.EGG) return item;
        byte clima = ClimaAnimal.garantir((Chicken) (Object) this);
        if (clima == ClimaAnimal.FRIO) return ModFlora.OVO_AZUL.get();
        if (clima == ClimaAnimal.QUENTE) return ModFlora.OVO_MARROM.get();
        return item;
    }
}
