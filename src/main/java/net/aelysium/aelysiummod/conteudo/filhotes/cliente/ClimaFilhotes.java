package net.aelysium.aelysiummod.conteudo.filhotes.cliente;

import net.aelysium.aelysiummod.conteudo.filhotes.clima.CacheClimaCliente;
import net.aelysium.aelysiummod.conteudo.filhotes.clima.ClimaAnimal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public enum ClimaFilhotes {
    TEMPERADO("temperate"),
    FRIO("cold"),
    QUENTE("warm");

    private final String sufixo;

    ClimaFilhotes(String sufixo) {
        this.sufixo = sufixo;
    }

    public String sufixo() {
        return sufixo;
    }

    public static ClimaFilhotes de(Entity entidade) {
        byte id = CacheClimaCliente.obter(entidade.getId());
        if (id < 0) {
            id = entidade.getPersistentData().contains(ClimaAnimal.CHAVE)
                    ? entidade.getPersistentData().getByte(ClimaAnimal.CHAVE)
                    : ClimaAnimal.calcular(entidade.level(), entidade.blockPosition());
        }
        ClimaFilhotes[] todos = values();
        return id >= 0 && id < todos.length ? todos[id] : TEMPERADO;
    }

    public static <T> Map<ClimaFilhotes, T> mapa(Function<ClimaFilhotes, T> fabrica) {
        Map<ClimaFilhotes, T> mapa = new EnumMap<>(ClimaFilhotes.class);
        for (ClimaFilhotes clima : values()) {
            mapa.put(clima, fabrica.apply(clima));
        }
        return mapa;
    }

    public ResourceLocation textura(String pasta, String base, boolean filhote) {
        return UtilFilhotes.textura((filhote ? "filhotes/" : "adultos/") + pasta + "/" + base + "_" + sufixo + (filhote ? "_baby" : ""));
    }
}
