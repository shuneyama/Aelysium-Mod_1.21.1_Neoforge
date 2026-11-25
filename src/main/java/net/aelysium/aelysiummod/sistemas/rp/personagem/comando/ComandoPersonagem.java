package net.aelysium.aelysiummod.sistemas.rp.personagem.comando;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.personagem.AtributosPersonagem;
import net.aelysium.aelysiummod.sistemas.rp.personagem.Personagem;
import net.aelysium.aelysiummod.sistemas.rp.personagem.RegistroPersonagens;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ComandoPersonagem {
    private static final int PERMISSAO = 2;

    private ComandoPersonagem() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("personagem")
                .requires(cs -> cs.hasPermission(PERMISSAO))
                .then(Commands.argument("personagem", StringArgumentType.word())
                        .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(
                                java.util.stream.Stream.concat(RegistroPersonagens.todos().stream().map(p -> net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(p.nome()).replace(" ", "")),
                                        java.util.Arrays.stream(TipoDeus.values()).filter(d -> d != TipoDeus.NONE).map(d -> net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(d.displayName).replace(" ", ""))), construtor))
                        .then(Commands.literal("atributos")
                                .then(Commands.literal("listar").executes(ComandoPersonagem::listar))
                                .then(Commands.literal("limpar").executes(ComandoPersonagem::limpar))
                                .then(definir())
                                .then(Commands.literal("remover")
                                        .then(atributoArgumento().executes(ComandoPersonagem::remover)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> definir() {
        var operacao = Commands.argument("operacao", StringArgumentType.word())
                .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(AtributosPersonagem.OPERACOES.keySet(), construtor))
                .executes(ComandoPersonagem::definir);
        var valor = Commands.argument("valor", DoubleArgumentType.doubleArg())
                .executes(ComandoPersonagem::definir)
                .then(operacao);
        return Commands.literal("definir").then(atributoArgumento().then(valor));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> atributoArgumento() {
        return Commands.argument("atributo", ResourceLocationArgument.id())
                .suggests((ctx, construtor) -> {
                    String digitado = construtor.getRemainingLowerCase();
                    BuiltInRegistries.ATTRIBUTE.keySet().stream()
                            .map(ResourceLocation::toString)
                            .filter(id -> id.contains(digitado))
                            .forEach(construtor::suggest);
                    return construtor.buildFuture();
                });
    }

    private record Alvo(String id, String nome) {
    }

    private static Alvo alvo(CommandContext<CommandSourceStack> ctx) {
        String nome = StringArgumentType.getString(ctx, "personagem");
        Personagem p = RegistroPersonagens.buscar(nome);
        if (p == null) {
            for (Personagem candidato : RegistroPersonagens.todos()) {
                if ((net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(candidato.nome()).equalsIgnoreCase(nome) || candidato.nome().equalsIgnoreCase(nome))) {
                    p = candidato;
                    break;
                }
            }
        }
        if (p != null) return new Alvo(p.id(), p.nome());
        for (TipoDeus deus : TipoDeus.values()) {
            if (deus != TipoDeus.NONE && (deus.id.equalsIgnoreCase(nome) || net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(deus.displayName).equalsIgnoreCase(nome) || deus.displayName.equalsIgnoreCase(nome) || net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(deus.displayName).replace(" ", "").equalsIgnoreCase(nome))) {
                return new Alvo(AtributosPersonagem.chaveDeus(deus.id), deus.displayName);
            }
        }
        ctx.getSource().sendFailure(TextoComando.traduzir("mensagem.aelysiummod.personagem.nao_encontrado", ChatFormatting.RED, nome));
        return null;
    }

    private static String curto(ResourceLocation id) {
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
    }

    private static String formatar(double valor, String operacao) {
        String numero = net.minecraft.world.item.component.ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(valor);
        return "porcentagem".equals(operacao) ? numero + "%" : numero;
    }

    private static int definir(CommandContext<CommandSourceStack> ctx) {
        Alvo p = alvo(ctx);
        if (p == null) return 0;
        CommandSourceStack fonte = ctx.getSource();
        ResourceLocation atributoId = ResourceLocationArgument.getId(ctx, "atributo");
        if (BuiltInRegistries.ATTRIBUTE.getHolder(atributoId).isEmpty()) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.atributos.atributo_nao_encontrado", ChatFormatting.RED, atributoId));
            return 0;
        }
        String operacao;
        try {
            operacao = StringArgumentType.getString(ctx, "operacao");
        } catch (IllegalArgumentException e) {
            operacao = "soma";
        }
        if (!AtributosPersonagem.OPERACOES.containsKey(operacao)) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.atributos.operacao_invalida", ChatFormatting.RED, operacao));
            return 0;
        }
        double valor = DoubleArgumentType.getDouble(ctx, "valor");
        AtributosPersonagem.definir(p.id(), new AtributosPersonagem.Entrada(atributoId, operacao, valor));
        AtributosPersonagem.reaplicarUsuarios(fonte.getServer(), p.id());
        String texto = formatar(valor, operacao);
        String op = operacao;
        fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.personagem.atributo_definido", ChatFormatting.GREEN,
                curto(atributoId), p.nome(), texto, op), false);
        return 1;
    }

    private static int remover(CommandContext<CommandSourceStack> ctx) {
        Alvo p = alvo(ctx);
        if (p == null) return 0;
        CommandSourceStack fonte = ctx.getSource();
        ResourceLocation atributoId = ResourceLocationArgument.getId(ctx, "atributo");
        if (AtributosPersonagem.remover(p.id(), atributoId) == 0) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.personagem.atributo_nao_tem", ChatFormatting.RED, p.nome(), curto(atributoId)));
            return 0;
        }
        AtributosPersonagem.reaplicarUsuarios(fonte.getServer(), p.id());
        fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.personagem.atributo_removido", ChatFormatting.GREEN,
                curto(atributoId), p.nome()), false);
        return 1;
    }

    private static int limpar(CommandContext<CommandSourceStack> ctx) {
        Alvo p = alvo(ctx);
        if (p == null) return 0;
        CommandSourceStack fonte = ctx.getSource();
        if (AtributosPersonagem.limpar(p.id()) == 0) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.personagem.sem_atributos", ChatFormatting.RED, p.nome()));
            return 0;
        }
        AtributosPersonagem.reaplicarUsuarios(fonte.getServer(), p.id());
        fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.personagem.atributos_limpos", ChatFormatting.GREEN, p.nome()), false);
        return 1;
    }

    private static int listar(CommandContext<CommandSourceStack> ctx) {
        Alvo p = alvo(ctx);
        if (p == null) return 0;
        CommandSourceStack fonte = ctx.getSource();
        var entradas = AtributosPersonagem.obter(p.id());
        if (entradas.isEmpty()) {
            fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.personagem.sem_atributos", ChatFormatting.GRAY, p.nome()), false);
            return 1;
        }
        fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.personagem.atributos_de", ChatFormatting.GRAY, p.nome()), false);
        for (AtributosPersonagem.Entrada e : entradas) {
            MutableComponent linha = TextoComando.traduzir("mensagem.aelysiummod.personagem.lista_linha", ChatFormatting.GRAY,
                    curto(e.atributo()),
                    Component.literal(formatar(e.valor(), e.operacao())).withStyle(ChatFormatting.GREEN),
                    e.operacao());
            linha.append(" ");
            String comando = "/aelysium personagem " + (p.id().startsWith(AtributosPersonagem.PREFIXO_DEUS) ? p.id().substring(AtributosPersonagem.PREFIXO_DEUS.length()) : p.nome()) + " atributos remover " + e.atributo();
            linha.append(Component.translatable("mensagem.aelysiummod.atributos.lista_botao_remover").withStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("mensagem.aelysiummod.atributos.lista_botao_dica")))));
            fonte.sendSuccess(() -> linha, false);
        }
        return 1;
    }
}
