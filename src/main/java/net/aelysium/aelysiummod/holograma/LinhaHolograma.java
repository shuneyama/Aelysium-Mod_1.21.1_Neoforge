package net.aelysium.aelysiummod.holograma;

import net.minecraft.nbt.CompoundTag;

public class LinhaHolograma {

    private String conteudo;
    private double offsetY;

    public static final double ALTURA_PADRAO = 0.3;

    public LinhaHolograma(String conteudo) {
        this.conteudo = conteudo;
        this.offsetY = ALTURA_PADRAO;
    }

    public LinhaHolograma(String conteudo, double offsetY) {
        this.conteudo = conteudo;
        this.offsetY = offsetY;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public CompoundTag salvarNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("conteudo", conteudo);
        tag.putDouble("offsetY", offsetY);
        return tag;
    }

    public static LinhaHolograma carregarNBT(CompoundTag tag) {
        String conteudo = tag.getString("conteudo");
        double offsetY = tag.contains("offsetY") ? tag.getDouble("offsetY") : ALTURA_PADRAO;
        return new LinhaHolograma(conteudo, offsetY);
    }
}