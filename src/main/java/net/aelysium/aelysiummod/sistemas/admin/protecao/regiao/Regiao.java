package net.aelysium.aelysiummod.sistemas.admin.protecao.regiao;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class Regiao {

    private static final String CHAVE_VERSAO_FLAGS = "versaoFlags";
    private static final int VERSAO_FLAGS = 2;

    private String nome;
    private final BlockPos posicaoMinima;
    private final BlockPos posicaoMaxima;
    private final EnumMap<FlagRegiao, Boolean> flags;
    private final Set<UUID> donos;
    private boolean donoOps = false;
    private String mensagemEntrada;
    private String mensagemSaida;
    private ResourceLocation dimensao;
    private int volumeCache = -1;

    private boolean domoAtivo = false;
    private int domoCor = 0x59BFFF;
    private float domoDistancia = 80.0f;

    public Regiao(String nome, BlockPos posicaoMinima, BlockPos posicaoMaxima, ResourceLocation dimensao) {
        this.nome = nome;
        this.posicaoMinima = posicaoMinima;
        this.posicaoMaxima = posicaoMaxima;
        this.dimensao = dimensao;
        this.flags = new EnumMap<>(FlagRegiao.class);
        this.donos = new HashSet<>();
        this.mensagemEntrada = "";
        this.mensagemSaida = "";
        inicializarFlagsDefault();
    }

    public Regiao(String nome, BlockPos posicaoMinima, BlockPos posicaoMaxima) {
        this(nome, posicaoMinima, posicaoMaxima, ResourceLocation.withDefaultNamespace("overworld"));
    }

    private void inicializarFlagsDefault() {
        for (FlagRegiao flag : FlagRegiao.values()) {
            flags.put(flag, false);
        }
        flags.put(FlagRegiao.ENTRAR_REGIAO, true);
        flags.put(FlagRegiao.SAIR_REGIAO, true);
        flags.put(FlagRegiao.FOME, true);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BlockPos getPosicaoMinima() { return posicaoMinima; }
    public BlockPos getPosicaoMaxima() { return posicaoMaxima; }
    public ResourceLocation getDimensao() { return dimensao; }
    public void setDimensao(ResourceLocation dimensao) { this.dimensao = dimensao; }

    public BlockPos getPosicaoCentral() {
        return new BlockPos(
                (posicaoMinima.getX() + posicaoMaxima.getX()) / 2,
                (posicaoMinima.getY() + posicaoMaxima.getY()) / 2,
                (posicaoMinima.getZ() + posicaoMaxima.getZ()) / 2
        );
    }

    public EnumMap<FlagRegiao, Boolean> getFlags() { return flags; }
    public void setFlag(FlagRegiao flag, boolean valor) { flags.put(flag, valor); }
    public boolean getFlagValor(FlagRegiao flag) { return flags.getOrDefault(flag, false); }

    public Set<UUID> getDonos() { return donos; }
    public void adicionarDono(UUID uuid) { donos.add(uuid); }
    public void removerDono(UUID uuid) { donos.remove(uuid); }
    public boolean isDono(UUID uuid) { return donos.contains(uuid); }

    public boolean isDonoOps() { return donoOps; }
    public void setDonoOps(boolean donoOps) { this.donoOps = donoOps; }

    public boolean isDono(ServerPlayer jogador) {
        return donos.contains(jogador.getUUID()) || (donoOps && jogador.hasPermissions(2));
    }

    public boolean isDomoAtivo() { return domoAtivo; }
    public void setDomoAtivo(boolean domoAtivo) { this.domoAtivo = domoAtivo; }

    public int getDomoCor() { return domoCor; }
    public void setDomoCor(int domoCor) { this.domoCor = domoCor; }

    public float getDomoDistancia() { return domoDistancia; }
    public void setDomoDistancia(float domoDistancia) { this.domoDistancia = domoDistancia; }

    public String getMensagemEntrada() { return mensagemEntrada; }
    public void setMensagemEntrada(String mensagemEntrada) { this.mensagemEntrada = mensagemEntrada; }

    public String getMensagemSaida() { return mensagemSaida; }
    public void setMensagemSaida(String mensagemSaida) { this.mensagemSaida = mensagemSaida; }

    public boolean contemPosicao(BlockPos pos) {
        return pos.getX() >= posicaoMinima.getX() && pos.getX() <= posicaoMaxima.getX()
                && pos.getY() >= posicaoMinima.getY() && pos.getY() <= posicaoMaxima.getY()
                && pos.getZ() >= posicaoMinima.getZ() && pos.getZ() <= posicaoMaxima.getZ();
    }

    public boolean contemPosicao(BlockPos pos, ResourceLocation dim) {
        if (dim != null && !dim.equals(this.dimensao)) return false;
        return contemPosicao(pos);
    }

    public int getVolume() {
        if (volumeCache == -1) {
            int lx = Math.abs(posicaoMaxima.getX() - posicaoMinima.getX()) + 1;
            int ly = Math.abs(posicaoMaxima.getY() - posicaoMinima.getY()) + 1;
            int lz = Math.abs(posicaoMaxima.getZ() - posicaoMinima.getZ()) + 1;
            volumeCache = lx * ly * lz;
        }
        return volumeCache;
    }

    public CompoundTag salvarNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("nome", nome);
        tag.putInt("minX", posicaoMinima.getX());
        tag.putInt("minY", posicaoMinima.getY());
        tag.putInt("minZ", posicaoMinima.getZ());
        tag.putInt("maxX", posicaoMaxima.getX());
        tag.putInt("maxY", posicaoMaxima.getY());
        tag.putInt("maxZ", posicaoMaxima.getZ());
        tag.putString("dimensao", dimensao.toString());

        CompoundTag flagsTag = new CompoundTag();
        for (Map.Entry<FlagRegiao, Boolean> entry : flags.entrySet()) {
            flagsTag.putBoolean(entry.getKey().name(), entry.getValue());
        }
        tag.put("flags", flagsTag);
        tag.putInt(CHAVE_VERSAO_FLAGS, VERSAO_FLAGS);

        ListTag donosTag = new ListTag();
        for (UUID uuid : donos) {
            donosTag.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("donos", donosTag);
        tag.putBoolean("donoOps", donoOps);

        tag.putString("mensagemEntrada", mensagemEntrada);
        tag.putString("mensagemSaida", mensagemSaida);

        tag.putBoolean("domoAtivo", domoAtivo);
        tag.putInt("domoCor", domoCor);
        tag.putFloat("domoDistancia", domoDistancia);
        return tag;
    }

    public static Regiao carregarNBT(CompoundTag tag) {
        String nome = tag.getString("nome");
        BlockPos min = new BlockPos(tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ"));
        BlockPos max = new BlockPos(tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"));

        ResourceLocation dim = tag.contains("dimensao")
                ? ResourceLocation.parse(tag.getString("dimensao"))
                : ResourceLocation.withDefaultNamespace("overworld");

        Regiao regiao = new Regiao(nome, min, max, dim);

        CompoundTag flagsTag = tag.getCompound("flags");
        for (FlagRegiao flag : FlagRegiao.values()) {
            if (flagsTag.contains(flag.name())) {
                regiao.setFlag(flag, flagsTag.getBoolean(flag.name()));
            }
        }
        if (tag.getInt(CHAVE_VERSAO_FLAGS) < VERSAO_FLAGS && flagsTag.contains(FlagRegiao.FOME.name())) {
            regiao.setFlag(FlagRegiao.FOME, !flagsTag.getBoolean(FlagRegiao.FOME.name()));
        }

        ListTag donosTag = tag.getList("donos", 8);
        for (int i = 0; i < donosTag.size(); i++) {
            regiao.adicionarDono(UUID.fromString(donosTag.getString(i)));
        }
        regiao.setDonoOps(tag.getBoolean("donoOps"));

        if (tag.contains("mensagemEntrada")) regiao.setMensagemEntrada(tag.getString("mensagemEntrada"));
        if (tag.contains("mensagemSaida")) regiao.setMensagemSaida(tag.getString("mensagemSaida"));

        if (tag.contains("domoAtivo")) regiao.setDomoAtivo(tag.getBoolean("domoAtivo"));
        if (tag.contains("domoCor")) regiao.setDomoCor(tag.getInt("domoCor"));
        if (tag.contains("domoDistancia")) regiao.setDomoDistancia(tag.getFloat("domoDistancia"));

        return regiao;
    }
}