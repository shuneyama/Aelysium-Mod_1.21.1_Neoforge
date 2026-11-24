package net.aelysium.aelysiummod.sistemas.rp.skin.cliente;

import net.aelysium.aelysiummod.sistemas.rp.skin.GerenciadorSkins;
import net.aelysium.aelysiummod.sistemas.rp.skin.rede.PacoteEnviarSkin;
import net.aelysium.aelysiummod.sistemas.social.npc.cliente.CompatCpmNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public final class EnvioSkinCliente {
    private EnvioSkinCliente() {
    }

    public static void escolherEEnviar(String tipo, String id, String estilo) {
        new Thread(() -> {
            try {
                String caminho;
                try (org.lwjgl.system.MemoryStack pilha = org.lwjgl.system.MemoryStack.stackPush()) {
                    org.lwjgl.PointerBuffer filtros = pilha.mallocPointer(3);
                    filtros.put(pilha.UTF8("*.png")).put(pilha.UTF8("*.cpmmodel")).put(pilha.UTF8("*.cpmproject")).flip();
                    caminho = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog(
                            "Selecionar Skin", "", filtros, "PNG, CPM Model, CPM Project", false);
                }
                if (caminho == null) return;
                File arquivo = new File(caminho);
                if (!arquivo.exists()) return;
                String nome = arquivo.getName().toLowerCase(Locale.ROOT);
                if (nome.endsWith(".cpmmodel") || nome.endsWith(".cpmproject")) {
                    enviarCpm(tipo, id, arquivo, nome.endsWith(".cpmproject"), estilo);
                    return;
                }
                if (arquivo.length() > GerenciadorSkins.TAMANHO_MAXIMO_PNG) {
                    avisar(Component.translatable("gui.aelysiummod.npc.arquivo_muito_grande_maximo_64_kb"));
                    return;
                }
                byte[] dados = Files.readAllBytes(arquivo.toPath());
                String modelo = "slim".equals(estilo) ? "slim" : "wide";
                Minecraft.getInstance().execute(() -> enviar(tipo, id, modelo, dados));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "EnvioSkinRp").start();
    }

    private static void enviarCpm(String tipo, String id, File arquivo, boolean projeto, String estilo) {
        if (!net.neoforged.fml.ModList.get().isLoaded("cpm")) {
            avisar(Component.translatable("gui.aelysiummod.npc.cpm_nao_instalado"));
            return;
        }
        Minecraft.getInstance().execute(() -> {
            try {
                byte[] dados = projeto ? CompatCpmNpc.converterProjeto(arquivo) : Files.readAllBytes(arquivo.toPath());
                if (dados.length == 0 || (dados[0] & 0xFF) != 83) {
                    avisar(Component.translatable("gui.aelysiummod.npc.cpm_arquivo_invalido"));
                    return;
                }
                if (dados.length > GerenciadorSkins.TAMANHO_MAXIMO_CPM) {
                    avisar(Component.translatable("gui.aelysiummod.npc.cpm_arquivo_muito_grande"));
                    return;
                }
                enviar(tipo, id, "slim".equals(estilo) ? "slim" : "wide", dados);
            } catch (Throwable e) {
                e.printStackTrace();
                avisar(Component.translatableEscape("gui.aelysiummod.npc.cpm_erro_conversao", String.valueOf(e.getMessage())));
            }
        });
    }

    private static void enviar(String tipo, String id, String modelo, byte[] dados) {
        int total = (dados.length + GerenciadorSkins.TAMANHO_PARTE - 1) / GerenciadorSkins.TAMANHO_PARTE;
        for (int i = 0; i < total; i++) {
            int de = i * GerenciadorSkins.TAMANHO_PARTE;
            int ate = Math.min(dados.length, de + GerenciadorSkins.TAMANHO_PARTE);
            PacketDistributor.sendToServer(new PacoteEnviarSkin(tipo, id, i, total, modelo, Arrays.copyOfRange(dados, de, ate)));
        }
    }

    private static void avisar(Component mensagem) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) Minecraft.getInstance().player.displayClientMessage(mensagem, true);
        });
    }
}
