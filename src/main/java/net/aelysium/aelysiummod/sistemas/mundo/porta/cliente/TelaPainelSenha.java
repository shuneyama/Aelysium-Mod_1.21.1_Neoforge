package net.aelysium.aelysiummod.sistemas.mundo.porta.cliente;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.mundo.porta.item.CartaoItem;
import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteEnviarSenha;
import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteVincularCartao;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class TelaPainelSenha extends Screen {

    private static final ResourceLocation TEXTURA_CRIAR =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/painel_criar.png");
    private static final ResourceLocation TEXTURA_ACESSAR =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "textures/gui/painel_acessar.png");

    private static final int LARGURA_PAINEL = 176;
    private static final int ALTURA_PAINEL = 116;

    private static final int DISPLAY_X = 58, DISPLAY_Y = 22, DISPLAY_W = 60, DISPLAY_H = 17;
    private static final int GRID_TAM = 16, GRID_PASSO = 19;
    private static final int[] GRID_COL_X = {61, 80, 99};
    private static final int[] GRID_ROW_Y = {47, 66, 85};
    private static final int HOVER_SLOT_U = 235, HOVER_SLOT_V = 2;
    private static final int ZERO_X = 118, ZERO_Y = 66;
    private static final int CARTAO_SLOT_X = 145, CARTAO_SLOT_Y = 22;

    private static final int MAX_DIGITOS = 6;
    private static final int COR_LEGENDA = 0xFFAAAAAA;
    private static final int COR_DISPLAY = 0xFFFFFFFF;
    private static final int COR_DIGITO = 0xFFAAAAAA;
    private static final int COR_DIGITO_HOVER = 0xFFFFDD55;

    private final BlockPos pos;
    private final boolean definindo;
    private final StringBuilder buffer = new StringBuilder();

    private int panelX, panelY;
    private int shakeTicks = 0;
    private final List<Botao> botoes = new ArrayList<>();

    private record Botao(char digito, int x, int y) {}

    public TelaPainelSenha(BlockPos pos, boolean definindo) {
        super(Component.translatable(definindo ? "gui.aelysiummod.porta.definir_senha" : "gui.aelysiummod.porta.inserir_senha"));
        this.pos = pos;
        this.definindo = definindo;
    }

    @Override
    protected void init() {
        panelX = (this.width - LARGURA_PAINEL) / 2;
        panelY = (this.height - ALTURA_PAINEL) / 2;

        botoes.clear();
        char[] digitosGrade = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
        int idx = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                botoes.add(new Botao(digitosGrade[idx++], GRID_COL_X[col], GRID_ROW_Y[row]));
            }
        }
        botoes.add(new Botao('0', ZERO_X, ZERO_Y));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            confirmar();
            return true;
        }
        if (keyCode == 259) {
            apagar();
            return true;
        }
        if (keyCode >= 48 && keyCode <= 57) {
            digitar((char) ('0' + (keyCode - 48)));
            return true;
        }
        if (keyCode >= 320 && keyCode <= 329) {
            digitar((char) ('0' + (keyCode - 320)));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Botao botao : botoes) {
            int bx = panelX + botao.x(), by = panelY + botao.y();
            if (mouseX >= bx && mouseX < bx + GRID_TAM && mouseY >= by && mouseY < by + GRID_TAM) {
                digitar(botao.digito());
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void digitar(char digito) {
        if (buffer.length() >= MAX_DIGITOS) return;
        buffer.append(digito);
    }

    private void apagar() {
        if (buffer.length() > 0) buffer.deleteCharAt(buffer.length() - 1);
    }

    private void confirmar() {
        if (buffer.length() == 0) return;

        if (definindo && this.minecraft != null && this.minecraft.player != null) {
            ItemStack maoPrincipal = this.minecraft.player.getMainHandItem();
            if (maoPrincipal.getItem() instanceof CartaoItem && !CartaoItem.estaVinculado(maoPrincipal)) {
                PacketDistributor.sendToServer(new PacoteVincularCartao(pos));
            }
        }

        PacketDistributor.sendToServer(new PacoteEnviarSenha(pos, buffer.toString()));
    }

    public void receberResultado(BlockPos posRecebida, boolean sucesso) {
        if (!posRecebida.equals(this.pos)) return;
        if (sucesso) {
            Minecraft.getInstance().setScreen(null);
        } else {
            buffer.setLength(0);
            shakeTicks = 12;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F, 4.0F));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (shakeTicks > 0) shakeTicks--;
    }

    @Override
    public void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xA0000000);

        int shakeOffset = shakeTicks > 0 ? (int) (Math.sin(shakeTicks * 2.5) * 4) : 0;
        int px = panelX + shakeOffset;
        int py = panelY;

        ResourceLocation textura = definindo ? TEXTURA_CRIAR : TEXTURA_ACESSAR;
        graphics.blit(textura, px, py, 0, 0, LARGURA_PAINEL, ALTURA_PAINEL, 256, 256);

        String textoDisplay = buffer.toString();
        graphics.drawCenteredString(this.font, textoDisplay,
                px + DISPLAY_X + DISPLAY_W / 2, py + DISPLAY_Y + (DISPLAY_H - 8) / 2, COR_DISPLAY);

        for (Botao botao : botoes) {
            int bx = px + botao.x(), by = py + botao.y();
            boolean hover = mouseX >= bx && mouseX < bx + GRID_TAM && mouseY >= by && mouseY < by + GRID_TAM;

            if (hover) {
                graphics.blit(textura, bx, by, HOVER_SLOT_U, HOVER_SLOT_V, GRID_TAM, GRID_TAM, 256, 256);
            }

            graphics.drawCenteredString(this.font, String.valueOf(botao.digito()),
                    bx + GRID_TAM / 2, by + (GRID_TAM - 8) / 2, hover ? COR_DIGITO_HOVER : COR_DIGITO);
        }

        if (definindo) {
            int cx = px + CARTAO_SLOT_X, cy = py + CARTAO_SLOT_Y;
            boolean hoverCartao = mouseX >= cx && mouseX < cx + GRID_TAM && mouseY >= cy && mouseY < cy + GRID_TAM;
            if (hoverCartao) {
                graphics.blit(textura, cx, cy, HOVER_SLOT_U, HOVER_SLOT_V, GRID_TAM, GRID_TAM, 256, 256);
            }
            if (this.minecraft != null && this.minecraft.player != null) {
                ItemStack maoPrincipal = this.minecraft.player.getMainHandItem();
                if (maoPrincipal.getItem() instanceof CartaoItem) {
                    graphics.renderItem(maoPrincipal, cx, cy);
                }
            }
        }

        int legendaX = px + LARGURA_PAINEL + 12;
        int legendaY = py + 10;
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.porta.para_confirmar_a_senha"), legendaX, legendaY, COR_LEGENDA, false);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.porta.aperte_enter"), legendaX, legendaY + 10, COR_LEGENDA, false);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.porta.para_apagar_um_digito"), legendaX, legendaY + 24, COR_LEGENDA, false);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.porta.aperte_backspace"), legendaX, legendaY + 34, COR_LEGENDA, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
