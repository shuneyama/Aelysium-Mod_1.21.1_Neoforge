package net.aelysium.aelysiummod.sistemas.midia.tocar.mixin;

import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(OptionsSubScreen.class)
public interface SubTelaOpcoesAccessor {

    @Accessor("list")
    OptionsList aelysium$getList();
}
