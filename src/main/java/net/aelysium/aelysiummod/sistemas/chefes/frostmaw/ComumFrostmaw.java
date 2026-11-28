package net.aelysium.aelysiummod.sistemas.chefes.frostmaw;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;

public final class ComumFrostmaw {

    public static final String MODID = AelysiumMod.MOD_ID;
    public static ProxyFrostmaw PROXY = new ProxyFrostmaw();

    private ComumFrostmaw() {
    }

    public static ResourceLocation resource(String caminho) {
        return ResourceLocation.fromNamespaceAndPath(MODID, caminho);
    }
}
