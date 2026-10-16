package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import com.mojang.authlib.GameProfile;
import com.tom.cpl.gui.UI;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinitionLoader;
import com.tom.cpm.shared.editor.Editor;
import com.tom.cpm.shared.editor.Exporter;
import com.tom.cpm.shared.editor.util.GetFreeSkinSlots;
import com.tom.cpm.shared.io.ModelFile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@OnlyIn(Dist.CLIENT)
public final class CompatCpmNpc {

    private static final long INTERVALO_REGISTRO_MS = 60_000L;

    private record Aplicado(byte[] bruto, ModelFile arquivo, long ultimoRegistro) {}

    private static final Map<UUID, Aplicado> APLICADOS = new ConcurrentHashMap<>();

    private CompatCpmNpc() {}

    public static boolean instalado() {
        return ModList.get().isLoaded("cpm");
    }

    @SuppressWarnings("unchecked")
    private static ModelDefinitionLoader<GameProfile> carregador() {
        return (ModelDefinitionLoader<GameProfile>) MinecraftClientAccess.get().getDefinitionLoader();
    }

    public static void sincronizar(GameProfile perfil, byte[] bruto) {
        UUID id = perfil.getId();
        Aplicado atual = APLICADOS.get(id);
        if (bruto == null) {
            if (atual != null) {
                APLICADOS.remove(id);
                carregador().setModel(perfil, null, false);
            }
            return;
        }
        long agora = System.currentTimeMillis();
        if (atual == null || atual.bruto() != bruto) {
            ModelFile arquivo = null;
            try {
                arquivo = ModelFile.load("npc", new ByteArrayInputStream(bruto));
                arquivo.registerLocalCache(carregador());
                carregador().setModel(perfil, arquivo.getDataBlock(), true);
            } catch (Exception e) {
                System.out.println("[NPC-CPM] Falha ao carregar modelo CPM: " + e);
            }
            APLICADOS.put(id, new Aplicado(bruto, arquivo, agora));
        } else if (atual.arquivo() != null && agora - atual.ultimoRegistro() > INTERVALO_REGISTRO_MS) {
            atual.arquivo().registerLocalCache(carregador());
            APLICADOS.put(id, new Aplicado(atual.bruto(), atual.arquivo(), agora));
        }
    }

    public static byte[] converterProjeto(File projeto) throws Exception {
        AtomicReference<String> erro = new AtomicReference<>();
        UI ui = (UI) Proxy.newProxyInstance(UI.class.getClassLoader(), new Class<?>[]{UI.class}, (proxy, metodo, args) -> {
            String nome = metodo.getName();
            if (nome.startsWith("i18n")) return args != null && args.length > 0 ? String.valueOf(args[0]) : "";
            if (nome.equals("executeLater") && args != null && args[0] instanceof Runnable r) {
                r.run();
                return null;
            }
            if (nome.equals("onGuiException") && args != null && args[1] instanceof Throwable t) {
                erro.set(String.valueOf(t));
                return null;
            }
            if (nome.equals("displayMessagePopup") && args != null && args.length > 1) {
                erro.set(String.valueOf(args[1]));
                return null;
            }
            Class<?> tipo = metodo.getReturnType();
            if (tipo == boolean.class) return false;
            if (tipo == int.class) return 0;
            if (tipo == long.class) return 0L;
            if (tipo == float.class) return 0f;
            if (tipo == double.class) return 0d;
            return null;
        });

        Editor editor = new Editor();
        editor.setUI(ui);
        editor.load(projeto).get(60, TimeUnit.SECONDS);
        if (editor.animEnc == null) editor.animEnc = GetFreeSkinSlots.getDefault(editor);
        if (!Exporter.exportTempModel(editor, ui)) {
            throw new IllegalStateException(erro.get() != null ? erro.get() : "export");
        }
        File saida = new File(new File(MinecraftClientAccess.get().getGameDir(), "player_models"), ".temp.cpmmodel");
        return Files.readAllBytes(saida.toPath());
    }
}
