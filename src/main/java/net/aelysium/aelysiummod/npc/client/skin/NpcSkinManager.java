package net.aelysium.aelysiummod.npc.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class NpcSkinManager {

    private static final Map<Integer, ResourceLocation> skinCache      = new ConcurrentHashMap<>();
    private static final Map<Integer, String>           modelTypeCache = new ConcurrentHashMap<>();
    private static final Map<Integer, String>           skinModeCache  = new ConcurrentHashMap<>();
    private static final Map<Integer, byte[]>           rawSkinDataCache = new ConcurrentHashMap<>();

    public static void registerSkin(int entityId, byte[] data, String skinMode, String modelType) {
        System.out.println("[NPC-SkinMgr] registerSkin: entityId=" + entityId
                + " skinMode=" + skinMode + " modelType=" + modelType
                + " dataSize=" + (data != null ? data.length : "null"));

        if (data == null || data.length == 0) {
            clearSkin(entityId);
            return;
        }

        skinModeCache.put(entityId, "vanilla");
        modelTypeCache.put(entityId, modelType);
        rawSkinDataCache.put(entityId, data);

        try {
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));
            DynamicTexture texture = new DynamicTexture(image);
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    "aelysiummod", "npc_skin/" + entityId);
            ResourceLocation old = skinCache.get(entityId);
            if (old != null) Minecraft.getInstance().getTextureManager().release(old);
            Minecraft.getInstance().getTextureManager().register(loc, texture);
            skinCache.put(entityId, loc);
            System.out.println("[NPC-SkinMgr] Vanilla skin registered: " + loc);
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