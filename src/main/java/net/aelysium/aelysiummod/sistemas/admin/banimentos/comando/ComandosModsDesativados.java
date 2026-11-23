package net.aelysium.aelysiummod.sistemas.admin.banimentos.comando;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoModsDesativados.ModDesativado;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.GerenciadorFiltroReceitas;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ComandosModsDesativados {
    private static final List<String> PARTES = List.of("loot", "trocas", "receitas", "worldgen", "conquistas");
    private static final String TUDO = "tudo";
    private static final Set<String> IDS_PROTEGIDOS = Set.of("minecraft", "neoforge", "aelysiummod");
    private static final String PREFIXO = "mensagem.aelysiummod.banimentos.";

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mod")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("desativar")
                        .then(Commands.argument("mod_id", StringArgumentType.word())
                                .suggests((ctx, construtor) -> sugerir(idsInstalados(), construtor))
                                .then(Commands.argument("parte", StringArgumentType.word())
                                        .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(partesComTudo(), construtor))
                                        .executes(ComandosModsDesativados::desativar))))
                .then(Commands.literal("reativar")
                        .then(Commands.argument("mod_id", StringArgumentType.word())
                                .suggests((ctx, construtor) -> sugerir(ConfiguracaoModsDesativados.getAll().keySet().stream(), construtor))
                                .then(Commands.argument("parte", StringArgumentType.word())
                                        .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(partesComTudo(), construtor))
                                        .executes(ComandosModsDesativados::reativar))))
                .then(Commands.literal("listar")
                        .executes(ComandosModsDesativados::listar))
                .then(Commands.literal("recarregar")
                        .executes(ComandosModsDesativados::recarregar));
    }

    private static int desativar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        String modId = StringArgumentType.getString(ctx, "mod_id").toLowerCase();
        String parte = StringArgumentType.getString(ctx, "parte").toLowerCase();
        if (!parteValida(parte)) {
            fonte.sendFailure(TextoComando.traduzir(PREFIXO + "parte_invalida", ChatFormatting.RED, parte));
            return 0;
        }
        if (!ModList.get().isLoaded(modId)) {
            fonte.sendFailure(TextoComando.traduzir(PREFIXO + "mod_nao_instalado", ChatFormatting.RED, modId));
            return 0;
        }
        ModDesativado atual = copiar(ConfiguracaoModsDesativados.getAll().get(modId));
        boolean mudou = false;
        for (String item : alvo(parte)) {
            if (!valor(atual, item)) {
                definir(atual, item, true);
                mudou = true;
            }
        }
        if (!mudou) {
            fonte.sendFailure(TextoComando.traduzir(PREFIXO + "parte_ja_desativada", ChatFormatting.GRAY, modId, parte));
            return 0;
        }
        salvar(fonte, modId, atual);
        fonte.sendSuccess(() -> TextoComando.traduzir(PREFIXO + "mod_parte_desativada", ChatFormatting.WHITE, modId, parte), true);
        avisos(fonte, parte);
        return 1;
    }

    private static int reativar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        String modId = StringArgumentType.getString(ctx, "mod_id").toLowerCase();
        String parte = StringArgumentType.getString(ctx, "parte").toLowerCase();
        if (!parteValida(parte)) {
            fonte.sendFailure(TextoComando.traduzir(PREFIXO + "parte_invalida", ChatFormatting.RED, parte));
            return 0;
        }
        ModDesativado existente = ConfiguracaoModsDesativados.getAll().get(modId);
        if (existente == null) {
            fonte.sendFailure(TextoComando.traduzir(PREFIXO + "mod_nao_esta_na_lista_de_capados", ChatFormatting.RED, modId));
            return 0;
        }
        ModDesativado atual = copiar(existente);
        boolean mudou = false;
        for (String item : alvo(parte)) {
            if (valor(atual, item)) {
                definir(atual, item, false);
                mudou = true;
            }
        }
        if (!mudou) {
            fonte.sendFailure(TextoComando.traduzir(PREFIXO + "parte_nao_desativada", ChatFormatting.RED, modId, parte));
            return 0;
        }
        salvar(fonte, modId, atual);
        fonte.sendSuccess(() -> TextoComando.traduzir(PREFIXO + "mod_parte_reativada", ChatFormatting.WHITE, modId, parte), true);
        avisos(fonte, parte);
        return 1;
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack fonte = ctx.getSource();
        Map<String, ModDesativado> todos = ConfiguracaoModsDesativados.getAll();
        if (todos.isEmpty()) {
            fonte.sendSuccess(() -> Component.translatable(PREFIXO + "nenhum_mod_capado_no_momento"), false);
            return 1;
        }
        fonte.sendSuccess(() -> TextoComando.traduzir(PREFIXO + "mods_capados", ChatFormatting.GOLD, Component.literal(String.valueOf(todos.size()))), false);
        for (Map.Entry<String, ModDesativado> entrada : todos.entrySet()) {
            String modId = entrada.getKey();
            ModDesativado cap = entrada.getValue();
            String nome = ModList.get().getModContainerById(modId)
                    .map(c -> c.getModInfo().getDisplayName())
                    .orElse(modId);
            MutableComponent titulo = TextoComando.traduzir(PREFIXO + "lista_mod", ChatFormatting.DARK_GRAY, Component.literal(nome).withStyle(ChatFormatting.YELLOW), Component.literal(modId));
            if (!ModList.get().isLoaded(modId)) {
                titulo.append(Component.translatable(PREFIXO + "lista_nao_instalado"));
            }
            MutableComponent partes = Component.literal("  ");
            for (String parte : PARTES) {
                partes.append(botao(modId, parte, valor(cap, parte))).append(" ");
            }
            fonte.sendSuccess(() -> titulo, false);
            fonte.sendSuccess(() -> partes, false);
        }
        return todos.size();
    }

    private static int recarregar(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoModsDesativados.load();
        GerenciadorFiltroReceitas.reapplyFilter(ctx.getSource().getServer());
        int total = ConfiguracaoModsDesativados.getAll().size();
        ctx.getSource().sendSuccess(() -> TextoComando.traduzir(PREFIXO + "aelysium_config_de_mods_capados", ChatFormatting.WHITE, total), true);
        return 1;
    }

    private static MutableComponent botao(String modId, String parte, boolean desativada) {
        String acao = desativada ? "reativar" : "desativar";
        String comando = "/aelysium mod " + acao + " " + modId + " " + parte;
        Style estilo = Style.EMPTY
                .withColor(desativada ? ChatFormatting.RED : ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(PREFIXO + (desativada ? "lista_dica_reativar" : "lista_dica_desativar"))));
        return Component.literal((desativada ? "✘ " : "✔ ") + parte).withStyle(estilo);
    }

    private static void salvar(CommandSourceStack fonte, String modId, ModDesativado cap) {
        if (PARTES.stream().noneMatch(parte -> valor(cap, parte))) {
            ConfiguracaoModsDesativados.removeCap(modId);
        } else {
            ConfiguracaoModsDesativados.setCap(modId, cap.loot, cap.trocas, cap.receitas, cap.worldgen, cap.conquistas);
        }
        GerenciadorFiltroReceitas.reapplyFilter(fonte.getServer());
    }

    private static void avisos(CommandSourceStack fonte, String parte) {
        if (parte.equals("trocas") || parte.equals("conquistas") || parte.equals(TUDO)) {
            fonte.sendSuccess(() -> Component.translatable(PREFIXO + "aviso_reload"), false);
        }
        if (parte.equals("worldgen") || parte.equals(TUDO)) {
            fonte.sendSuccess(() -> Component.translatable(PREFIXO + "aviso_worldgen"), false);
        }
    }

    private static boolean parteValida(String parte) {
        return parte.equals(TUDO) || PARTES.contains(parte);
    }

    private static List<String> alvo(String parte) {
        return parte.equals(TUDO) ? PARTES : List.of(parte);
    }

    private static Iterable<String> partesComTudo() {
        return Stream.concat(PARTES.stream(), Stream.of(TUDO)).toList();
    }

    private static Stream<String> idsInstalados() {
        return ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .filter(id -> !IDS_PROTEGIDOS.contains(id))
                .distinct();
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> sugerir(
            Stream<String> ids, com.mojang.brigadier.suggestion.SuggestionsBuilder construtor) {
        String digitado = construtor.getRemainingLowerCase();
        ids.filter(id -> id.contains(digitado)).sorted().forEach(construtor::suggest);
        return construtor.buildFuture();
    }

    private static ModDesativado copiar(ModDesativado origem) {
        if (origem == null) return new ModDesativado();
        return new ModDesativado(origem.loot, origem.trocas, origem.receitas, origem.worldgen, origem.conquistas);
    }

    private static boolean valor(ModDesativado cap, String parte) {
        return switch (parte) {
            case "loot" -> cap.loot;
            case "trocas" -> cap.trocas;
            case "receitas" -> cap.receitas;
            case "worldgen" -> cap.worldgen;
            default -> cap.conquistas;
        };
    }

    private static void definir(ModDesativado cap, String parte, boolean valor) {
        switch (parte) {
            case "loot" -> cap.loot = valor;
            case "trocas" -> cap.trocas = valor;
            case "receitas" -> cap.receitas = valor;
            case "worldgen" -> cap.worldgen = valor;
            default -> cap.conquistas = valor;
        }
    }
}
