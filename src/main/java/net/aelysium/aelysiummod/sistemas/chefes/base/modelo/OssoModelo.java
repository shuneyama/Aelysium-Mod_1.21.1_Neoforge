package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

import java.util.ArrayList;
import java.util.List;

public class OssoModelo {
    private final String nome;
    private final List<CuboModelo> cubos = new ArrayList<>();
    private final List<OssoModelo> filhos = new ArrayList<>();
    private final float pivoX;
    private final float pivoY;
    private final float pivoZ;
    private final float rotInicialX;
    private final float rotInicialY;
    private final float rotInicialZ;
    private final boolean oculto;

    private float rotX;
    private float rotY;
    private float rotZ;
    private float posX;
    private float posY;
    private float posZ;
    private float escalaX = 1;
    private float escalaY = 1;
    private float escalaZ = 1;

    public OssoModelo(String nome, float pivoX, float pivoY, float pivoZ, float rotInicialX, float rotInicialY, float rotInicialZ, boolean oculto) {
        this.nome = nome;
        this.pivoX = pivoX;
        this.pivoY = pivoY;
        this.pivoZ = pivoZ;
        this.rotInicialX = rotInicialX;
        this.rotInicialY = rotInicialY;
        this.rotInicialZ = rotInicialZ;
        this.oculto = oculto;
        restaurar();
    }

    public void restaurar() {
        rotX = rotInicialX;
        rotY = rotInicialY;
        rotZ = rotInicialZ;
        posX = 0;
        posY = 0;
        posZ = 0;
        escalaX = 1;
        escalaY = 1;
        escalaZ = 1;
    }

    public String getName() {
        return nome;
    }

    public List<CuboModelo> getCubos() {
        return cubos;
    }

    public List<OssoModelo> getFilhos() {
        return filhos;
    }

    public boolean isOculto() {
        return oculto;
    }

    public float getPivoX() {
        return pivoX;
    }

    public float getPivoY() {
        return pivoY;
    }

    public float getPivoZ() {
        return pivoZ;
    }

    public float getRotInicialX() {
        return rotInicialX;
    }

    public float getRotInicialY() {
        return rotInicialY;
    }

    public float getRotInicialZ() {
        return rotInicialZ;
    }

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public void setPos(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public void setRot(float x, float y, float z) {
        this.rotX = x;
        this.rotY = y;
        this.rotZ = z;
    }

    public float getEscalaX() {
        return escalaX;
    }

    public float getEscalaY() {
        return escalaY;
    }

    public float getEscalaZ() {
        return escalaZ;
    }

    public void setEscala(float x, float y, float z) {
        this.escalaX = x;
        this.escalaY = y;
        this.escalaZ = z;
    }
}
