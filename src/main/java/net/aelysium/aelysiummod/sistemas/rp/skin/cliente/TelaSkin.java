package net.aelysium.aelysiummod.sistemas.rp.skin.cliente;

import net.aelysium.aelysiummod.nucleo.cliente.PainelGui;
import net.aelysium.aelysiummod.sistemas.rp.skin.rede.PacoteEstiloSkin;
import net.aelysium.aelysiummod.sistemas.rp.skin.rede.PacoteRemoverSkin;
import net.aelysium.aelysiummod.sistemas.rp.skin.rede.PacoteSolicitarInfoSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class TelaSkin extends Screen {
    private static final int LARGURA = 176;
    private static final int ALTURA = 142;
    private static final int COR_TEXTO = 0xFF404040;

    private final Screen pai;
    private final String tipo;
    private final String id;
    private boolean definida;
    private String estilo = "wide";
    private int x0;
    private int y0;
    private Button botaoEstilo;
    private Button botaoRemover;

    public TelaSkin(Screen pai, String tipo, String id) {
        super(Component.translatable("gui.aelysiummod.skin.titulo"));
        this.pai = pai;
        this.tipo = tipo;
        this.id = id;
    }

    public static void receber(String tipo, String id, boolean definida, String modelo, boolean cpm) {
        if (Minecraft.getInstance().screen instanceof TelaSkin tela && tela.tipo.equals(tipo) && tela.id.equals(id)) {
            tela.definida = definida;
            tela.estilo = "slim".equals(modelo) ? "slim" : "wide";
            tela.cpm = cpm;
            tela.atualizar();
        }
    }

    private boolean cpm;

    @Override
    protected void init() {
        x0 = (width - LARGURA) / 2;
        y0 = (height - ALTURA) / 2;
        int larguraBotao = LARGURA - 20;
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.skin.adicionar"),
                b -> EnvioSkinCliente.escolherEEnviar(tipo, id, estilo)).bounds(x0 + 10, y0 + 32, larguraBotao, 20).build());
        botaoEstilo = addRenderableWidget(Button.builder(textoEstilo(), b -> {
            estilo = "slim".equals(estilo) ? "wide" : "slim";
            PacketDistributor.sendToServer(new PacoteEstiloSkin(tipo, id, estilo));
            atualizar();
        }).bounds(x0 + 10, y0 + 58, larguraBotao, 20).build());
        botaoRemover = addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.skin.remover"),
                b -> PacketDistributor.sendToServer(new PacoteRemoverSkin(tipo, id))).bounds(x0 + 10, y0 + 84, larguraBotao, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.skin.fechar"),
                b -> onClose()).bounds(x0 + 10, y0 + ALTURA - 28, larguraBotao, 20).build());
        atualizar();
        PacketDistributor.sendToServer(new PacoteSolicitarInfoSkin(tipo, id));
    }

    private Component textoEstilo() {
        return Component.translatable("gui.aelysiummod.skin.estilo",
                Component.translatable("slim".equals(estilo) ? "gui.aelysiummod.skin.alex" : "gui.aelysiummod.skin.steve"));
    }

    private void atualizar() {
        if (botaoEstilo != null) {
            botaoEstilo.setMessage(textoEstilo());
        }
        if (botaoRemover != null) botaoRemover.active = definida;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float parcial) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float parcial) {
        pai.render(g, -1, -1, parcial);
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        g.fill(0, 0, width, height, 0x90000000);
        PainelGui.painel(g, x0, y0, LARGURA, ALTURA);
        g.drawString(font, title, x0 + (LARGURA - font.width(title)) / 2, y0 + 8, COR_TEXTO, false);
        String chave = !definida ? "gui.aelysiummod.skin.info_nenhuma" : (cpm ? "gui.aelysiummod.skin.info_cpm" : "gui.aelysiummod.skin.info_definida");
        Component info = Component.translatable(chave);
        g.drawString(font, info, x0 + (LARGURA - font.width(info)) / 2, y0 + 20, 0xFF707070, false);
        super.render(g, mouseX, mouseY, parcial);
        g.pose().popPose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(pai);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
