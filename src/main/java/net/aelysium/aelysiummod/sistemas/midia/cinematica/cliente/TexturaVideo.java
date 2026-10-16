package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;


public final class TexturaVideo implements AutoCloseable {

    private DynamicTexture  texture;
    private ResourceLocation location;
    private ByteBuffer       stagingBuf;
    private int texWidth, texHeight;

    public ResourceLocation getLocation() { return location; }


    public void update(QuadroVideo frame) {
        if (frame == null) return;
        ensureTexture(frame.width, frame.height);
        if (texture == null || stagingBuf == null) return;

        int size = frame.width * frame.height * 4;
        stagingBuf.clear();
        stagingBuf.put(frame.rgba, 0, size);
        stagingBuf.flip();


        RenderSystem.bindTexture(texture.getId());
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0,
                frame.width, frame.height,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                stagingBuf);
    }

    private void ensureTexture(int w, int h) {
        if (texture != null && texWidth == w && texHeight == h) return;
        close();
        try {

            NativeImage img = new NativeImage(NativeImage.Format.RGBA, w, h, false);
            texture  = new DynamicTexture(img);
            location = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "cutscene_frame");
            Minecraft.getInstance().getTextureManager().register(location, texture);

            texture.upload();
            stagingBuf = MemoryUtil.memAlloc(w * h * 4);
            texWidth  = w;
            texHeight = h;
        } catch (Exception e) {
            close();
        }
    }

    @Override
    public void close() {
        if (stagingBuf != null) { MemoryUtil.memFree(stagingBuf); stagingBuf = null; }
        if (location != null) {
            try { Minecraft.getInstance().getTextureManager().release(location); } catch (Exception ignored) {}
            location = null;
        }
        if (texture != null) {
            try { texture.close(); } catch (Exception ignored) {}
            texture = null;
        }
        texWidth = 0;
        texHeight = 0;
    }
}
