package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
public abstract class MixinTexturaFolhas {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void aelysium$folhasEscuras(ResourceLocation nome, FrameSize tamanho, NativeImage imagem, ResourceMetadata metadados, CallbackInfo ci) {
        String caminho = nome.getPath();
        if (!caminho.startsWith("block/") || !caminho.endsWith("_leaves")) return;
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        long vermelho = 0;
        long verde = 0;
        long azul = 0;
        long opacos = 0;
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int pixel = imagem.getPixelRGBA(x, y);
                if ((pixel >>> 24) == 0) continue;
                vermelho += pixel & 0xFF;
                verde += (pixel >> 8) & 0xFF;
                azul += (pixel >> 16) & 0xFF;
                opacos++;
            }
        }
        if (opacos == 0) return;
        int r = (int) (vermelho / opacos * 0.4);
        int g = (int) (verde / opacos * 0.4);
        int b = (int) (azul / opacos * 0.4);
        int escuro = (b << 16) | (g << 8) | r;
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if ((imagem.getPixelRGBA(x, y) >>> 24) == 0) imagem.setPixelRGBA(x, y, escuro);
            }
        }
    }
}
