package net.aelysium.aelysiummod.compatibilidade;

import net.aelysium.aelysiummod.registro.AbaCriativo;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItensVisiveisSemOp {

    public static final Set<String> SEMPRE_VISIVEIS = Set.of(
            "ciane_minerio",
            "ciane_cru",
            "ciane_fragmento",
            "ciane_barra",
            "ciane_capacete",
            "ciane_peitoral",
            "ciane_calcinha",
            "ciane_botas",
            "ciane_picareta",
            "ciane_pa",
            "ciane_enxada",
            "ciane_machado",
            "ciane_espada",
            "cueca_prefeito"
    );

    private static volatile Set<String> restritos;

    public static boolean estaLiberado(String path) {
        return SEMPRE_VISIVEIS.contains(path) || !restritos().contains(path);
    }

    private static Set<String> restritos() {
        Set<String> atual = restritos;
        if (atual == null) {
            atual = Stream.of(AbaCriativo.conteudoItens(), AbaCriativo.conteudoArmas(), AbaCriativo.conteudoBlocos())
                    .flatMap(List::stream)
                    .map(item -> BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
                    .collect(Collectors.toUnmodifiableSet());
            restritos = atual;
        }
        return atual;
    }

    private ItensVisiveisSemOp() {
    }
}
