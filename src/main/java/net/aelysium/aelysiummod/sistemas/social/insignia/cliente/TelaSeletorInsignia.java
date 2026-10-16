package net.aelysium.aelysiummod.sistemas.social.insignia.cliente;

import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;

@OnlyIn(Dist.CLIENT)
public class TelaSeletorInsignia extends Screen {

    private static final int SCALE = 3;
    private static final int CELL_W = 130;
    private static final int CELL_H = 46;
    private static final int GAP = 10;
    private static final int PER_ROW = 3;

    private final Screen parent;
    private final java.util.List<BotaoInsignia> topo = new java.util.ArrayList<>();
    private final java.util.List<BotaoInsignia> grade = new java.util.ArrayList<>();
    private final java.util.List<Integer> baseY = new java.util.ArrayList<>();
    private int gridTop;
    private int rolagem = 0;
    private int rolagemMaxima = 0;
    private final Consumer<String> onSelect;

    public TelaSeletorInsignia(Screen parent, Consumer<String> onSelect) {
        super(Component.translatable("gui.aelysiummod.insignia.escolher_badge"));
        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        topo.clear();
        grade.clear();
        baseY.clear();
        rolagem = 0;
        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.insignia.automatico_raca"), btn -> select(ConsultaInsignia.AUTOMATICA))
                .bounds(centerX - 100, startY, 200, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.insignia.nenhum"), btn -> select(""))
                .bounds(centerX - 100, startY + 24, 200, 20).build());

        int gridY = startY + 56;
        gridTop = gridY - 4;

        TipoInsignia[] badges = TipoInsignia.values();
        java.util.List<net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente.Entrada> personalizadas =
                new java.util.ArrayList<>(net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente.todas());
        for (int i = 0; i < badges.length + personalizadas.size(); i++) {
            int col = i % PER_ROW;
            int row = i / PER_ROW;
            int bx = centerX - (PER_ROW * CELL_W + (PER_ROW - 1) * GAP) / 2 + col * (CELL_W + GAP);
            int by = gridY + row * (CELL_H + GAP);
            if (i >= badges.length) {
                var personalizada = personalizadas.get(i - badges.length);
                BotaoInsignia botao = new BotaoInsignia(personalizada.textura(), personalizada.largura(), 10, personalizada.dados().texto(),
                        bx, by, CELL_W, CELL_H, btn -> select(personalizada.dados().id()));
                grade.add(botao);
                baseY.add(by);
                continue;
            }
            TipoInsignia badge = badges[i];

            BotaoInsignia botao = new BotaoInsignia(badge.texture(), badge.textureWidth, badge.textureHeight, badge.id, bx, by, CELL_W, CELL_H, btn -> select(badge.id));
            grade.add(botao);
            baseY.add(by);
        }
        int linhas = (grade.size() + PER_ROW - 1) / PER_ROW;
        int conteudo = linhas * (CELL_H + GAP);
        rolagemMaxima = Math.max(0, conteudo - (this.height - gridY - 10));
    }

    private void select(String badgeId) {
        onSelect.accept(badgeId);
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, Component.translatable("gui.aelysiummod.insignia.escolher_badge_2"), this.width / 2, 15, 0xFFFFFF);

        graphics.enableScissor(0, gridTop, this.width, this.height);
        for (int i = 0; i < grade.size(); i++) {
            BotaoInsignia botao = grade.get(i);
            botao.setY(baseY.get(i) - rolagem);
            if (botao.getY() + botao.getHeight() < gridTop || botao.getY() > this.height) continue;
            botao.render(graphics, mouseY >= gridTop ? mouseX : -1000, mouseY >= gridTop ? mouseY : -1000, partialTick);
        }
        graphics.disableScissor();

        if (rolagemMaxima > 0) {
            int barraAltura = this.height - gridTop - 4;
            int pino = Math.max(16, barraAltura * barraAltura / (barraAltura + rolagemMaxima));
            int pinoY = gridTop + Math.round((barraAltura - pino) * (rolagem / (float) rolagemMaxima));
            graphics.fill(this.width - 6, pinoY, this.width - 3, pinoY + pino, 0xAAFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int botao) {
        if (my >= gridTop) {
            for (BotaoInsignia b : grade) {
                if (b.mouseClicked(mx, my, botao)) return true;
            }
            return false;
        }
        return super.mouseClicked(mx, my, botao);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        rolagem = Math.max(0, Math.min(rolagemMaxima, rolagem - (int) Math.signum(sy) * 24));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class BotaoInsignia extends Button {
        private final net.minecraft.resources.ResourceLocation textura;
        private final int larguraTextura;
        private final int alturaTextura;

        BotaoInsignia(net.minecraft.resources.ResourceLocation textura, int larguraTextura, int alturaTextura, String nome,
                      int x, int y, int w, int h, OnPress onPress) {
            super(x, y, w, h, Component.literal(nome), onPress, DEFAULT_NARRATION);
            this.textura = textura;
            this.larguraTextura = larguraTextura;
            this.alturaTextura = alturaTextura;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused()) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40FFFFFF);
            }

            int escala = larguraTextura * SCALE > getWidth() - 4 ? 2 : SCALE;
            int w = larguraTextura * escala;
            int h = alturaTextura * escala;
            int x = getX() + (getWidth() - w) / 2;
            int y = getY() + (getHeight() - h) / 2;

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(escala, escala, 1f);
            graphics.blit(textura, 0, 0, 0, 0, larguraTextura, alturaTextura, larguraTextura, alturaTextura);
            graphics.pose().popPose();
        }
    }
}
