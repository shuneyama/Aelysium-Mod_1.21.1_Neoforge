package net.aelysium.aelysiummod.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;

import java.util.Optional;

public class AtributosCommand {

    public static void registrar(CommandDispatcher<CommandSourceStack> dispatcher) {

        var atributoArg = Commands.argument("atributo", ResourceLocationArgument.id())
                .suggests((ctx, builder) -> {
                    BuiltInRegistries.ATTRIBUTE.keySet()
                            .forEach(rl -> builder.suggest(rl.toString()));
                    return builder.buildFuture();
                });

        dispatcher.register(
                Commands.literal("aelysium")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.literal("atributos")

                                .then(Commands.literal("listar")
                                        .executes(ctx -> listar(ctx.getSource()))
                                )

                                .then(Commands.literal("limpar")
                                        .executes(ctx -> limpar(ctx.getSource()))
                                )

                                .then(Commands.literal("durabilidade")

                                        .then(Commands.literal("set")
                                                .then(Commands.argument("valor", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> durabSet(
                                                                ctx.getSource(),
                                                                IntegerArgumentType.getInteger(ctx, "valor")
                                                        ))
                                                )
                                        )

                                        .then(Commands.literal("adicionar")
                                                .then(Commands.argument("valor", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> durabAdicionar(
                                                                ctx.getSource(),
                                                                IntegerArgumentType.getInteger(ctx, "valor")
                                                        ))
                                                )
                                        )

                                        .then(Commands.literal("remover")
                                                .then(Commands.argument("valor", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> durabRemover(
                                                                ctx.getSource(),
                                                                IntegerArgumentType.getInteger(ctx, "valor")
                                                        ))
                                                )
                                        )

                                        .then(Commands.literal("inquebravel")
                                                .executes(ctx -> durabInquebravel(ctx.getSource()))
                                        )
                                )

                                .then(Commands.literal("modificar")
                                        .then(atributoArg
                                                .then(Commands.argument("valor", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> modificar(
                                                                ctx.getSource(),
                                                                ResourceLocationArgument.getId(ctx, "atributo"),
                                                                DoubleArgumentType.getDouble(ctx, "valor")
                                                        ))
                                                )
                                        )
                                )

                                .then(Commands.literal("remover")
                                        .then(Commands.argument("atributo", ResourceLocationArgument.id())
                                                .suggests((ctx, builder) -> {
                                                    BuiltInRegistries.ATTRIBUTE.keySet()
                                                            .forEach(rl -> builder.suggest(rl.toString()));
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> remover(
                                                        ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "atributo")
                                                ))
                                        )
                                )
                        )
        );
    }

    private static int modificar(CommandSourceStack source, ResourceLocation atributoId, double valor) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        Optional<Holder.Reference<Attribute>> attrOpt = BuiltInRegistries.ATTRIBUTE.getHolder(atributoId);
        if (attrOpt.isEmpty()) {
            source.sendFailure(Component.literal("§cAtributo não encontrado: §e" + atributoId));
            return 0;
        }

        Holder<Attribute> attrHolder = attrOpt.get();
        ResourceLocation modId = modifierId(atributoId);

        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        existing.modifiers().forEach(entry -> {
            boolean mesmaMod = entry.modifier().id().equals(modId) && entry.attribute().equals(attrHolder);
            if (!mesmaMod) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        });

        builder.add(attrHolder, new AttributeModifier(modId, valor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());

        source.sendSuccess(() -> Component.literal(
                "§aAtributo §e" + atributoId + " §adefinido: §e" + valor
        ), false);

        return 1;
    }

    private static int remover(CommandSourceStack source, ResourceLocation atributoId) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        Optional<Holder.Reference<Attribute>> attrOpt = BuiltInRegistries.ATTRIBUTE.getHolder(atributoId);
        if (attrOpt.isEmpty()) {
            source.sendFailure(Component.literal("§cAtributo não encontrado: §e" + atributoId));
            return 0;
        }

        Holder<Attribute> attrHolder = attrOpt.get();
        ResourceLocation modId = modifierId(atributoId);

        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        boolean[] removeu = {false};

        existing.modifiers().forEach(entry -> {
            if (entry.modifier().id().equals(modId) && entry.attribute().equals(attrHolder)) {
                removeu[0] = true;
            } else {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        });

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());

        if (removeu[0]) {
            source.sendSuccess(() -> Component.literal("§aAtributo §e" + atributoId + " §aremovido do item."), false);
        } else {
            source.sendFailure(Component.literal("§cEsse atributo não foi encontrado no item."));
        }

        return 1;
    }

    private static int listar(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (modifiers.modifiers().isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Este item não tem modifiers de atributo."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§6─── Atributos do item ───"), false);
        modifiers.modifiers().forEach(entry -> {
            String attrKey = BuiltInRegistries.ATTRIBUTE.getKey(entry.attribute().value()) != null
                    ? BuiltInRegistries.ATTRIBUTE.getKey(entry.attribute().value()).toString()
                    : "?";
            source.sendSuccess(() -> Component.literal(
                    "§e" + attrKey + " §7→ §a" + entry.modifier().amount() +
                            " §7(" + entry.slot().getSerializedName() + ")"
            ), false);
        });

        return 1;
    }

    private static int limpar(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        source.sendSuccess(() -> Component.literal("§aTodos os atributos do item foram removidos."), false);
        return 1;
    }

    private static int durabSet(CommandSourceStack source, int valor) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        stack.set(DataComponents.MAX_DAMAGE, valor);
        stack.set(DataComponents.DAMAGE, 0);
        source.sendSuccess(() -> Component.literal(
                "§aDurabilidade máxima definida para §e" + valor + "§a."
        ), false);
        return 1;
    }

    private static int durabAdicionar(CommandSourceStack source, int valor) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        Integer maxAtual = stack.get(DataComponents.MAX_DAMAGE);
        if (maxAtual == null) {
            source.sendFailure(Component.literal("§cEste item não possui durabilidade."));
            return 0;
        }

        int novoMax = maxAtual + valor;
        stack.set(DataComponents.MAX_DAMAGE, novoMax);
        source.sendSuccess(() -> Component.literal(
                "§aDurabilidade máxima: §e" + maxAtual + " §7→ §e" + novoMax
        ), false);
        return 1;
    }

    private static int durabRemover(CommandSourceStack source, int valor) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        Integer maxAtual = stack.get(DataComponents.MAX_DAMAGE);
        if (maxAtual == null) {
            source.sendFailure(Component.literal("§cEste item não possui durabilidade."));
            return 0;
        }

        int novoMax = Math.max(1, maxAtual - valor);
        int danoAtual = stack.getOrDefault(DataComponents.DAMAGE, 0);
        if (danoAtual >= novoMax) {
            stack.set(DataComponents.DAMAGE, novoMax - 1);
        }
        stack.set(DataComponents.MAX_DAMAGE, novoMax);
        source.sendSuccess(() -> Component.literal(
                "§aDurabilidade máxima: §e" + maxAtual + " §7→ §e" + novoMax
        ), false);
        return 1;
    }

    private static int durabInquebravel(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) return 0;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("§cSegure um item na mão principal."));
            return 0;
        }

        boolean jaInquebravel = stack.has(DataComponents.UNBREAKABLE);
        if (jaInquebravel) {
            stack.remove(DataComponents.UNBREAKABLE);
            source.sendSuccess(() -> Component.literal("§aItem voltou a ter durabilidade normal."), false);
        } else {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            source.sendSuccess(() -> Component.literal("§aItem definido como §6Indestrutível§a!"), false);
        }
        return 1;
    }

    private static ResourceLocation modifierId(ResourceLocation atributoId) {
        String path = "atributo_" + atributoId.getNamespace() + "_" + atributoId.getPath().replace("/", "_").replace(":", "_");
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, path);
    }

    private static ServerPlayer getPlayer(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cApenas jogadores podem usar este comando."));
            return null;
        }
        return player;
    }
}
