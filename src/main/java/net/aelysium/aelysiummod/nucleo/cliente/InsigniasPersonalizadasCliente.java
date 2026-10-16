package net.aelysium.aelysiummod.nucleo.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import net.aelysium.aelysiummod.nucleo.jogador.GeradorInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.InsigniaPersonalizada;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@OnlyIn(Dist.CLIENT)
public final class InsigniasPersonalizadasCliente {

    public record Entrada(InsigniaPersonalizada dados, ResourceLocation textura) {
        public int largura() {
            return dados.largura();
        }
    }

    private static final Map<Integer, Entrada> POR_INDICE = new TreeMap<>();

    private InsigniasPersonalizadasCliente() {}

    public static void substituir(List<InsigniaPersonalizada> lista) {
        for (Entrada e : POR_INDICE.values()) liberar(e.textura());
        POR_INDICE.clear();
        for (InsigniaPersonalizada i : lista) {
            ResourceLocation textura = registrar("insignia_custom/" + i.indice(), GeradorInsignia.gerar(i.texto(), i.cor()));
            POR_INDICE.put(i.indice(), new Entrada(i, textura));
        }
    }

    public static ResourceLocation registrar(String caminho, GeradorInsignia.Imagem imagem) {
        NativeImage nativa = new NativeImage(imagem.largura(), imagem.altura(), false);
        for (int y = 0; y < imagem.altura(); y++) {
            for (int x = 0; x < imagem.largura(); x++) {
                int argb = imagem.pixels()[y * imagem.largura() + x];
                int a = (argb >>> 24) & 255;
                int r = (argb >> 16) & 255;
                int g = (argb >> 8) & 255;
                int b = argb & 255;
                nativa.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        ResourceLocation local = ResourceLocation.fromNamespaceAndPath("aelysiummod", caminho);
        Minecraft.getInstance().getTextureManager().register(local, new DynamicTexture(nativa));
        return local;
    }

    public static void liberar(ResourceLocation textura) {
        if (textura != null) Minecraft.getInstance().getTextureManager().release(textura);
    }

    public static Entrada porIndice(int indice) {
        return POR_INDICE.get(indice);
    }

    public static Entrada porId(String id) {
        if (id == null) return null;
        for (Entrada e : POR_INDICE.values()) {
            if (e.dados().id().equals(id)) return e;
        }
        return null;
    }

    public static Collection<Entrada> todas() {
        return new ArrayList<>(POR_INDICE.values());
    }
}
