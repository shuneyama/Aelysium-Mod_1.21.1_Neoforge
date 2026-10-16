package net.aelysium.aelysiummod.sistemas.admin.banimentos.comando;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.ConfiguracaoListaBanimentos;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComandoBanidos {

    private static final int POR_PAGINA = 8;

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("lista")
                .executes(ctx -> mostrarModids(ctx.getSource(), 1));
    }

    public static void registrarExtras(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("extras")
                        .requires(source -> AelysiumGuarda.isAutorizado())
                        .then(Commands.argument("alvo", StringArgumentType.word())
                                .executes(ctx -> {
                                    String alvo = StringArgumentType.getString(ctx, "alvo");
                                    Integer pagina = comoPagina(alvo);
                                    return pagina != null
                                            ? mostrarModids(ctx.getSource(), pagina)
                                            : mostrarItens(ctx.getSource(), alvo, 1);
                                })
                                .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                        .executes(ctx -> mostrarItens(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "alvo"),
                                                IntegerArgumentType.getInteger(ctx, "pagina")))
                                )
                        )
        );
    }

    private static String nomeDoMod(String modid) {
        return ModList.get().getModContainerById(modid)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modid);
    }

    private static Integer comoPagina(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int mostrarModids(CommandSourceStack source, int pagina) {
        Map<String, Long> porModid = ConfiguracaoListaBanimentos.getBannedItems().stream()
                .collect(Collectors.groupingBy(ResourceLocation::getNamespace, Collectors.counting()));
        List<String> modids = porModid.keySet().stream().sorted().collect(Collectors.toList());

        int total = modids.size();
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / POR_PAGINA));
        int paginaAtual = Math.max(1, Math.min(pagina, totalPaginas));
        int inicio = (paginaAtual - 1) * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, total);

        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatable("mensagem.aelysiummod.banimentos.itens_banidos"));
        source.sendSystemMessage(separador());

        if (total == 0) {
            source.sendSystemMessage(Component.translatable("mensagem.aelysiummod.banimentos.nenhum_item_banido_no_momento"));
        }

        for (int i = inicio; i < fim; i++) {
            String modid = modids.get(i);
            long qtd = porModid.get(modid);
            String nomeMod = nomeDoMod(modid);
            MutableComponent linha = Component.literal("  §7» ");
            linha.append(botaoClicavel(
                    Component.translatableEscape(qtd > 1
                            ? "mensagem.aelysiummod.banimentos.mod_com_itens"
                            : "mensagem.aelysiummod.banimentos.mod_com_item", nomeMod, qtd),
                    "/extras " + modid,
                    Component.translatableEscape("mensagem.aelysiummod.banimentos.clique_ver_itens_do_mod", nomeMod)));
            source.sendSystemMessage(linha);
        }

        source.sendSystemMessage(separador());
        source.sendSystemMessage(navegacao(paginaAtual, totalPaginas, "/extras"));
        return 1;
    }

    private static int mostrarItens(CommandSourceStack source, String modid, int pagina) {
        List<ResourceLocation> itens = ConfiguracaoListaBanimentos.getBannedItems().stream()
                .filter(rl -> rl.getNamespace().equals(modid))
                .sorted(Comparator.comparing(ResourceLocation::getPath))
                .collect(Collectors.toList());

        int total = itens.size();
        if (total == 0) {
            source.sendFailure(Component.translatableEscape("mensagem.aelysiummod.banimentos.nenhum_item_banido_para_o_modid", modid));
            return 0;
        }

        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / POR_PAGINA));
        int paginaAtual = Math.max(1, Math.min(pagina, totalPaginas));
        int inicio = (paginaAtual - 1) * POR_PAGINA;
        int fim = Math.min(inicio + POR_PAGINA, total);

        String nomeMod = nomeDoMod(modid);
        source.sendSystemMessage(separador());
        source.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.banimentos.itens_banidos_2", nomeMod));
        source.sendSystemMessage(separador());

        for (int i = inicio; i < fim; i++) {
            ResourceLocation item = itens.get(i);
            Item itemObj = BuiltInRegistries.ITEM.get(item);
            Component nome = itemObj != Items.AIR ? itemObj.getDescription() : Component.literal(item.getPath());
            MutableComponent linha = Component.literal("  §7" + (i + 1) + ". §f");
            linha.append(nome);
            source.sendSystemMessage(linha);
        }

        source.sendSystemMessage(separador());
        source.sendSystemMessage(navegacao(paginaAtual, totalPaginas, "/extras " + modid));

        MutableComponent voltar = Component.literal("  ");
        voltar.append(botaoClicavel(
                Component.translatable("mensagem.aelysiummod.banimentos.voltar"),
                "/aelysium itens lista",
                Component.translatable("mensagem.aelysiummod.banimentos.voltar_lista_mods")));
        source.sendSystemMessage(voltar);
        return 1;
    }

    private static MutableComponent separador() {
        return Component.literal("§8§m──────────────────────────────§r");
    }

    private static MutableComponent botaoClicavel(Component texto, String comando, Component tooltip) {
        return texto.copy().withStyle(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
    }

    private static MutableComponent navegacao(int paginaAtual, int totalPaginas, String baseCmd) {
        MutableComponent nav = Component.literal("  ");
        if (paginaAtual > 1) {
            nav.append(botaoClicavel(Component.literal("§7[§f◀§7]"), baseCmd + " " + (paginaAtual - 1),
                    Component.translatable("mensagem.aelysiummod.comum.pagina_anterior")));
            nav.append(Component.literal("  "));
        }
        nav.append(Component.translatableEscape("mensagem.aelysiummod.banimentos.pagina", paginaAtual, totalPaginas));
        if (paginaAtual < totalPaginas) {
            nav.append(Component.literal("  "));
            nav.append(botaoClicavel(Component.literal("§7[§f▶§7]"), baseCmd + " " + (paginaAtual + 1),
                    Component.translatable("mensagem.aelysiummod.comum.proxima_pagina")));
        }
        return nav;
    }
}
