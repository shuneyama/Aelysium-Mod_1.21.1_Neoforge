package net.aelysium.aelysiummod.sistemas.admin.atributos.comando;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.aelysium.aelysiummod.nucleo.comando.TextoComando;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class AcoesAtributo {
    public static final Map<String, AttributeModifier.Operation> OPERACOES = new LinkedHashMap<>();

    static {
        OPERACOES.put("soma", AttributeModifier.Operation.ADD_VALUE);
        OPERACOES.put("porcentagem", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static final String PREFIXO = "atributo_";
    private static final String SEPARADOR = "__";
    private static final double FATOR_PORCENTAGEM = 0.01;

    private AcoesAtributo() {
    }

    public static int remover(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        CommandSourceStack fonte = ctx.getSource();
        ResourceLocation atributoId = ResourceLocationArgument.getId(ctx, "atributo");
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(atributoId).orElse(null);
        if (holder == null) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.atributos.atributo_nao_encontrado", ChatFormatting.RED, atributoId));
            return 0;
        }
        String base = base(atributoId);
        int removidos = reconstruir(item, entrada -> entrada.attribute().equals(holder) && ehDoAtributo(entrada.modifier(), base));
        if (removidos == 0) {
            fonte.sendFailure(Component.translatable("mensagem.aelysiummod.atributos.esse_atributo_nao_foi_encontrado_no"));
            return 0;
        }
        fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.atributos.atributo_removido_do_item", ChatFormatting.GREEN, curto(atributoId)), false);
        return 1;
    }

    public static int limpar(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        CommandSourceStack fonte = ctx.getSource();
        int removidos = reconstruir(item, entrada -> nosso(entrada.modifier()));
        if (removidos == 0) {
            fonte.sendFailure(Component.translatable("mensagem.aelysiummod.atributos.nenhum_atributo_adicionado"));
            return 0;
        }
        fonte.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.atributos.todos_os_atributos_do_item_foram"), false);
        return 1;
    }

    public static int listar(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        CommandSourceStack fonte = ctx.getSource();
        ItemAttributeModifiers modificadores = item.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modificadores.modifiers().isEmpty()) {
            fonte.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.atributos.este_item_nao_tem_modifiers_de"), false);
        } else {
            fonte.sendSuccess(() -> Component.translatable("mensagem.aelysiummod.atributos.atributos_do_item"), false);
            for (ItemAttributeModifiers.Entry entrada : modificadores.modifiers()) {
                ResourceLocation atributoId = BuiltInRegistries.ATTRIBUTE.getKey(entrada.attribute().value());
                String nome = atributoId != null ? curto(atributoId) : "?";
                AttributeModifier modificador = entrada.modifier();
                MutableComponent linha = TextoComando.traduzir(
                        "mensagem.aelysiummod.atributos.lista_linha", ChatFormatting.GRAY,
                        nome,
                        Component.literal(formatarValor(modificador.amount(), modificador.operation())).withStyle(ChatFormatting.GREEN),
                        nomeOperacao(modificador.operation()),
                        entrada.slot().getSerializedName());
                linha.append(" ");
                if (nosso(modificador) && atributoId != null) {
                    String comando = "/aelysium item atributo remover " + atributoId + " " + jogador.getGameProfile().getName();
                    linha.append(Component.translatable("mensagem.aelysiummod.atributos.lista_botao_remover").withStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("mensagem.aelysiummod.atributos.lista_botao_dica")))));
                } else {
                    linha.append(Component.translatable("mensagem.aelysiummod.atributos.lista_padrao"));
                }
                fonte.sendSuccess(() -> linha, false);
            }
        }
        int maximo = item.getMaxDamage();
        if (maximo > 0) {
            MutableComponent linha = TextoComando.traduzir(
                    "mensagem.aelysiummod.atributos.lista_durabilidade", ChatFormatting.GRAY, maximo - item.getDamageValue(), maximo);
            if (item.has(DataComponents.UNBREAKABLE)) {
                linha.append(Component.translatable("mensagem.aelysiummod.atributos.lista_inquebravel"));
            }
            fonte.sendSuccess(() -> linha, false);
        }
        return 1;
    }

    public static int definir(CommandContext<CommandSourceStack> ctx, ServerPlayer jogador, ItemStack item) {
        CommandSourceStack fonte = ctx.getSource();
        ResourceLocation atributoId = ResourceLocationArgument.getId(ctx, "atributo");
        Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(atributoId).orElse(null);
        if (holder == null) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.atributos.atributo_nao_encontrado", ChatFormatting.RED, atributoId));
            return 0;
        }
        double valor = DoubleArgumentType.getDouble(ctx, "valor");
        String nomeOperacao = texto(ctx, "operacao", "soma");
        AttributeModifier.Operation operacao = OPERACOES.get(nomeOperacao);
        if (operacao == null) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.atributos.operacao_invalida", ChatFormatting.RED, nomeOperacao));
            return 0;
        }
        String nomeSlot = texto(ctx, "slot", "mainhand");
        EquipmentSlotGroup slot = buscarSlot(nomeSlot);
        if (slot == null) {
            fonte.sendFailure(TextoComando.traduzir("mensagem.aelysiummod.atributos.slot_invalido", ChatFormatting.RED, nomeSlot));
            return 0;
        }
        ResourceLocation modId = modifierId(atributoId, nomeOperacao, slot);
        ItemAttributeModifiers atuais = item.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder construtor = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entrada : atuais.modifiers()) {
            if (!(entrada.attribute().equals(holder) && entrada.modifier().id().equals(modId))) {
                construtor.add(entrada.attribute(), entrada.modifier(), entrada.slot());
            }
        }
        double fator = operacao == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? FATOR_PORCENTAGEM : 1.0;
        double total = valor * fator;
        construtor.add(holder, new AttributeModifier(modId, total, operacao), slot);
        item.set(DataComponents.ATTRIBUTE_MODIFIERS, construtor.build());
        String formatado = formatarValor(total, operacao);
        fonte.sendSuccess(() -> TextoComando.traduzir("mensagem.aelysiummod.atributos.atributo_definido", ChatFormatting.GREEN,
                curto(atributoId), formatado, nomeOperacao, nomeSlot), false);
        return 1;
    }

    private static int reconstruir(ItemStack item, Predicate<ItemAttributeModifiers.Entry> remover) {
        ItemAttributeModifiers atuais = item.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder construtor = ItemAttributeModifiers.builder();
        int removidos = 0;
        for (ItemAttributeModifiers.Entry entrada : atuais.modifiers()) {
            if (remover.test(entrada)) {
                removidos++;
            } else {
                construtor.add(entrada.attribute(), entrada.modifier(), entrada.slot());
            }
        }
        if (removidos > 0) {
            item.set(DataComponents.ATTRIBUTE_MODIFIERS, construtor.build());
        }
        return removidos;
    }

    private static boolean nosso(AttributeModifier modificador) {
        ResourceLocation id = modificador.id();
        return id.getNamespace().equals(AelysiumMod.MOD_ID) && id.getPath().startsWith(PREFIXO);
    }

    private static boolean ehDoAtributo(AttributeModifier modificador, String base) {
        if (!modificador.id().getNamespace().equals(AelysiumMod.MOD_ID)) return false;
        String caminho = modificador.id().getPath();
        return caminho.equals(base) || caminho.startsWith(base + SEPARADOR);
    }

    private static String base(ResourceLocation atributoId) {
        return PREFIXO + atributoId.getNamespace() + "_" + atributoId.getPath().replace("/", "_").replace(":", "_");
    }

    private static ResourceLocation modifierId(ResourceLocation atributoId, String operacao, EquipmentSlotGroup slot) {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID,
                base(atributoId) + SEPARADOR + operacao + SEPARADOR + slot.getSerializedName());
    }

    private static EquipmentSlotGroup buscarSlot(String nome) {
        for (EquipmentSlotGroup grupo : EquipmentSlotGroup.values()) {
            if (grupo.getSerializedName().equals(nome)) return grupo;
        }
        return null;
    }

    private static String nomeOperacao(AttributeModifier.Operation operacao) {
        for (Map.Entry<String, AttributeModifier.Operation> entrada : OPERACOES.entrySet()) {
            if (entrada.getValue() == operacao) return entrada.getKey();
        }
        return operacao.getSerializedName();
    }

    private static String formatarValor(double valor, AttributeModifier.Operation operacao) {
        if (operacao == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(valor / FATOR_PORCENTAGEM) + "%";
        }
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(valor);
    }

    private static String texto(CommandContext<CommandSourceStack> ctx, String nome, String padrao) {
        try {
            return StringArgumentType.getString(ctx, nome);
        } catch (IllegalArgumentException e) {
            return padrao;
        }
    }

    public static String curto(ResourceLocation id) {
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
    }
}
