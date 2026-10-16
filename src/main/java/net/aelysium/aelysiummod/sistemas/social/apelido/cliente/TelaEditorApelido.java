package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.sistemas.social.apelido.Pronome;
import net.aelysium.aelysiummod.sistemas.social.apelido.UtilitariosApelido;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.cliente.TelasCliente;
import net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedeApelido;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;

public class TelaEditorApelido extends Screen {

    private final UUID targetUUID;
    private final String realName;

    private EditBox prefixField;
    private EditBox prefixCor1Field;
    private EditBox prefixCor2Field;
    private int prefixFormat;

    private EditBox nickField;
    private EditBox nickCorField;
    private int nickFormat;

    private EditBox suffixField;
    private EditBox suffixCor1Field;
    private EditBox suffixCor2Field;
    private int suffixFormat;

    private static boolean semEspacos(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isWhitespace(texto.charAt(i))) return false;
        }
        return true;
    }

    private String pronomesEscolhido;
    private String initPronomes;
    private String initPrefix, initNick, initSuffix;
    private int initPrefixCor1, initPrefixCor2, initPrefixFormat;
    private int initNickCor, initNickFormat;
    private int initSuffixCor1, initSuffixCor2, initSuffixFormat;

    private String badgeOverride;

    public TelaEditorApelido(UUID targetUUID, String realName,
                            String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
                            String nick, int nickCor, int nickFormat,
                            String suffix, int suffixCor1, int suffixCor2, int suffixFormat,
                            String badgeOverride, String pronomes) {
        super(Component.translatable("gui.aelysiummod.apelido.editor_de_nickname"));
        this.targetUUID = targetUUID;
        this.realName = realName;
        this.initPrefix = prefix;
        this.initPrefixCor1 = prefixCor1;
        this.initPrefixCor2 = prefixCor2;
        this.initPrefixFormat = prefixFormat;
        this.initNick = nick;
        this.initNickCor = nickCor;
        this.initNickFormat = nickFormat;
        this.initSuffix = suffix;
        this.initSuffixCor1 = suffixCor1;
        this.initSuffixCor2 = suffixCor2;
        this.initSuffixFormat = suffixFormat;
        this.prefixFormat = prefixFormat;
        this.nickFormat = nickFormat;
        this.suffixFormat = suffixFormat;
        this.badgeOverride = badgeOverride;
        this.initPronomes = pronomes == null ? "" : pronomes;
        this.pronomesEscolhido = this.initPronomes;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;
        int fieldW = 100;
        int colorW = 60;

        int y = startY + 12;

        prefixField = new EditBox(this.font, centerX - 140, y, fieldW, 18, Component.translatable("gui.aelysiummod.apelido.prefixo"));
        prefixField.setMaxLength(16);
        prefixField.setFilter(TelaEditorApelido::semEspacos);
        prefixField.setValue(initPrefix.replaceAll("\\s+", ""));
        addRenderableWidget(prefixField);

        prefixCor1Field = new EditBox(this.font, centerX - 30, y, colorW, 18, Component.translatable("gui.aelysiummod.apelido.cor1"));
        prefixCor1Field.setMaxLength(7);
        prefixCor1Field.setValue(initPrefixCor1 >= 0 ? UtilitariosApelido.toHex(initPrefixCor1) : "#FFFFFF");
        addRenderableWidget(prefixCor1Field);

        prefixCor2Field = new EditBox(this.font, centerX + 40, y, colorW, 18, Component.translatable("gui.aelysiummod.apelido.cor2"));
        prefixCor2Field.setMaxLength(7);
        prefixCor2Field.setValue(initPrefixCor2 >= 0 ? UtilitariosApelido.toHex(initPrefixCor2) : "");
        addRenderableWidget(prefixCor2Field);

        addFormatButtons(centerX + 110, y, "prefix");

        y += 34;

        nickField = new EditBox(this.font, centerX - 140, y, fieldW, 18, Component.translatable("gui.aelysiummod.apelido.nick"));
        nickField.setMaxLength(24);
        nickField.setFilter(TelaEditorApelido::semEspacos);
        nickField.setValue(initNick.replaceAll("\\s+", ""));
        addRenderableWidget(nickField);

        nickCorField = new EditBox(this.font, centerX - 30, y, colorW, 18, Component.translatable("gui.aelysiummod.apelido.cor"));
        nickCorField.setMaxLength(7);
        nickCorField.setValue(UtilitariosApelido.toHex(initNickCor));
        addRenderableWidget(nickCorField);

        addFormatButtons(centerX + 110, y, "nick");

        y += 34;

        suffixField = new EditBox(this.font, centerX - 140, y, fieldW, 18, Component.translatable("gui.aelysiummod.apelido.sufixo"));
        suffixField.setMaxLength(16);
        suffixField.setFilter(TelaEditorApelido::semEspacos);
        suffixField.setValue(initSuffix.replaceAll("\\s+", ""));
        addRenderableWidget(suffixField);

        suffixCor1Field = new EditBox(this.font, centerX + -30, y, colorW, 18, Component.translatable("gui.aelysiummod.apelido.cor1"));
        suffixCor1Field.setMaxLength(7);
        suffixCor1Field.setValue(initSuffixCor1 >= 0 ? UtilitariosApelido.toHex(initSuffixCor1) : "#FFFFFF");
        addRenderableWidget(suffixCor1Field);

        suffixCor2Field = new EditBox(this.font, centerX + 40, y, colorW, 18, Component.translatable("gui.aelysiummod.apelido.cor2"));
        suffixCor2Field.setMaxLength(7);
        suffixCor2Field.setValue(initSuffixCor2 >= 0 ? UtilitariosApelido.toHex(initSuffixCor2) : "");
        addRenderableWidget(suffixCor2Field);

        addFormatButtons(centerX + 110, y, "suffix");

        y += 34;

        int yPronomes = y;

        addRenderableWidget(Button.builder(textoBotaoPronomes(), btn -> {
            pronomesEscolhido = Pronome.proximoId(pronomesEscolhido);
            btn.setMessage(textoBotaoPronomes());
        }).bounds(centerX - 140, y, 180, 18).build());

        y += 34;

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.escolher_badge"), btn -> {
                sincronizarInicial();
                this.minecraft.setScreen(TelasCliente.seletorInsignia(this, selected -> this.badgeOverride = selected));
        }).bounds(centerX - 140, y, 135, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.criar_badge"), btn -> {
                sincronizarInicial();
                this.minecraft.setScreen(TelasCliente.criadorInsignia(this));
        }).bounds(centerX + 5, y, 135, 20).build());

        Component textoSalvarPredef = Component.translatable("gui.aelysiummod.apelido.salvar_predef");
        Component textoCarregarPredef = Component.translatable("gui.aelysiummod.apelido.carregar_predef");
        int larguraPredef = Math.max(92, Math.max(this.font.width(textoSalvarPredef), this.font.width(textoCarregarPredef)) + 10);
        boolean cabeAEsquerda = centerX - 140 >= larguraPredef + 12;
        int predefX = cabeAEsquerda ? 6 : centerX + 50;
        int predefSalvarY = cabeAEsquerda ? 30 : yPronomes - 10;
        int predefCarregarY = cabeAEsquerda ? 50 : yPronomes + 8;
        if (!cabeAEsquerda) larguraPredef = Math.min(larguraPredef, 160);

        addRenderableWidget(Button.builder(textoSalvarPredef, btn -> {
                sincronizarInicial();
                this.minecraft.setScreen(new TelaNomePredefinicao(this, this::salvarPredefinicao));
        }).bounds(predefX, predefSalvarY, larguraPredef, 16).build());

        addRenderableWidget(Button.builder(textoCarregarPredef, btn -> {
                sincronizarInicial();
                PacketDistributor.sendToServer(new net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedePredefinicoesApelido.PacoteListarPredefinicoes());
        }).bounds(predefX, predefCarregarY, larguraPredef, 16).build());

        y += 34;

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.salvar"), btn -> save())
                .bounds(centerX - 105, y, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.aelysiummod.apelido.cancelar"), btn -> onClose())
                .bounds(centerX + 5, y, 100, 20).build());
    }

    private void addFormatButtons(int x, int y, String target) {
        int btnSize = 18;
        int gap = 2;

        String[] labels = {"B", "I", "U", "S", "Z"};
        int[] flags = {UtilitariosApelido.BOLD, UtilitariosApelido.ITALIC, UtilitariosApelido.UNDERLINED, UtilitariosApelido.STRIKETHROUGH, UtilitariosApelido.OBFUSCATED};

        for (int i = 0; i < labels.length; i++) {
            int flag = flags[i];
            int bx = x + (btnSize + gap) * i;
            String label = labels[i];

            addRenderableWidget(Button.builder(Component.literal(getFormatLabel(target, flag, label)), btn -> {
                toggleFormat(target, flag);
                btn.setMessage(Component.literal(getFormatLabel(target, flag, label)));
            }).bounds(bx, y, btnSize, btnSize).build());
        }
    }

    private String getFormatLabel(String target, int flag, String label) {
        int currentFormat = switch (target) {
            case "prefix" -> prefixFormat;
            case "nick" -> nickFormat;
            case "suffix" -> suffixFormat;
            default -> 0;
        };
        boolean active = (currentFormat & flag) != 0;
        return active ? "§a" + label : "§7" + label;
    }

    private void toggleFormat(String target, int flag) {
        switch (target) {
            case "prefix" -> prefixFormat ^= flag;
            case "nick" -> nickFormat ^= flag;
            case "suffix" -> suffixFormat ^= flag;
        }
    }

    private Component describeBadge() {
        if (badgeOverride == null || badgeOverride.equals(ConsultaInsignia.AUTOMATICA)) return Component.translatable("gui.aelysiummod.apelido.badge_automatico");
        if (badgeOverride.isEmpty()) return Component.translatable("gui.aelysiummod.apelido.badge_nenhum");
        TipoInsignia badge = TipoInsignia.fromId(badgeOverride);
        if (badge != null) return Component.literal(badge.id);
        var personalizada = net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente.porId(badgeOverride);
        if (personalizada != null) return Component.literal(personalizada.dados().texto());
        return Component.translatable("gui.aelysiummod.apelido.badge_automatico");
    }

    private Component textoBotaoPronomes() {
        Pronome pronome = Pronome.deId(pronomesEscolhido);
        Component valor = pronome == null
                ? Component.translatable("gui.aelysiummod.apelido.badge_nenhum")
                : Component.literal(pronome.texto).withStyle(e -> e.withColor(net.minecraft.network.chat.TextColor.fromRgb(pronome.cor)));
        return Component.translatableEscape("gui.aelysiummod.apelido.pronomes_botao", valor);
    }

    private void sincronizarInicial() {
        initPrefix = prefixField.getValue();
        initPrefixCor1 = parseColorSafe(prefixCor1Field.getValue(), 0xFFFFFF);
        initPrefixCor2 = parseColorSafe(prefixCor2Field.getValue(), -1);
        initNick = nickField.getValue();
        int corNick = parseColorSafe(nickCorField.getValue(), 0xFFFFFF);
        initNickCor = corNick == -1 ? 0xFFFFFF : corNick;
        initSuffix = suffixField.getValue();
        initSuffixCor1 = parseColorSafe(suffixCor1Field.getValue(), 0xFFFFFF);
        initSuffixCor2 = parseColorSafe(suffixCor2Field.getValue(), -1);
        initPronomes = pronomesEscolhido;
    }

    private void salvarPredefinicao(String nome) {
        sincronizarInicial();
        PacketDistributor.sendToServer(new net.aelysium.aelysiummod.sistemas.social.apelido.rede.RedePredefinicoesApelido.PacoteSalvarPredefinicao(
                new net.aelysium.aelysiummod.sistemas.social.apelido.PredefinicaoApelido(nome,
                        initPrefix, initPrefixCor1, initPrefixCor2, prefixFormat,
                        initNick, initNickCor, nickFormat,
                        initSuffix, initSuffixCor1, initSuffixCor2, suffixFormat,
                        pronomesEscolhido == null ? "" : pronomesEscolhido, badgeOverride == null ? "" : badgeOverride)));
    }

    public void aplicarPredefinicao(net.aelysium.aelysiummod.sistemas.social.apelido.PredefinicaoApelido p) {
        sincronizarInicial();
        initPrefix = p.prefixo();
        initPrefixCor1 = p.prefixoCor1();
        initPrefixCor2 = p.prefixoCor2();
        prefixFormat = p.prefixoFormato();
        if (!p.nick().isEmpty()) initNick = p.nick();
        initNickCor = p.nickCor();
        nickFormat = p.nickFormato();
        initSuffix = p.sufixo();
        initSuffixCor1 = p.sufixoCor1();
        initSuffixCor2 = p.sufixoCor2();
        suffixFormat = p.sufixoFormato();
        pronomesEscolhido = p.pronomes();
        initPronomes = p.pronomes();
        badgeOverride = p.badge();
    }

    private int parseColorSafe(String hex, int fallback) {
        if (hex == null || hex.trim().isEmpty()) return -1;
        try {
            return UtilitariosApelido.parseHex(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void save() {
        String prefix = prefixField.getValue().trim();
        int pCor1 = parseColorSafe(prefixCor1Field.getValue(), 0xFFFFFF);
        int pCor2 = parseColorSafe(prefixCor2Field.getValue(), -1);

        String nick = nickField.getValue().trim();
        int nCor = parseColorSafe(nickCorField.getValue(), 0xFFFFFF);
        if (nCor == -1) nCor = 0xFFFFFF;

        String suffix = suffixField.getValue().trim();
        int sCor1 = parseColorSafe(suffixCor1Field.getValue(), 0xFFFFFF);
        int sCor2 = parseColorSafe(suffixCor2Field.getValue(), -1);

        PacketDistributor.sendToServer(new RedeApelido.PacoteSalvarEditorApelido(
                targetUUID,
                prefix, pCor1, pCor2, prefixFormat,
                nick, nCor, nickFormat,
                suffix, sCor1, sCor2, suffixFormat,
                badgeOverride, pronomesEscolhido
        ));

        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 30;

        
        graphics.drawCenteredString(this.font, Component.translatable("gui.aelysiummod.apelido.editor_de_nickname_2"), centerX, 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatableEscape("gui.aelysiummod.apelido.jogador", realName), centerX, 20, 0xAAAAAA);

        
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.prefixo_2"), centerX - 140, startY, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.cor1_2"), centerX - 30, startY, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.cor2_2"), centerX + 40, startY, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.formato"), centerX + 110, startY, 0xFFFFFF);

        int y2 = startY + 34;
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.nick_2"), centerX - 140, y2, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.cor_2"), centerX - 30, y2, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.formato"), centerX + 110, y2, 0xFFFFFF);

        int y3 = startY + 68;
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.sufixo_2"), centerX - 140, y3, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.cor1_2"), centerX - 30, y3, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.cor2_2"), centerX + 40, y3, 0xFFFFFF);
        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.formato"), centerX + 110, y3, 0xFFFFFF);

        graphics.drawString(this.font, Component.translatable("gui.aelysiummod.apelido.pronomes_2"), centerX - 140, startY + 102, 0xFFFFFF);

        int y4 = startY + 136;
        Component rotuloBadge = Component.translatableEscape("gui.aelysiummod.apelido.badge", describeBadge());
        graphics.drawString(this.font, rotuloBadge, centerX - 140, y4, 0xFFFFFF);
        int iconeX = centerX - 140 + this.font.width(rotuloBadge) + 6;
        TipoInsignia selectedBadge = TipoInsignia.fromId(badgeOverride);
        if (selectedBadge != null) {
            graphics.blit(selectedBadge.texture(), iconeX, y4 - 1,
                    0, 0, selectedBadge.textureWidth, selectedBadge.textureHeight,
                    selectedBadge.textureWidth, selectedBadge.textureHeight);
        } else {
            var personalizada = net.aelysium.aelysiummod.nucleo.cliente.InsigniasPersonalizadasCliente.porId(badgeOverride);
            if (personalizada != null) {
                graphics.blit(personalizada.textura(), iconeX, y4 - 1,
                        0, 0, personalizada.largura(), 10, personalizada.largura(), 10);
            }
        }


        int previewY = startY + 210;
        graphics.drawCenteredString(this.font, Component.translatable("gui.aelysiummod.apelido.preview"), centerX, previewY, 0xAAAAAA);

        MutableComponent preview = buildPreview();
        int previewWidth = this.font.width(preview);
        graphics.drawString(this.font, preview, centerX - previewWidth / 2, previewY + 12, 0xFFFFFF);
    }

    private MutableComponent buildPreview() {
        MutableComponent result = Component.empty();

        String prefix = prefixField.getValue().trim();
        String nick = nickField.getValue().trim();
        String suffix = suffixField.getValue().trim();

        if (!prefix.isEmpty()) {
            int c1 = parseColorSafe(prefixCor1Field.getValue(), 0xFFFFFF);
            if (c1 == -1) c1 = 0xFFFFFF;
            int c2 = parseColorSafe(prefixCor2Field.getValue(), -1);
            result.append(UtilitariosApelido.renderDualColor(prefix, c1, c2, prefixFormat));
            result.append(Component.literal(" "));
        }

        if (!nick.isEmpty()) {
            int c = parseColorSafe(nickCorField.getValue(), 0xFFFFFF);
            if (c == -1) c = 0xFFFFFF;
            result.append(UtilitariosApelido.renderSingleColor(nick, c, nickFormat));
        } else {
            result.append(Component.literal("???"));
        }

        if (!suffix.isEmpty()) {
            result.append(Component.literal(" "));
            int c1 = parseColorSafe(suffixCor1Field.getValue(), 0xFFFFFF);
            if (c1 == -1) c1 = 0xFFFFFF;
            int c2 = parseColorSafe(suffixCor2Field.getValue(), -1);
            result.append(UtilitariosApelido.renderDualColor(suffix, c1, c2, suffixFormat));
        }

        Pronome pronome = Pronome.deId(pronomesEscolhido);
        if (pronome != null) {
            result.append(Component.literal(" "));
            result.append(Component.literal("(" + pronome.texto + ")").withStyle(e -> e.withColor(net.minecraft.network.chat.TextColor.fromRgb(pronome.cor))));
        }

        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}