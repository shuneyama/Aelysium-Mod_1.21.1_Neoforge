package net.aelysium.aelysiummod.sistemas.admin.cabeca.cliente;

import net.aelysium.aelysiummod.sistemas.admin.cabeca.rede.PacoteDefinirCabeca;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class TelaCabecaJogador extends Screen {

    private final BlockPos pos;
    private final String atual;
    private EditBox campo;

    public TelaCabecaJogador(BlockPos pos, String atual) {
        super(Component.translatable("gui.aelysiummod.cabeca.titulo"));
        this.pos = pos;
        this.atual = atual;
    }

    @Override
    protected void init() {
        campo = new EditBox(font, (width - 160) / 2, height / 2 - 20, 160, 20, Component.translatable("gui.aelysiummod.cabeca.dica"));
        campo.setMaxLength(16);
        campo.setFilter(texto -> texto.chars().allMatch(c -> c == '_' || Character.isLetterOrDigit(c) && c < 128));
        campo.setHint(Component.translatable("gui.aelysiummod.cabeca.dica"));
        campo.setValue(atual);
        addRenderableWidget(campo);
        setInitialFocus(campo);

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.cabeca.aplicar"), b -> aplicar())
                .bounds((width - 160) / 2, height / 2 + 8, 78, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(width / 2 + 2, height / 2 + 8, 78, 20).build());
    }

    private void aplicar() {
        PacketDistributor.sendToServer(new PacoteDefinirCabeca(pos, campo.getValue()));
        onClose();
    }

    @Override
    public boolean keyPressed(int tecla, int scan, int modificadores) {
        if (tecla == GLFW.GLFW_KEY_ENTER || tecla == GLFW.GLFW_KEY_KP_ENTER) {
            aplicar();
            return true;
        }
        return super.keyPressed(tecla, scan, modificadores);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        super.render(g, mouseX, mouseY, parcial);
        g.drawCenteredString(font, title, width / 2, height / 2 - 44, 0xFFFFFFFF);
    }
}
