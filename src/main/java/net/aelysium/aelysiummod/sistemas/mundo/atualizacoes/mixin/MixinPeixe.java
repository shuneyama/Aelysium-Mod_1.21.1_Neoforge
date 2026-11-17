package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import net.aelysium.aelysiummod.conteudo.filhotes.aquatico.TamanhoSalmao;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFish.class)
public abstract class MixinPeixe {

    @Inject(method = "saveToBucketTag", at = @At("RETURN"))
    private void aelysium$salvarTamanho(ItemStack balde, CallbackInfo ci) {
        if ((Object) this instanceof Salmon salmao) {
            int variante = TamanhoSalmao.obter(salmao);
            CustomData.update(DataComponents.BUCKET_ENTITY_DATA, balde, tag -> tag.putInt(TamanhoSalmao.CHAVE, variante));
        }
    }

    @Inject(method = "loadFromBucketTag", at = @At("RETURN"))
    private void aelysium$lerTamanho(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof Salmon salmao && tag.contains(TamanhoSalmao.CHAVE)) {
            TamanhoSalmao.definir(salmao, tag.getInt(TamanhoSalmao.CHAVE));
        }
    }
}
