package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.InstrumentosCobre;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoteBlock.class)
public abstract class MixinNotaCobre {

    @WrapOperation(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/properties/NoteBlockInstrument;getSoundEvent()Lnet/minecraft/core/Holder;"))
    private Holder<SoundEvent> aelysium$trompete(NoteBlockInstrument instrumento, Operation<Holder<SoundEvent>> original, @Local(argsOnly = true) Level nivel, @Local(argsOnly = true) BlockPos pos) {
        if (instrumento == NoteBlockInstrument.HARP) {
            Holder<SoundEvent> trompete = InstrumentosCobre.para(nivel.getBlockState(pos.below()).getBlock());
            if (trompete != null) return trompete;
        }
        return original.call(instrumento);
    }
}
