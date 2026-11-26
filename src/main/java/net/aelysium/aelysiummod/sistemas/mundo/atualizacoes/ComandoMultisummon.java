package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ComandoMultisummon {

    private static final int QUANTIDADE_MAXIMA = 100;
    private static final int LADO_MAXIMO = 100;
    private static final double ESPACAMENTO_MAXIMO = 64.0D;

    private static final Dynamic2CommandExceptionType EXCESSO = new Dynamic2CommandExceptionType(
            (pedido, maximo) -> Component.literal("Isso invocaria " + pedido + " entidades, o máximo é " + maximo));

    private interface Gerador {
        List<Vec3> posicoes(CommandContext<CommandSourceStack> contexto, Vec3 centro) throws CommandSyntaxException;
    }

    @SubscribeEvent
    public static void registrar(RegisterCommandsEvent evento) {
        CommandBuildContext construcao = evento.getBuildContext();

        Gerador simples = (contexto, centro) -> {
            int quantidade = IntegerArgumentType.getInteger(contexto, "quantidade");
            List<Vec3> lista = new ArrayList<>(quantidade);
            for (int i = 0; i < quantidade; i++) lista.add(centro);
            return lista;
        };

        Gerador grade = (contexto, centro) -> {
            int largura = IntegerArgumentType.getInteger(contexto, "spreadx");
            int profundidade = IntegerArgumentType.getInteger(contexto, "spreadz");
            double espaco = DoubleArgumentType.getDouble(contexto, "spacing");
            if (largura * profundidade > QUANTIDADE_MAXIMA) throw EXCESSO.create(largura * profundidade, QUANTIDADE_MAXIMA);
            List<Vec3> lista = new ArrayList<>(largura * profundidade);
            for (int x = 0; x < largura; x++) {
                for (int z = 0; z < profundidade; z++) {
                    double dx = (x - (largura - 1) / 2.0D) * espaco;
                    double dz = (z - (profundidade - 1) / 2.0D) * espaco;
                    lista.add(centro.add(dx, 0.0D, dz));
                }
            }
            return lista;
        };

        evento.getDispatcher().register(Commands.literal("multisummon")
                .requires(fonte -> fonte.hasPermission(2))
                .then(Commands.literal("spread")
                        .then(Commands.argument("spreadx", IntegerArgumentType.integer(1, LADO_MAXIMO))
                                .then(Commands.argument("spreadz", IntegerArgumentType.integer(1, LADO_MAXIMO))
                                        .then(Commands.argument("spacing", DoubleArgumentType.doubleArg(0.0D, ESPACAMENTO_MAXIMO))
                                                .then(ramoEntidade(construcao, grade))))))
                .then(Commands.argument("quantidade", IntegerArgumentType.integer(1, QUANTIDADE_MAXIMA))
                        .then(ramoEntidade(construcao, simples))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> ramoEntidade(CommandBuildContext construcao, Gerador gerador) {
        return Commands.argument("entity", ResourceArgument.resource(construcao, Registries.ENTITY_TYPE))
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(c -> invocar(c, gerador, c.getSource().getPosition(), new CompoundTag(), true))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                        .executes(c -> invocar(c, gerador, Vec3Argument.getVec3(c, "pos"), new CompoundTag(), true))
                        .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                .executes(c -> invocar(c, gerador, Vec3Argument.getVec3(c, "pos"),
                                        CompoundTagArgument.getCompoundTag(c, "nbt"), false))));
    }

    private static int invocar(CommandContext<CommandSourceStack> contexto, Gerador gerador, Vec3 centro, CompoundTag nbt, boolean aleatorizar) throws CommandSyntaxException {
        CommandSourceStack fonte = contexto.getSource();
        Holder.Reference<EntityType<?>> tipo = ResourceArgument.getSummonableEntityType(contexto, "entity");
        CompoundTag semUuid = nbt.copy();
        semUuid.remove("UUID");
        List<Vec3> posicoes = gerador.posicoes(contexto, centro);
        int total = 0;
        CommandSyntaxException primeiroErro = null;
        for (Vec3 posicao : posicoes) {
            try {
                SummonCommand.createEntity(fonte, tipo, posicao, total == 0 ? nbt : semUuid, aleatorizar);
                total++;
            } catch (CommandSyntaxException e) {
                if (primeiroErro == null) primeiroErro = e;
            }
        }
        if (total == 0) {
            if (primeiroErro != null) throw primeiroErro;
            return 0;
        }
        int quantidade = total;
        Component nome = Component.translatable(tipo.value().getDescriptionId());
        fonte.sendSuccess(() -> Component.literal("Invocado(s) " + quantidade + "x ").append(nome), true);
        return total;
    }
}
