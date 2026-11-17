package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.sodium;

import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.OpcoesAtualizadas;
import net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.PredefinicaoGrafica;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@ConfigEntryPointForge("aelysiummod")
public class ConfigSodiumAelysium implements ConfigEntryPoint {

    private static final StorageEventHandler SALVAR = OpcoesAtualizadas.SPEC::save;

    private static ResourceLocation id(String nome) {
        return ResourceLocation.fromNamespaceAndPath("aelysiummod", nome);
    }

    @Override
    public void registerConfigLate(ConfigBuilder construtor) {
        construtor.registerOwnModOptions()
                .addPage(construtor.createOptionPage()
                        .setName(Component.translatable("aelysiummod.sodium.pagina"))
                        .addOptionGroup(construtor.createOptionGroup()
                                .addOption(construtor.createEnumOption(id("preset"), PredefinicaoGrafica.class)
                                        .setName(Component.translatable("options.graphics.preset"))
                                        .setTooltip(Component.translatable("options.graphics.preset.tooltip"))
                                        .setElementNameProvider(p -> Component.translatable(p.chave()))
                                        .setStorageHandler(SALVAR)
                                        .setDefaultValue(PredefinicaoGrafica.DETALHADO)
                                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                        .setBinding(p -> p.aplicar(Minecraft.getInstance().options), () -> PredefinicaoGrafica.detectar(Minecraft.getInstance().options))))
                        .addOptionGroup(construtor.createOptionGroup()
                                .addOption(construtor.createIntegerOption(id("weather_radius"))
                                        .setName(Component.translatable("options.weatherRadius"))
                                        .setTooltip(Component.translatable("options.weatherRadius.tooltip"))
                                        .setRange(3, 10, 1)
                                        .setValueFormatter(v -> Component.literal(Integer.toString(v)))
                                        .setStorageHandler(SALVAR)
                                        .setDefaultValue(5)
                                        .setImpact(OptionImpact.LOW)
                                        .setBinding(OpcoesAtualizadas.RAIO_CLIMA::set, OpcoesAtualizadas.RAIO_CLIMA::get))
                                .addOption(construtor.createBooleanOption(id("vignette"))
                                        .setName(Component.translatable("options.vignette"))
                                        .setTooltip(Component.translatable("options.vignette.tooltip"))
                                        .setStorageHandler(SALVAR)
                                        .setDefaultValue(true)
                                        .setBinding(OpcoesAtualizadas.VINHETA::set, OpcoesAtualizadas.VINHETA::get))
                                .addOption(construtor.createBooleanOption(id("exclusive_fullscreen"))
                                        .setName(Component.translatable("options.exclusiveFullscreen"))
                                        .setTooltip(Component.translatable("options.exclusiveFullscreen.tooltip"))
                                        .setStorageHandler(() -> {
                                            OpcoesAtualizadas.SPEC.save();
                                            ((net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.cliente.ReaplicarJanela) (Object) Minecraft.getInstance().getWindow()).aelysium$reaplicar();
                                        })
                                        .setDefaultValue(false)
                                        .setBinding(OpcoesAtualizadas.TELA_CHEIA_EXCLUSIVA::set, OpcoesAtualizadas.TELA_CHEIA_EXCLUSIVA::get)))
                        .addOptionGroup(construtor.createOptionGroup()
                                .addOption(construtor.createBooleanOption(id("mipmap_all_blocks"))
                                        .setName(Component.translatable("options.mipmapAllBlocks"))
                                        .setTooltip(Component.translatable("options.mipmapAllBlocks.tooltip"))
                                        .setStorageHandler(SALVAR)
                                        .setDefaultValue(true)
                                        .setImpact(OptionImpact.LOW)
                                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                        .setBinding(OpcoesAtualizadas.MIPMAP_TODOS::set, OpcoesAtualizadas.MIPMAP_TODOS::get))));
    }
}
