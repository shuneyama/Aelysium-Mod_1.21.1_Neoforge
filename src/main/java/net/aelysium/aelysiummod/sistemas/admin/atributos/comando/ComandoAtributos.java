package net.aelysium.aelysiummod.sistemas.admin.atributos.comando;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlotGroup;

import java.util.Arrays;

public class ComandoAtributos {
    private static final int PERMISSAO = 2;

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("item")
                .requires(cs -> cs.hasPermission(PERMISSAO))
                .then(preencherAtributo(Commands.literal("atributo")))
                .then(durabilidade());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> preencherAtributo(LiteralArgumentBuilder<CommandSourceStack> raiz) {
        return raiz
                .then(AlvoItem.ligar(Commands.literal("listar"), AcoesAtributo::listar))
                .then(AlvoItem.ligar(Commands.literal("limpar"), AcoesAtributo::limpar))
                .then(definir())
                .then(Commands.literal("remover")
                        .then(AlvoItem.ligar(atributoArgumento(), AcoesAtributo::remover)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> definir() {
        AlvoItem.Acao acao = AcoesAtributo::definir;
        var slot = AlvoItem.ligar(Commands.argument("slot", StringArgumentType.word())
                .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(EquipmentSlotGroup.values()).map(EquipmentSlotGroup::getSerializedName), construtor)), acao);
        var operacao = AlvoItem.executar(Commands.argument("operacao", StringArgumentType.word())
                .suggests((ctx, construtor) -> SharedSuggestionProvider.suggest(AcoesAtributo.OPERACOES.keySet(), construtor)), acao)
                .then(slot);
        var valor = AlvoItem.executar(Commands.argument("valor", DoubleArgumentType.doubleArg()), acao)
                .then(operacao);
        return Commands.literal("definir").then(atributoArgumento().then(valor));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, net.minecraft.resources.ResourceLocation> atributoArgumento() {
        return Commands.argument("atributo", ResourceLocationArgument.id())
                .suggests((ctx, construtor) -> {
                    String digitado = construtor.getRemainingLowerCase();
                    BuiltInRegistries.ATTRIBUTE.keySet().stream()
                            .map(net.minecraft.resources.ResourceLocation::toString)
                            .filter(id -> id.contains(digitado))
                            .forEach(construtor::suggest);
                    return construtor.buildFuture();
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> durabilidade() {
        return Commands.literal("durabilidade")
                .then(Commands.literal("definir")
                        .then(AlvoItem.ligar(Commands.argument("valor", IntegerArgumentType.integer(1)), AcoesDurabilidade::definir)))
                .then(AlvoItem.ligar(Commands.literal("restaurar"), AcoesDurabilidade::restaurar))
                .then(AlvoItem.ligar(Commands.literal("inquebravel"), AcoesDurabilidade::inquebravel));
    }
}
