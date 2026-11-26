package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ComandoEncantar {

    private static final DynamicCommandExceptionType NAO_VIVO = new DynamicCommandExceptionType(
            nome -> Component.translatableEscape("commands.enchant.failed.entity", nome));
    private static final DynamicCommandExceptionType SEM_ITEM = new DynamicCommandExceptionType(
            nome -> Component.translatableEscape("commands.enchant.failed.itemless", nome));
    private static final SimpleCommandExceptionType NADA_ACONTECEU = new SimpleCommandExceptionType(
            Component.translatable("commands.enchant.failed"));

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registrar(RegisterCommandsEvent evento) {
        CommandDispatcher<CommandSourceStack> despachante = evento.getDispatcher();
        CommandBuildContext contexto = evento.getBuildContext();
        remover(despachante.getRoot(), "enchant");
        despachante.register(Commands.literal("enchant")
                .requires(fonte -> fonte.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("enchantment", ResourceArgument.resource(contexto, Registries.ENCHANTMENT))
                                .executes(c -> encantar(c.getSource(),
                                        EntityArgument.getEntities(c, "targets"),
                                        ResourceArgument.getEnchantment(c, "enchantment"),
                                        1))
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 255))
                                        .executes(c -> encantar(c.getSource(),
                                                EntityArgument.getEntities(c, "targets"),
                                                ResourceArgument.getEnchantment(c, "enchantment"),
                                                IntegerArgumentType.getInteger(c, "level")))))));
    }

    private static void remover(CommandNode<CommandSourceStack> raiz, String nome) {
        for (String campo : new String[]{"children", "literals", "arguments"}) {
            try {
                Field f = CommandNode.class.getDeclaredField(campo);
                f.setAccessible(true);
                ((Map<?, ?>) f.get(raiz)).remove(nome);
            } catch (ReflectiveOperationException ignorado) {
            }
        }
    }

    private static int encantar(CommandSourceStack fonte, Collection<? extends Entity> alvos, Holder<Enchantment> encantamento, int nivel) throws CommandSyntaxException {
        int total = 0;
        boolean unico = alvos.size() == 1;
        for (Entity entidade : alvos) {
            if (!(entidade instanceof LivingEntity vivo)) {
                if (unico) throw NAO_VIVO.create(entidade.getName().getString());
                continue;
            }
            ItemStack item = vivo.getMainHandItem();
            if (item.isEmpty()) {
                if (unico) throw SEM_ITEM.create(vivo.getName().getString());
                continue;
            }
            if (nivel <= 0) {
                if (EnchantmentHelper.getEnchantmentsForCrafting(item).getLevel(encantamento) <= 0) continue;
                EnchantmentHelper.updateEnchantments(item, mutavel -> mutavel.set(encantamento, 0));
                total++;
                continue;
            }
            EnchantmentHelper.updateEnchantments(item, mutavel -> mutavel.set(encantamento, nivel));
            total++;
        }
        if (total == 0) throw NADA_ACONTECEU.create();
        Component nome = nivel <= 0 ? encantamento.value().description() : Enchantment.getFullname(encantamento, nivel);
        if (nivel <= 0) {
            if (unico) {
                Component alvo = alvos.iterator().next().getDisplayName();
                fonte.sendSuccess(() -> Component.literal("Removido ").append(nome).append(" de ").append(alvo), true);
            } else {
                int quantidade = total;
                fonte.sendSuccess(() -> Component.literal("Removido ").append(nome).append(" de " + quantidade + " entidades"), true);
            }
        } else if (unico) {
            fonte.sendSuccess(() -> Component.translatable("commands.enchant.success.single", nome, alvos.iterator().next().getDisplayName()), true);
        } else {
            fonte.sendSuccess(() -> Component.translatable("commands.enchant.success.multiple", nome, alvos.size()), true);
        }
        return total;
    }
}
