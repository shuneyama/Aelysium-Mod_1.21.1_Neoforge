package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public class EventosOpcoesCliente {

    private static final String ADICIONADO = "aelysiummod_opcoes";

    private static boolean ataqueAtivo;
    private static boolean usarAtivo;
    private static boolean ataqueAnterior;
    private static boolean usarAnterior;

    private static OptionInstance<Boolean> booleano(String chave, boolean valor, java.util.function.Consumer<Boolean> aoMudar) {
        return OptionInstance.createBoolean(chave, valor, aoMudar);
    }

    private static OptionInstance<Boolean> alternavel(String chave, boolean valor, java.util.function.Consumer<Boolean> aoMudar) {
        return OptionInstance.createBoolean(chave, OptionInstance.noTooltip(),
                (legenda, alternar) -> Component.translatable(alternar ? "options.key.toggle" : "options.key.hold").withStyle(s -> s),
                valor, aoMudar);
    }

    public static OptionInstance<Double> volumeInterface() {
        return new OptionInstance<>("soundCategory.ui", OptionInstance.noTooltip(),
                (legenda, valor) -> Component.translatable("options.percent_value", legenda, (int) (valor * 100.0D)),
                OptionInstance.UnitDouble.INSTANCE, OpcoesAtualizadas.VOLUME_UI.get(),
                valor -> salvar(() -> OpcoesAtualizadas.VOLUME_UI.set(valor)));
    }

    @SubscribeEvent
    public static void tela(ScreenEvent.Init.Post evento) {
        if (!(evento.getScreen() instanceof OptionsSubScreen tela) || tela.list == null) return;
        if (tela instanceof SoundOptionsScreen) {
            OpcoesAtualizadas.FrequenciaMusica[] valores = OpcoesAtualizadas.FrequenciaMusica.values();
            OptionInstance<OpcoesAtualizadas.FrequenciaMusica> frequencia = new OptionInstance<>("options.music_frequency",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.music_frequency.tooltip")),
                    (legenda, valor) -> Component.translatable("options.music_frequency." + (valor == OpcoesAtualizadas.FrequenciaMusica.PADRAO ? "default" : valor == OpcoesAtualizadas.FrequenciaMusica.FREQUENTE ? "frequent" : "constant")),
                    new OptionInstance.Enum<>(List.of(valores), Codec.INT.xmap(i -> valores[i], Enum::ordinal)),
                    OpcoesAtualizadas.FREQUENCIA.get(), valor -> salvar(() -> OpcoesAtualizadas.FREQUENCIA.set(valor)));
            OptionInstance<Boolean> aviso = OptionInstance.createBoolean("options.showNowPlayingToast",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.showNowPlayingToast.tooltip")),
                    OpcoesAtualizadas.AVISO_MUSICA.get(), valor -> salvar(() -> OpcoesAtualizadas.AVISO_MUSICA.set(valor)));
            tela.list.addSmall(frequencia, aviso);
        } else if (tela instanceof VideoSettingsScreen) {
            videoAtualizado(tela);
        } else if (tela instanceof MouseSettingsScreen) {
            tela.list.addSmall(booleano("options.invertMouseX", OpcoesAtualizadas.INVERTER_X.get(),
                    valor -> salvar(() -> OpcoesAtualizadas.INVERTER_X.set(valor))));
        } else if (tela instanceof ControlsScreen) {
            OptionInstance<Integer> janela = new OptionInstance<>("options.sprintWindow",
                    OptionInstance.cachedConstantTooltip(Component.translatable("options.sprintWindow.tooltip")),
                    (legenda, valor) -> Component.translatable("options.generic_value", legenda, valor),
                    new OptionInstance.IntRange(1, 20), OpcoesAtualizadas.JANELA_CORRIDA.get(),
                    valor -> salvar(() -> OpcoesAtualizadas.JANELA_CORRIDA.set(valor)));
            tela.list.addSmall(janela);
            tela.list.addSmall(alternavel("key.attack", OpcoesAtualizadas.ALTERNAR_ATAQUE.get(),
                    valor -> salvar(() -> OpcoesAtualizadas.ALTERNAR_ATAQUE.set(valor))),
                    alternavel("key.use", OpcoesAtualizadas.ALTERNAR_USAR.get(),
                            valor -> salvar(() -> OpcoesAtualizadas.ALTERNAR_USAR.set(valor))));
        }
    }

    private static void videoAtualizado(OptionsSubScreen tela) {
        Minecraft mc = Minecraft.getInstance();
        PredefinicaoGrafica[] predefinicoes = PredefinicaoGrafica.values();
        OptionInstance<PredefinicaoGrafica> predefinicao = new OptionInstance<>("options.graphics.preset",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.graphics.preset.tooltip")),
                (legenda, valor) -> Component.translatable("options.generic_value", legenda, Component.translatable(valor.chave())),
                new OptionInstance.Enum<>(List.of(predefinicoes), Codec.INT.xmap(i -> predefinicoes[i], Enum::ordinal)),
                PredefinicaoGrafica.detectar(mc.options), valor -> {
            if (valor == PredefinicaoGrafica.PERSONALIZADO) return;
            valor.aplicar(mc.options);
            mc.options.save();
            mc.execute(tela::rebuildWidgets);
        });
        OptionInstance<Boolean> folhas = OptionInstance.createBoolean("options.cutoutLeaves",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.cutoutLeaves.tooltip")),
                OpcoesAtualizadas.FOLHAS_TRANSPARENTES.get(), valor -> {
                    salvar(() -> OpcoesAtualizadas.FOLHAS_TRANSPARENTES.set(valor));
                    FiltroTexturas.recarregarChunks();
                });
        OptionInstance<Boolean> vinheta = OptionInstance.createBoolean("options.vignette",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.vignette.tooltip")),
                OpcoesAtualizadas.VINHETA.get(), valor -> salvar(() -> OpcoesAtualizadas.VINHETA.set(valor)));
        OptionInstance<Integer> raio = new OptionInstance<>("options.weatherRadius",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.weatherRadius.tooltip")),
                (legenda, valor) -> Component.translatable("options.generic_value", legenda, valor),
                new OptionInstance.IntRange(3, 10), OpcoesAtualizadas.RAIO_CLIMA.get(),
                valor -> salvar(() -> OpcoesAtualizadas.RAIO_CLIMA.set(valor)));
        OptionInstance<Boolean> mipmap = OptionInstance.createBoolean("options.mipmapAllBlocks",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.mipmapAllBlocks.tooltip")),
                OpcoesAtualizadas.MIPMAP_TODOS.get(), valor -> {
                    salvar(() -> OpcoesAtualizadas.MIPMAP_TODOS.set(valor));
                    FiltroTexturas.recarregarChunks();
                });
        OptionInstance<Boolean> telaCheia = OptionInstance.createBoolean("options.exclusiveFullscreen",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.exclusiveFullscreen.tooltip")),
                OpcoesAtualizadas.TELA_CHEIA_EXCLUSIVA.get(), valor -> {
                    salvar(() -> OpcoesAtualizadas.TELA_CHEIA_EXCLUSIVA.set(valor));
                    ((ReaplicarJanela) (Object) mc.getWindow()).aelysium$reaplicar();
                });
        tela.list.addSmall(predefinicao, folhas);
        tela.list.addSmall(vinheta, raio);
        tela.list.addSmall(telaCheia);
        tela.list.addSmall(mipmap);
    }

    private static void salvar(Runnable acao) {
        acao.run();
        OpcoesAtualizadas.SPEC.save();
    }

    private static boolean fisico(KeyMapping tecla) {
        long janela = Minecraft.getInstance().getWindow().getWindow();
        InputConstants.Key chave = tecla.getKey();
        if (chave.getType() == InputConstants.Type.MOUSE) return GLFW.glfwGetMouseButton(janela, chave.getValue()) == GLFW.GLFW_PRESS;
        if (chave.getType() == InputConstants.Type.KEYSYM) return InputConstants.isKeyDown(janela, chave.getValue());
        return false;
    }

    @SubscribeEvent
    public static void tique(ClientTickEvent.Pre evento) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) {
            ataqueAtivo = false;
            usarAtivo = false;
            return;
        }
        if (OpcoesAtualizadas.ALTERNAR_ATAQUE.get()) {
            boolean agora = fisico(minecraft.options.keyAttack);
            if (agora && !ataqueAnterior) ataqueAtivo = !ataqueAtivo;
            ataqueAnterior = agora;
            minecraft.options.keyAttack.setDown(ataqueAtivo || agora);
        } else {
            ataqueAtivo = false;
        }
        if (OpcoesAtualizadas.ALTERNAR_USAR.get()) {
            boolean agora = fisico(minecraft.options.keyUse);
            if (agora && !usarAnterior) usarAtivo = !usarAtivo;
            usarAnterior = agora;
            minecraft.options.keyUse.setDown(usarAtivo || agora);
        } else {
            usarAtivo = false;
        }
    }
}
