package net.aelysium.aelysiummod.sistemas.admin.vip;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GerenciadorVips {

    public static final int TAMANHO = 27;
    private static final String EXTENSAO = ".dat";
    private static final Pattern NOME_VALIDO = Pattern.compile("[\\p{L}\\p{N}_ -]{1,32}");

    public static boolean nomeValido(String nome) {
        return nome != null && NOME_VALIDO.matcher(nome).matches();
    }

    public static Path pasta(MinecraftServer servidor) {
        Path pasta = servidor.getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("vips");
        try {
            Files.createDirectories(pasta);
        } catch (IOException ignored) {}
        return pasta;
    }

    private static Path arquivo(MinecraftServer servidor, String nome) {
        return pasta(servidor).resolve(nome.toLowerCase() + EXTENSAO);
    }

    public static boolean existe(MinecraftServer servidor, String nome) {
        return nomeValido(nome) && Files.isRegularFile(arquivo(servidor, nome));
    }

    public static List<String> listar(MinecraftServer servidor) {
        List<String> nomes = new ArrayList<>();
        try (Stream<Path> arquivos = Files.list(pasta(servidor))) {
            arquivos.forEach(caminho -> {
                String nome = caminho.getFileName().toString();
                if (nome.endsWith(EXTENSAO)) nomes.add(nome.substring(0, nome.length() - EXTENSAO.length()));
            });
        } catch (IOException ignored) {}
        nomes.sort(String::compareTo);
        return nomes;
    }

    public static NonNullList<ItemStack> carregar(MinecraftServer servidor, String nome) {
        NonNullList<ItemStack> itens = NonNullList.withSize(TAMANHO, ItemStack.EMPTY);
        Path caminho = arquivo(servidor, nome);
        if (!Files.isRegularFile(caminho)) return itens;
        try {
            CompoundTag tag = NbtIo.readCompressed(caminho, NbtAccounter.unlimitedHeap());
            ContainerHelper.loadAllItems(tag, itens, servidor.registryAccess());
        } catch (IOException ignored) {}
        return itens;
    }

    public static boolean salvar(MinecraftServer servidor, String nome, List<ItemStack> itens) {
        NonNullList<ItemStack> lista = NonNullList.withSize(TAMANHO, ItemStack.EMPTY);
        for (int i = 0; i < TAMANHO && i < itens.size(); i++) {
            lista.set(i, itens.get(i));
        }
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, lista, true, servidor.registryAccess());
        try {
            NbtIo.writeCompressed(tag, arquivo(servidor, nome));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean remover(MinecraftServer servidor, String nome) {
        try {
            return Files.deleteIfExists(arquivo(servidor, nome));
        } catch (IOException e) {
            return false;
        }
    }
}
