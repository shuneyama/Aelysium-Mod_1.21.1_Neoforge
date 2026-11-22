package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ComandoAtributo {

    private static final Dynamic2CommandExceptionType SEM_ATRIBUTO = new Dynamic2CommandExceptionType(
            (alvo, atributo) -> Component.translatableEscape("commands.attribute.failed.no_attribute", alvo, atributo));

    private static final SuggestionProvider<CommandSourceStack> SUGESTOES = (contexto, construtor) -> {
        Entity alvo = EntityArgument.getEntity(contexto, "target");
        Holder<Attribute> atributo = ResourceArgument.getAttribute(contexto, "attribute");
        if (alvo instanceof LivingEntity vivo) {
            AttributeInstance instancia = vivo.getAttributes().getInstance(atributo);
            if (instancia != null) {
                return SharedSuggestionProvider.suggestResource(instancia.getModifiers().stream().map(AttributeModifier::id), construtor);
            }
        }
        return construtor.buildFuture();
    };

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registrar(RegisterCommandsEvent evento) {
        CommandDispatcher<CommandSourceStack> despachante = evento.getDispatcher();
        CommandNode<CommandSourceStack> raiz = despachante.getRoot().getChild("attribute");
        if (raiz == null) return;
        CommandNode<CommandSourceStack> alvo = raiz.getChild("target");
        if (alvo == null) return;
        if (alvo.getChild("lista") == null) {
            alvo.addChild(Commands.literal("lista")
                    .requires(fonte -> fonte.hasPermission(2))
                    .executes(contexto -> listar(contexto.getSource(), EntityArgument.getEntity(contexto, "target")))
                    .build());
        }
        CommandNode<CommandSourceStack> atributo = alvo.getChild("attribute");
        if (atributo == null) return;
        CommandNode<CommandSourceStack> base = atributo.getChild("base");
        if (base != null && base.getChild("reset") == null) {
            base.addChild(Commands.literal("reset").executes(contexto -> redefinir(contexto.getSource(),
                    EntityArgument.getEntity(contexto, "target"),
                    ResourceArgument.getAttribute(contexto, "attribute"))).build());
        }
        CommandNode<CommandSourceStack> modificador = atributo.getChild("modifier");
        if (modificador == null) return;
        sugerir(modificador, "remove");
        CommandNode<CommandSourceStack> valor = modificador.getChild("value");
        if (valor != null) sugerir(valor, "get");
    }

    private static void sugerir(CommandNode<CommandSourceStack> pai, String filho) {
        CommandNode<CommandSourceStack> no = pai.getChild(filho);
        if (no == null) return;
        CommandNode<CommandSourceStack> id = no.getChild("id");
        if (!(id instanceof ArgumentCommandNode<?, ?> argumento)) return;
        try {
            Field campo = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            campo.setAccessible(true);
            campo.set(argumento, SUGESTOES);
        } catch (ReflectiveOperationException ignorado) {
        }
    }

    @SuppressWarnings("unchecked")
    private static int redefinir(CommandSourceStack fonte, Entity alvo, Holder<Attribute> atributo) throws CommandSyntaxException {
        if (!(alvo instanceof LivingEntity vivo)) {
            throw SEM_ATRIBUTO.create(alvo.getName(), Component.translatable(atributo.value().getDescriptionId()));
        }
        EntityType<? extends LivingEntity> tipo = (EntityType<? extends LivingEntity>) vivo.getType();
        AttributeInstance instancia = vivo.getAttributes().getInstance(atributo);
        if (instancia == null || !DefaultAttributes.hasSupplier(tipo)) {
            throw SEM_ATRIBUTO.create(alvo.getName(), Component.translatable(atributo.value().getDescriptionId()));
        }
        AttributeSupplier fornecedor = DefaultAttributes.getSupplier(tipo);
        if (!fornecedor.hasAttribute(atributo)) {
            throw SEM_ATRIBUTO.create(alvo.getName(), Component.translatable(atributo.value().getDescriptionId()));
        }
        instancia.setBaseValue(fornecedor.getBaseValue(atributo));
        double valor = vivo.getAttributeBaseValue(atributo);
        fonte.sendSuccess(() -> Component.translatable("commands.attribute.base_value.reset.success",
                Component.translatable(atributo.value().getDescriptionId()), alvo.getName(), valor), false);
        return 1;
    }

    private static String numero(double valor) {
        return BigDecimal.valueOf(valor).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static String descreverModificador(AttributeModifier modificador) {
        double valor = modificador.amount();
        return switch (modificador.operation()) {
            case ADD_VALUE -> (valor >= 0 ? "+" : "") + numero(valor);
            case ADD_MULTIPLIED_BASE -> (valor >= 0 ? "+" : "") + numero(valor * 100.0D) + "% (base)";
            case ADD_MULTIPLIED_TOTAL -> (valor >= 0 ? "+" : "") + numero(valor * 100.0D) + "% (total)";
        };
    }

    @SuppressWarnings("unchecked")
    private static int listar(CommandSourceStack fonte, Entity alvo) {
        if (!(alvo instanceof LivingEntity vivo)) {
            fonte.sendFailure(Component.literal("§cEssa entidade não possui atributos."));
            return 0;
        }
        EntityType<? extends LivingEntity> tipo = (EntityType<? extends LivingEntity>) vivo.getType();
        AttributeSupplier padrao = DefaultAttributes.hasSupplier(tipo) ? DefaultAttributes.getSupplier(tipo) : null;
        List<Component> linhas = new ArrayList<>();
        int total = 0;
        for (Holder.Reference<Attribute> referencia : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
            AttributeInstance instancia = vivo.getAttributes().getInstance(referencia);
            if (instancia == null) continue;
            Double original = padrao != null && padrao.hasAttribute(referencia) ? padrao.getBaseValue(referencia) : null;
            double base = instancia.getBaseValue();
            boolean baseMudou = original != null && Math.abs(original - base) > 1.0E-9D;
            boolean temModificadores = !instancia.getModifiers().isEmpty();
            if (!baseMudou && !temModificadores) continue;
            total++;
            Component nome = Component.translatable(referencia.value().getDescriptionId());
            linhas.add(Component.literal("§e").append(nome).append(Component.literal(" §8(" + referencia.key().location() + ")")));
            String textoOriginal = original != null ? numero(original) : "?";
            linhas.add(Component.literal("  §7Original: §f" + textoOriginal + " §7| Base: §f" + numero(base) + " §7| Atual: §a" + numero(instancia.getValue())));
            for (AttributeModifier modificador : instancia.getModifiers()) {
                linhas.add(Component.literal("  §7- §b" + modificador.id() + " §f" + descreverModificador(modificador)));
            }
        }
        if (total == 0) {
            fonte.sendSuccess(() -> Component.literal("§aNenhum atributo modificado em §f").append(alvo.getName()), false);
            return 0;
        }
        int quantidade = total;
        fonte.sendSuccess(() -> Component.literal("§6" + quantidade + " atributo(s) modificado(s) em §f").append(alvo.getName()).append(Component.literal("§6:")), false);
        for (Component linha : linhas) {
            fonte.sendSuccess(() -> linha, false);
        }
        return total;
    }
}
