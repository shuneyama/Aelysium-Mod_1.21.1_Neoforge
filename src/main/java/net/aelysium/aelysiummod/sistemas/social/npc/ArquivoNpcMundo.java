package net.aelysium.aelysiummod.sistemas.social.npc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArquivoNpcMundo {

    public static final int VERSAO = 2;
    private static final String PASTA_ANTIGA = "data/aelysium/npcs";
    private static final Map<UUID, Path> ARQUIVOS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> ULTIMO_HASH = new ConcurrentHashMap<>();

    private static net.minecraft.server.MinecraftServer servidorPronto;
    private static final String MARCA_COMPLETO = "AelysiumArquivoCompleto";
    private static final int PRIMEIRO_BYTE_CPM = 83;

    private ArquivoNpcMundo() {}

    public static CompoundTag construir(EntidadeNpcPersonalizado npc) {
        HolderLookup.Provider provider = npc.registryAccess();
        CompoundTag tag = new CompoundTag();
        tag.putInt("FormatVersion", VERSAO);
        tag.putString("NpcUuid", npc.getUUID().toString());
        tag.putString("NpcName", npc.getNpcName());
        tag.putString("ModelType", npc.getModelType());
        tag.putBoolean("Invulnerable", npc.isNpcInvulnerable());
        tag.putBoolean("LookAtPlayers", npc.getLookAtPlayers());
        tag.putFloat("LookRadius", npc.getLookRadius());
        tag.putFloat("ScaleX", npc.getScaleX());
        tag.putFloat("ScaleY", npc.getScaleY());
        tag.putFloat("ScaleZ", npc.getScaleZ());
        tag.putFloat("RotationX", npc.getRotationX());
        tag.putFloat("RotationY", npc.getRotationY());
        tag.putFloat("RotationZ", npc.getRotationZ());
        tag.putBoolean("HasGravity", npc.getHasGravity());
        tag.putBoolean("NameVisible", npc.getNameVisible());

        ListTag trocas = new ListTag();
        for (DadosTrocaNpc troca : npc.getTrades()) {
            if (!troca.isValid()) continue;
            CompoundTag t = troca.save(provider);
            t.remove("PlayerStock");
            t.remove("PlayerCooldown");
            t.remove("TotalTimesSold");
            trocas.add(t);
        }
        tag.put("Trades", trocas);

        ListTag equipamento = new ListTag();
        for (int i = 0; i < 6; i++) {
            CompoundTag slot = new CompoundTag();
            slot.putInt("Slot", i);
            if (!npc.getNpcEquipment(i).isEmpty()) slot.put("Item", npc.getNpcEquipment(i).save(provider));
            equipamento.add(slot);
        }
        tag.put("Equipment", equipamento);

        byte[] skin = npc.getSkinData();
        if (skin != null && skin.length > 0) tag.putByteArray("SkinData", skin);
        return tag;
    }

    public static byte[] serializar(CompoundTag tag) throws java.io.IOException {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, saida);
        return saida.toByteArray();
    }

    public static byte[] construirBytes(EntidadeNpcPersonalizado npc) throws java.io.IOException {
        return serializar(construir(npc));
    }

    public static Path pasta(ServerLevel nivel) {
        return nivel.getServer().getWorldPath(LevelResource.ROOT).resolve("aelysium").resolve("npcs");
    }

    private static Path pastaSkins(ServerLevel nivel) {
        return pasta(nivel).resolve("skins");
    }

    private static synchronized void garantirPronto(ServerLevel nivel) {
        var servidor = nivel.getServer();
        if (servidorPronto == servidor) return;
        servidorPronto = servidor;
        ARQUIVOS.clear();
        ULTIMO_HASH.clear();

        Path raiz = servidor.getWorldPath(LevelResource.ROOT);
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverPasta(raiz.resolve(PASTA_ANTIGA), pasta(nivel));
        net.aelysium.aelysiummod.nucleo.armazenamento.MigracaoPasta.moverPasta(raiz.resolve("data/customnpc/skins"), pastaSkins(nivel));
        try {
            Files.deleteIfExists(raiz.resolve("data/customnpc"));
        } catch (Exception ignored) {}

        if (!Files.isDirectory(pasta(nivel))) return;
        try (var stream = Files.list(pasta(nivel))) {
            for (Path arquivo : stream.filter(f -> f.getFileName().toString().endsWith(".nbt")).toList()) {
                UUID dono = donoDoArquivo(arquivo);
                if (dono != null) ARQUIVOS.put(dono, arquivo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CompoundTag lerArquivoNbt(Path arquivo) {
        try {
            return NbtIo.readCompressed(arquivo, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        } catch (Exception e) {
            return null;
        }
    }

    private static UUID donoDoArquivo(Path arquivo) {
        CompoundTag tag = lerArquivoNbt(arquivo);
        if (tag == null) return null;
        try {
            return UUID.fromString(tag.getString("NpcUuid"));
        } catch (Exception e) {
            return null;
        }
    }

    public static CompoundTag lerCompleto(ServerLevel nivel, UUID npc) {
        garantirPronto(nivel);
        Path arquivo = ARQUIVOS.get(npc);
        if (arquivo == null || !Files.exists(arquivo)) return null;
        CompoundTag tag = lerArquivoNbt(arquivo);
        return tag != null && tag.getBoolean(MARCA_COMPLETO) ? tag : null;
    }

    public static CompoundTag construirCompleto(EntidadeNpcPersonalizado npc) {
        CompoundTag tag = new CompoundTag();
        PersistenciaNpc.salvar(npc, tag);
        tag.putString("NpcUuid", npc.getUUID().toString());
        tag.putBoolean(MARCA_COMPLETO, true);
        return tag;
    }

    public static Path arquivo(ServerLevel nivel, EntidadeNpcPersonalizado npc) {
        UUID id = npc.getUUID();
        String curto = id.toString().substring(0, 8);
        String nome = net.aelysium.aelysiummod.nucleo.jogador.ArquivosJogador.sanitizar(
        npc.getNpcName() == null || npc.getNpcName().isBlank() ? "npc_" + curto : npc.getNpcName());
        Path candidato = pasta(nivel).resolve(nome + ".nbt");
        boolean emUso = false;
        for (var e : ARQUIVOS.entrySet()) {
            if (!e.getKey().equals(id) && e.getValue().toString().equalsIgnoreCase(candidato.toString())) emUso = true;
        }
        return emUso ? pasta(nivel).resolve(nome + "_" + curto + ".nbt") : candidato;
    }

    public static boolean salvar(EntidadeNpcPersonalizado npc) {
        if (!(npc.level() instanceof ServerLevel nivel)) return false;
        try {
            garantirPronto(nivel);
            byte[] dados = serializar(construirCompleto(npc));
            int hash = Arrays.hashCode(dados);
            Integer anterior = ULTIMO_HASH.get(npc.getUUID());
            Path destino = arquivo(nivel, npc);
            if (anterior != null && anterior == hash && Files.exists(destino) && destino.equals(ARQUIVOS.get(npc.getUUID()))) {
                return true;
            }
            Files.createDirectories(destino.getParent());
            Path temporario = destino.resolveSibling(npc.getUUID() + ".tmp");
            Files.write(temporario, dados);
            Files.move(temporario, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            ULTIMO_HASH.put(npc.getUUID(), hash);

            Path antigo = ARQUIVOS.put(npc.getUUID(), destino);
            if (antigo != null && !antigo.equals(destino)) Files.deleteIfExists(antigo);
            Files.deleteIfExists(pasta(nivel).resolve(npc.getUUID() + ".nbt"));

            salvarSkin(nivel, npc, destino);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void salvarSkin(ServerLevel nivel, EntidadeNpcPersonalizado npc, Path arquivoNpc) throws java.io.IOException {
        String base = arquivoNpc.getFileName().toString();
        base = base.substring(0, base.length() - ".nbt".length());
        Path pasta = pastaSkins(nivel);
        for (String extensao : new String[]{".png", ".cpmmodel"}) {
            Files.deleteIfExists(pasta.resolve(npc.getUUID() + extensao));
        }
        byte[] skin = npc.getSkinData();
        if (skin == null || skin.length == 0) {
            for (String extensao : new String[]{".png", ".cpmmodel"}) Files.deleteIfExists(pasta.resolve(base + extensao));
            return;
        }
        boolean cpm = (skin[0] & 0xFF) == PRIMEIRO_BYTE_CPM;
        Files.createDirectories(pasta);
        Files.write(pasta.resolve(base + (cpm ? ".cpmmodel" : ".png")), skin);
        Files.deleteIfExists(pasta.resolve(base + (cpm ? ".png" : ".cpmmodel")));
    }
}
