package net.aelysium.aelysiummod.sistemas.chefes.base.modelo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModeloBedrock {
    private final List<OssoModelo> raizes;
    private final Map<String, OssoModelo> ossos;

    public ModeloBedrock(List<OssoModelo> raizes, Map<String, OssoModelo> ossos) {
        this.raizes = raizes;
        this.ossos = ossos;
    }

    public List<OssoModelo> raizes() {
        return raizes;
    }

    public Collection<OssoModelo> todos() {
        return ossos.values();
    }

    public Optional<OssoModelo> getBone(String nome) {
        return Optional.ofNullable(ossos.get(nome));
    }

    public OssoModelo osso(String nome) {
        return ossos.get(nome);
    }
}
