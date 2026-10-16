package net.aelysium.aelysiummod.conteudo.item.escudo.cliente;

import net.aelysium.aelysiummod.registro.ModComponentesDados;
import net.aelysium.aelysiummod.conteudo.item.escudo.EscudoAbismoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public class DecoradorRecargaEscudo implements IItemDecorator {

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }

        long restante = stack.getOrDefault(ModComponentesDados.ESCUDO_COOLDOWN_FIM.get(), 0L) - minecraft.level.getGameTime();
        if (restante <= 0) {
            return false;
        }

        float fracao = Mth.clamp((float) restante / EscudoAbismoItem.TICKS_COOLDOWN, 0.0F, 1.0F);
        int topo = y + Mth.floor(16.0F * (1.0F - fracao));
        int base = topo + Mth.ceil(16.0F * fracao);
        graphics.fill(RenderType.guiOverlay(), x, topo, x + 16, base, Integer.MAX_VALUE);
        return true;
    }
}
