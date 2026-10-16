package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.nucleo.armazenamento.DadosEmArquivos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class DadosDificuldade extends DadosEmArquivos {

    public static final int NIVEL_MAXIMO = 10;
    private static final String NOME_DADOS = "aelysium_dificuldade";

    private int nivel = 0;
    private boolean griefing = false;
    private boolean hordas = false;

    public static DadosDificuldade get(MinecraftServer servidor) {
        return obter(servidor, new Factory<>(DadosDificuldade::new, DadosDificuldade::carregar), NOME_DADOS, DadosEmArquivos.Destino.global("dificuldade", "estado.json"));
    }

    private static DadosDificuldade carregar(CompoundTag tag, HolderLookup.Provider provedor) {
        DadosDificuldade dados = new DadosDificuldade();
        dados.nivel = Math.max(0, Math.min(NIVEL_MAXIMO, tag.getInt("nivel")));
        dados.griefing = tag.getBoolean("griefing");
        dados.hordas = tag.getBoolean("hordas");
        return dados;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provedor) {
        tag.putInt("nivel", nivel);
        tag.putBoolean("griefing", griefing);
        tag.putBoolean("hordas", hordas);
        return tag;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isGriefing() {
        return griefing;
    }

    public boolean isHordas() {
        return hordas;
    }

    public void definir(int nivel, boolean griefing, boolean hordas) {
        this.nivel = Math.max(0, Math.min(NIVEL_MAXIMO, nivel));
        this.griefing = griefing;
        this.hordas = hordas;
        setDirty();
    }
}
