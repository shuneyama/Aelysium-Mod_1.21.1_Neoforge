package net.aelysium.aelysiummod.sistemas.chefes.base.utilitarios;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class EntidadesAtivas<T extends Entity> {
    private final Set<T> entidades = Collections.newSetFromMap(new WeakHashMap<>());

    public void adicionar(T entidade) {
        entidades.add(entidade);
    }

    public void remover(T entidade) {
        entidades.remove(entidade);
    }

    public boolean existeEm(Level nivel) {
        if (entidades.isEmpty()) return false;
        for (T entidade : entidades) {
            if (!entidade.isRemoved() && entidade.level() == nivel) return true;
        }
        return false;
    }

    public Iterable<T> todas() {
        entidades.removeIf(Entity::isRemoved);
        return entidades;
    }
}
