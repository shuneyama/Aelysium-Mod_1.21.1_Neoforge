package net.aelysium.aelysiummod.sistemas.mundo.bordas;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

public final class BordasPorDimensao {

    private static final Logger LOGGER = LoggerFactory.getLogger("aelysiummod");
    private static final String NOME_DADOS = "aelysium_borda";

    private BordasPorDimensao() {
    }

    public static class Dados extends SavedData {

        private CompoundTag borda = new CompoundTag();

        public static Dados carregar(CompoundTag tag, HolderLookup.Provider provedor) {
            Dados dados = new Dados();
            dados.borda = tag.getCompound("borda");
            return dados;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provedor) {
            tag.put("borda", borda);
            return tag;
        }

        boolean temDados() {
            return !borda.isEmpty();
        }

        void gravar(WorldBorder origem) {
            CompoundTag nova = new CompoundTag();
            origem.createSettings().write(nova);
            borda = nova;
            setDirty();
        }

        void aplicar(WorldBorder destino) {
            destino.applySettings(WorldBorder.Settings.read(new Dynamic<>(NbtOps.INSTANCE, borda), WorldBorder.DEFAULT_SETTINGS));
        }
    }

    private static Dados obter(ServerLevel nivel) {
        return nivel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Dados::new, Dados::carregar), NOME_DADOS);
    }

    @SuppressWarnings("unchecked")
    public static void isolar(MinecraftServer servidor) {
        ServerLevel principal = servidor.overworld();
        try {
            Field campo = WorldBorder.class.getDeclaredField("listeners");
            campo.setAccessible(true);
            List<BorderChangeListener> ouvintes = (List<BorderChangeListener>) campo.get(principal.getWorldBorder());
            ouvintes.removeIf(ouvinte -> ouvinte instanceof BorderChangeListener.DelegateBorderChangeListener);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Nao foi possivel separar as bordas por dimensao: ", e);
            return;
        }
        for (ServerLevel nivel : servidor.getAllLevels()) {
            if (nivel == principal) continue;
            Dados dados = obter(nivel);
            if (dados.temDados()) {
                dados.aplicar(nivel.getWorldBorder());
            } else {
                dados.gravar(nivel.getWorldBorder());
            }
        }
    }

    public static void salvar(ServerLevel nivel) {
        if (nivel.dimension() == Level.OVERWORLD) return;
        obter(nivel).gravar(nivel.getWorldBorder());
    }

    public static void salvarTodas(MinecraftServer servidor) {
        for (ServerLevel nivel : servidor.getAllLevels()) {
            salvar(nivel);
        }
    }
}
