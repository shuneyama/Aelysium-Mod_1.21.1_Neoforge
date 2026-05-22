package net.aelysium.aelysiummod.holograma;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public class Holograma {

    private String nome;
    private String mundo;
    private double x, y, z;
    private final List<LinhaHolograma> linhas = new ArrayList<>();
    private boolean ativo = true;
    private double rangeVisibilidade = 48.0;
    private int intervaloAtualizacao = 20; // ticks

    public Holograma(String nome, String mundo, double x, double y, double z) {
        this.nome = nome;
        this.mundo = mundo;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMundo() {
        return mundo;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setPosicao(String mundo, double x, double y, double z) {
        this.mundo = mundo;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPosicao(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public List<LinhaHolograma> getLinhas() {
        return linhas;
    }

    public int getQuantidadeLinhas() {
        return linhas.size();
    }

    public LinhaHolograma getLinha(int index) {
        if (index < 0 || index >= linhas.size()) return null;
        return linhas.get(index);
    }

    public void adicionarLinha(String conteudo) {
        linhas.add(new LinhaHolograma(conteudo));
    }

    public void inserirLinha(int index, String conteudo) {
        if (index < 0) index = 0;
        if (index > linhas.size()) index = linhas.size();
        linhas.add(index, new LinhaHolograma(conteudo));
    }

    public boolean removerLinha(int index) {
        if (index < 0 || index >= linhas.size()) return false;
        linhas.remove(index);
        return true;
    }

    public boolean editarLinha(int index, String novoConteudo) {
        if (index < 0 || index >= linhas.size()) return false;
        linhas.get(index).setConteudo(novoConteudo);
        return true;
    }

    public boolean trocarLinhas(int index1, int index2) {
        if (index1 < 0 || index1 >= linhas.size()) return false;
        if (index2 < 0 || index2 >= linhas.size()) return false;
        LinhaHolograma temp = linhas.get(index1);
        linhas.set(index1, linhas.get(index2));
        linhas.set(index2, temp);
        return true;
    }

    public boolean definirAlturaLinha(int index, double altura) {
        if (index < 0 || index >= linhas.size()) return false;
        linhas.get(index).setOffsetY(altura);
        return true;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public double getRangeVisibilidade() {
        return rangeVisibilidade;
    }

    public void setRangeVisibilidade(double range) {
        this.rangeVisibilidade = range;
    }

    public int getIntervaloAtualizacao() {
        return intervaloAtualizacao;
    }

    public void setIntervaloAtualizacao(int ticks) {
        this.intervaloAtualizacao = ticks;
    }

    public double getAlturaTotal() {
        double total = 0;
        for (LinhaHolograma linha : linhas) {
            total += linha.getOffsetY();
        }
        return total;
    }

    public CompoundTag salvarNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("nome", nome);
        tag.putString("mundo", mundo);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putBoolean("ativo", ativo);
        tag.putDouble("rangeVisibilidade", rangeVisibilidade);
        tag.putInt("intervaloAtualizacao", intervaloAtualizacao);

        ListTag listaLinhas = new ListTag();
        for (LinhaHolograma linha : linhas) {
            listaLinhas.add(linha.salvarNBT());
        }
        tag.put("linhas", listaLinhas);

        return tag;
    }

    public static Holograma carregarNBT(CompoundTag tag) {
        String nome = tag.getString("nome");
        String mundo = tag.getString("mundo");
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");

        Holograma holo = new Holograma(nome, mundo, x, y, z);
        holo.ativo = tag.getBoolean("ativo");

        if (tag.contains("rangeVisibilidade")) {
            holo.rangeVisibilidade = tag.getDouble("rangeVisibilidade");
        }
        if (tag.contains("intervaloAtualizacao")) {
            holo.intervaloAtualizacao = tag.getInt("intervaloAtualizacao");
        }

        ListTag listaLinhas = tag.getList("linhas", 10);
        for (int i = 0; i < listaLinhas.size(); i++) {
            holo.linhas.add(LinhaHolograma.carregarNBT(listaLinhas.getCompound(i)));
        }

        return holo;
    }
}