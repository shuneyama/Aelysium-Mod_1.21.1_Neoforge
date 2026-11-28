package net.aelysium.aelysiummod.nucleo.comando;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.aelysium.aelysiummod.nucleo.comando.condicao.CriteriosJogador;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.admin.protecao.cliente.RegiaoCacheCliente;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.rp.escola.ElementoMagico;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AelysiumOpcoesSeletor {

    private static final String P = "mensagem.aelysiummod.nucleo_comando.";
    private static final DynamicCommandExceptionType VALOR_INVALIDO =
            new DynamicCommandExceptionType(valor -> Component.translatable(P + "valor_seletor_invalido", valor));

    @FunctionalInterface
    private interface Criterio {
        Predicate<Entity> criar(String valor);
    }

    public static void registrar() {
        registrarOpcao("raca", P + "raca_raca",
                () -> Arrays.stream(TipoRaca.values()).map(raca -> raca.id).toList(),
                true,
                valor -> {
                    TipoRaca raca = TipoRaca.fromId(valor);
                    return entidade -> CriteriosJogador.raca(entidade, raca);
                });

        registrarOpcao("deus", P + "deus_deus",
                () -> Arrays.stream(TipoDeus.values()).map(deus -> deus.id).toList(),
                false,
                valor -> {
                    TipoDeus deus = TipoDeus.fromId(valor);
                    return entidade -> CriteriosJogador.deus(entidade, deus);
                });

        registrarOpcao("escola", P + "escola_escola",
                () -> Arrays.stream(ElementoMagico.values()).map(ElementoMagico::id).toList(),
                true,
                valor -> {
                    ElementoMagico elemento = ElementoMagico.doId(valor);
                    return entidade -> CriteriosJogador.escola(entidade, elemento);
                });

        registrarOpcao("live", P + "live_live",
                () -> List.of("true", "false"),
                true,
                valor -> {
                    boolean esperado = Boolean.parseBoolean(valor);
                    return entidade -> CriteriosJogador.live(entidade) == esperado;
                });

        registrarOpcao("vip", P + "vip_vip",
                List::of,
                false,
                valor -> entidade -> CriteriosJogador.vip(entidade, valor));

        registrarOpcao("regiao", P + "regiao_regiao",
                AelysiumOpcoesSeletor::nomesRegioes,
                false,
                valor -> entidade -> CriteriosJogador.regiao(entidade, valor));
    }

    private static void registrarOpcao(String nome, String chaveDescricao, Supplier<Collection<String>> sugestoes, boolean estrito, Criterio criterio) {
        EntitySelectorOptions.register(nome, parser -> {
            parser.setSuggestions((construtor, consumidor) -> sugerir(construtor, sugestoes.get()));
            StringReader leitor = parser.getReader();
            int inicio = leitor.getCursor();
            boolean negado = parser.shouldInvertValue();
            String valor = leitor.readString();
            if (!valorAceito(valor, sugestoes.get(), estrito)) {
                leitor.setCursor(inicio);
                throw VALOR_INVALIDO.createWithContext(leitor, valor);
            }
            Predicate<Entity> teste = criterio.criar(valor);
            parser.addPredicate(entidade -> teste.test(entidade) != negado);
        }, parser -> AelysiumGuarda.isAutorizadoAgora(), Component.translatable(chaveDescricao));
    }

    private static boolean valorAceito(String valor, Collection<String> conhecidos, boolean estrito) {
        if (valor.isEmpty()) return false;
        String minusculo = valor.toLowerCase(Locale.ROOT);
        boolean prefixo = false;
        for (String conhecido : conhecidos) {
            String alvo = conhecido.toLowerCase(Locale.ROOT);
            if (alvo.equals(minusculo)) return true;
            if (alvo.startsWith(minusculo)) prefixo = true;
        }
        return !estrito && !prefixo;
    }

    private static CompletableFuture<Suggestions> sugerir(SuggestionsBuilder construtor, Collection<String> valores) {
        String restante = construtor.getRemaining().toLowerCase(Locale.ROOT);
        boolean negado = restante.startsWith("!");
        String prefixo = negado ? restante.substring(1) : restante;
        for (String valor : valores) {
            String exibido = valor.matches("[A-Za-z0-9_.+-]*") ? valor : "\"" + valor + "\"";
            if (!exibido.toLowerCase(Locale.ROOT).startsWith(prefixo)) continue;
            if (!negado) construtor.suggest(exibido);
            construtor.suggest("!" + exibido);
        }
        return construtor.buildFuture();
    }

    private static Collection<String> nomesRegioes() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return RegiaoCacheCliente.getEntries().stream().map(RegiaoCacheCliente.RegiaoEntry::nome).toList();
        }
        return GerenciadorRegioes.getInstance().getNomesRegioes();
    }
}
