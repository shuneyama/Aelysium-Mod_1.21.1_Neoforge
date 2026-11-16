package net.aelysium.aelysiummod.compatibilidade;

import java.util.Set;

public class ItensVisiveisSemOp {

    public static final Set<String> IDS = Set.of(
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
            "ciane_espada"
    );

    public static boolean estaLiberado(String path) {
        return IDS.contains(path) || net.aelysium.aelysiummod.registro.ModBlocosDecoracao.ENTRADAS.containsKey(path);
    }

    private ItensVisiveisSemOp() {
    }
}
