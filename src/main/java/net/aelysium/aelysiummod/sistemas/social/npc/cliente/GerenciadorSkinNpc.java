package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;

@OnlyIn(Dist.CLIENT)
public class GerenciadorSkinNpc {

    private static final Int2ObjectMap<ResourceLocation> skinCache      = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<String>           modelTypeCache = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<String>           skinModeCache  = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<byte[]>           rawSkinDataCache = new Int2ObjectOpenHashMap<>();

    private static Boolean cpmInstalado;

    public static void registerSkin(int entityId, byte[] data, String skinMode, String modelType) {
        if (data == null || data.length == 0) {
            clearSkin(entityId);
            return;
        }

        modelTypeCache.put(entityId, modelType);
        rawSkinDataCache.put(entityId, data);

        if ((data[0] & 0xFF) == 83) {
            ResourceLocation antiga = skinCache.remove(entityId);
            if (antiga != null) Minecraft.getInstance().getTextureManager().release(antiga);
            skinModeCache.put(entityId, "cpm");
            return;
        }

        skinModeCache.put(entityId, "vanilla");

        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    "aelysiummod", "npc_skin/" + entityId);
            ResourceLocation old = skinCache.get(entityId);
            if (old != null) Minecraft.getInstance().getTextureManager().release(old);
            Minecraft.getInstance().getTextureManager().register(loc, texture);
            skinCache.put(entityId, loc);
        } catch (Exception e) {
            System.out.println("[NPC-SkinMgr] ERROR loading vanilla skin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ResourceLocation getSkinTexture(int entityId) {
        return skinCache.get(entityId);
    }

    public static String getModelType(int entityId) {
        return modelTypeCache.getOrDefault(entityId, "wide");
    }

    public static String getSkinMode(int entityId) {
        return skinModeCache.getOrDefault(entityId, "vanilla");
    }

    public static byte[] getRawSkinData(int entityId) {
        return rawSkinDataCache.get(entityId);
    }

    public static boolean isCpm(int entityId) {
        return "cpm".equals(skinModeCache.get(entityId));
    }

    public static boolean cpmInstalado() {
        Boolean instalado = cpmInstalado;
        if (instalado == null) {
            instalado = net.neoforged.fml.ModList.get().isLoaded("cpm");
            cpmInstalado = instalado;
        }
        return instalado;
    }

    public static boolean usaCpm(int entityId) {
        return cpmInstalado() && isCpm(entityId);
    }

    public static void sincronizarCpm(JogadorNpcFalso falso, int entityId) {
        if (!cpmInstalado()) return;
        byte[] bruto = isCpm(entityId) ? rawSkinDataCache.get(entityId) : null;
        CompatCpmNpc.sincronizar(falso.getGameProfile(), bruto);
        falso.cpmAtivo = bruto != null;
    }

    public static boolean hasSkin(int entityId) {
        return skinCache.containsKey(entityId);
    }

    public static void clearSkin(int entityId) {
        ResourceLocation loc = skinCache.remove(entityId);
        if (loc != null) Minecraft.getInstance().getTextureManager().release(loc);
        modelTypeCache.remove(entityId);
        skinModeCache.remove(entityId);
        rawSkinDataCache.remove(entityId);
    }

    public static void clearAll() {
        for (ResourceLocation loc : skinCache.values()) {
            Minecraft.getInstance().getTextureManager().release(loc);
        }
        skinCache.clear();
        modelTypeCache.clear();
        skinModeCache.clear();
        rawSkinDataCache.clear();
    }
}
