package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface AcessorGraficosGui {

    @Accessor("managed")
    boolean aelysium$gerenciado();

    @Accessor("managed")
    void aelysium$definirGerenciado(boolean gerenciado);
}
