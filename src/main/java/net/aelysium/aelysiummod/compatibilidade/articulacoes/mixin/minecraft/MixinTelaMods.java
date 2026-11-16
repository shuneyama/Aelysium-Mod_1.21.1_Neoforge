package net.aelysium.aelysiummod.compatibilidade.articulacoes.mixin.minecraft;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.aelysium.aelysiummod.AelysiumMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = ModListScreen.class, remap = false)
public abstract class MixinTelaMods {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/ModList;getSortedMods()Ljava/util/List;"))
    private List<ModContainer> cpmec$ocultarArticulacoes(ModList lista) {
        return lista.getSortedMods().stream()
                .filter(mod -> !cpmec$ehArticulacoesPropria(mod))
                .toList();
    }

    private static boolean cpmec$ehArticulacoesPropria(ModContainer mod) {
        return mod.getModId().equals("bendylib")
                && mod.getModInfo().getOwningFile().getMods().stream().anyMatch(info -> info.getModId().equals(AelysiumMod.MOD_ID));
    }
}
