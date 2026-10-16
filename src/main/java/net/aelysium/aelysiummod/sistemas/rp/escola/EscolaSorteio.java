package net.aelysium.aelysiummod.sistemas.rp.escola;

import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;

import java.util.List;
import java.util.Random;

public class EscolaSorteio {

    private static final Random RANDOM = new Random();

    private static final ElementoMagico[] ORDEM_PADRAO = {
            ElementoMagico.FOGO, ElementoMagico.GELO, ElementoMagico.NATUREZA, ElementoMagico.RAIO,
            ElementoMagico.SANGUE, ElementoMagico.VAZIO, ElementoMagico.NENHUMA
    };

    private static final ElementoMagico[] OUTROS_VALKYRIA = {
            ElementoMagico.RAIO, ElementoMagico.NATUREZA, ElementoMagico.GELO, ElementoMagico.FOGO
    };

    public record Resultado(List<ElementoMagico> elementos) {

        public String textoColorido() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < elementos.size(); i++) {
                if (i > 0) sb.append("§f e ");
                sb.append(elementos.get(i).colorido());
            }
            return sb.toString();
        }
    }

    public static Resultado sortear(TipoRaca raca) {
        if (raca == TipoRaca.ROBO) {
            return new Resultado(List.of(ElementoMagico.NENHUMA));
        }

        if (raca == TipoRaca.VALKYRIA) {
            return sortearValkyria();
        }

        int[] tabela = tabelaPadrao(raca);
        int d100 = 1 + RANDOM.nextInt(100);
        return new Resultado(List.of(resolverPadrao(tabela, d100)));
    }

    private static int[] tabelaPadrao(TipoRaca raca) {
        return switch (raca) {
            case TIEFLING -> new int[]{61, 12, 12, 12, 1, 1, 1};
            case UNDYNE -> new int[]{12, 61, 12, 12, 1, 1, 1};
            case ELVARIN -> new int[]{12, 12, 61, 12, 1, 1, 1};
            case DRACONO -> new int[]{12, 12, 12, 61, 1, 1, 1};
            default -> new int[]{10, 10, 10, 10, 1, 1, 58};
        };
    }

    private static ElementoMagico resolverPadrao(int[] tabela, int d100) {
        int acumulado = 0;
        for (int i = 0; i < tabela.length; i++) {
            acumulado += tabela[i];
            if (d100 <= acumulado) return ORDEM_PADRAO[i];
        }
        return ElementoMagico.NENHUMA;
    }

    private static Resultado sortearValkyria() {
        int d100 = 1 + RANDOM.nextInt(100);
        if (d100 <= 60) {
            return new Resultado(List.of(ElementoMagico.SACRA));
        }

        ElementoMagico outro = OUTROS_VALKYRIA[RANDOM.nextInt(4)];
        return new Resultado(List.of(ElementoMagico.SACRA, outro));
    }

    private EscolaSorteio() {
    }
}
