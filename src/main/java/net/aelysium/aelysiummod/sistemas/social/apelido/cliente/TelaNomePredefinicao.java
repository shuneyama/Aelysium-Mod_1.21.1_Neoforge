package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class TelaNomePredefinicao extends Screen {

    private final Screen pai;
    private final Consumer<String> aoConfirmar;
    private EditBox campoNome;

    public TelaNomePredefinicao(Screen pai, Consumer<String> aoConfirmar) {
        super(Component.translatable("gui.aelysiummod.apelido.predef_nome_titulo"));
        this.pai = pai;
        this.aoConfirmar = aoConfirmar;
    }

    @Override
    protected void init() {
        int centroX = this.width / 2;
        campoNome = new EditBox(this.font, centroX - 100, this.height / 2 - 20, 200, 18, Component.translatable("gui.aelysiummod.apelido.predef_nome_titulo"));
        campoNome.setMaxLength(24);
        campoNome.setFilter(t -> t.matches("[\\p{L}0-9 _-]*"));
        addRenderableWidget(campoNome);
        setInitialFocus(campoNome);

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.insignia.salvar"), b -> confirmar())
                .bounds(centroX - 100, this.height / 2 + 8, 98, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.cancelar"), b -> onClose())
                .bounds(centroX + 2, this.height / 2 + 8, 98, 20).build());
    }

    private void confirmar() {
        String nome = campoNome.getValue().trim();
        if (nome.isEmpty()) return;
        aoConfirmar.accept(nome);
        this.minecraft.setScreen(pai);
    }

    @Override
    public boolean keyPressed(int tecla, int codigo, int modificadores) {
        if (tecla == 257 || tecla == 335) {
            confirmar();
            return true;
        }
        return super.keyPressed(tecla, codigo, modificadores);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(pai);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
