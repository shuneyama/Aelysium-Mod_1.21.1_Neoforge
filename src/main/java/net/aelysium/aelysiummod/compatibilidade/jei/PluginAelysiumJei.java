package net.aelysium.aelysiummod.compatibilidade.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class PluginAelysiumJei implements IModPlugin {

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        PonteAelysiumJei.setRuntime(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        PonteAelysiumJei.limparRuntime();
    }
}
