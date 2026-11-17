package net.aelysium.aelysiummod.sistemas.mundo.mochila;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;

public record DicaMochila(BundleContents conteudo, int selecionado) implements TooltipComponent {
}
