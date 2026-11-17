package net.aelysium.aelysiummod.sistemas.mundo.flora.cliente;

import net.minecraft.client.resources.LegacyStuffWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;

public class CoresFolhagemSeca extends SimplePreparableReloadListener<int[]> {

    public static final int PADRAO = -10732494;
    private static final ResourceLocation TEXTURA = ResourceLocation.withDefaultNamespace("textures/colormap/dry_foliage.png");
    private static int[] pixels = new int[65536];

    public static int obter(double temperatura, double chuva) {
        double temp = Mth.clamp(temperatura, 0.0D, 1.0D);
        double umidade = Mth.clamp(chuva, 0.0D, 1.0D) * temp;
        int i = (int) ((1.0D - temp) * 255.0D);
        int j = (int) ((1.0D - umidade) * 255.0D);
        int indice = j << 8 | i;
        return indice >= pixels.length ? PADRAO : pixels[indice];
    }

    @Override
    protected int[] prepare(ResourceManager gerenciador, ProfilerFiller perfil) {
        try {
            return LegacyStuffWrapper.getPixels(gerenciador, TEXTURA);
        } catch (IOException e) {
            return new int[65536];
        }
    }

    @Override
    protected void apply(int[] dados, ResourceManager gerenciador, ProfilerFiller perfil) {
        pixels = dados;
    }
}
