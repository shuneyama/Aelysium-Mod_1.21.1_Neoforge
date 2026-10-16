package net.aelysium.aelysiummod.sistemas.rp.deus.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.FormaDivina;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;

public class ComandoDeus {

    private static final ResourceLocation MOD_HEALTH      = rl("deus_max_health");
    private static final ResourceLocation MOD_ARMOR       = rl("deus_armor");
    private static final ResourceLocation MOD_ARMOR_TOUGH = rl("deus_armor_toughness");
    private static final ResourceLocation MOD_ATTACK      = rl("deus_attack_damage");
    private static final ResourceLocation MOD_SPELL       = rl("deus_spell_power");
    private static final ResourceLocation MOD_FLY         = rl("deus_creative_flight");

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("aelysiummod", path);
    }

    private static final SuggestionProvider<CommandSourceStack> DEUS_SUGGESTIONS =
            (ctx, builder) -> {
                for (TipoDeus d : TipoDeus.values()) {
                    if (d != TipoDeus.NONE) builder.suggest(d.id);
                }
                builder.suggest("nenhum");
                return builder.buildFuture();
            };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("deus")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("definir")
                        .then(Commands.argument("deus", StringArgumentType.string())
                                .suggests(DEUS_SUGGESTIONS)
                                .executes(ctx -> executeSet(ctx, ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("jogador", EntityArgument.player())
                                        .executes(ctx -> executeSet(ctx, EntityArgument.getPlayer(ctx, "jogador")))
                                )
                        )
                )
                .then(Commands.literal("resetar")
                        .executes(ctx -> executeReset(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> executeReset(ctx, EntityArgument.getPlayer(ctx, "jogador")))
                        )
                );
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        String deusId = StringArgumentType.getString(ctx, "deus");
        TipoDeus deus = TipoDeus.fromId(deusId);

        if (deus == TipoDeus.NONE) {
            return doReset(ctx, player);
        }

        if (FormaDivina.estaAtiva(player)) {
            FormaDivina.desativar(player);
        }

        removeDeusAttributes(player);
        applyDeusAttributes(player);
        player.setInvulnerable(true);

        DadosDeus.get(player.serverLevel()).setDeus(player.getUUID(), deus);

        String nome = player.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.deus.aelysium_deus_de_definido_como", nome, deus.displayName), false);

        return 1;
    }

    private static int executeReset(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        return doReset(ctx, player);
    }

    private static int doReset(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        if (FormaDivina.estaAtiva(player)) {
            FormaDivina.desativar(player);
        }

        removeDeusAttributes(player);
        player.setInvulnerable(false);

        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        float maxHp = (float) player.getMaxHealth();
        if (player.getHealth() > maxHp) {
            player.setHealth(maxHp);
        }

        DadosDeus.get(player.serverLevel()).setDeus(player.getUUID(), TipoDeus.NONE);

        String nomeReset = player.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.deus.aelysium_deus_de_removido", nomeReset), false);

        return 1;
    }

    private static void applyDeusAttributes(ServerPlayer player) {
        addMod(player, Attributes.MAX_HEALTH, MOD_HEALTH, 180.0);
        addMod(player, Attributes.ARMOR, MOD_ARMOR, 200.0);
        addMod(player, Attributes.ARMOR_TOUGHNESS, MOD_ARMOR_TOUGH, 200.0);
        addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 99.0);
        addModSoft(player, "irons_spellbooks:spell_power", MOD_SPELL, 0.05);
        addModSoft(player, "neoforge:creative_flight", MOD_FLY, 1.0);

        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();

        player.setHealth(player.getMaxHealth());
    }

    private static void removeDeusAttributes(ServerPlayer player) {
        removeMod(player, Attributes.MAX_HEALTH, MOD_HEALTH);
        removeMod(player, Attributes.ARMOR, MOD_ARMOR);
        removeMod(player, Attributes.ARMOR_TOUGHNESS, MOD_ARMOR_TOUGH);
        removeMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK);
        removeModSoft(player, "irons_spellbooks:spell_power", MOD_SPELL);
        removeModSoft(player, "neoforge:creative_flight", MOD_FLY);
    }

    private static void addMod(ServerPlayer player, Holder<Attribute> attribute,
                               ResourceLocation modId, double amount) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr == null) return;
        attr.removeModifier(modId);
        attr.addPermanentModifier(new AttributeModifier(modId, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void addModSoft(ServerPlayer player, String attributeId,
                                   ResourceLocation modId, double amount) {
        Holder<Attribute> holder = getHolder(attributeId);
        if (holder == null || !player.getAttributes().hasAttribute(holder)) return;
        AttributeInstance attr = player.getAttribute(holder);
        if (attr == null) return;
        attr.removeModifier(modId);
        attr.addPermanentModifier(new AttributeModifier(modId, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void removeMod(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation modId) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr != null) attr.removeModifier(modId);
    }

    private static void removeModSoft(ServerPlayer player, String attributeId, ResourceLocation modId) {
        Holder<Attribute> holder = getHolder(attributeId);
        if (holder == null || !player.getAttributes().hasAttribute(holder)) return;
        AttributeInstance attr = player.getAttribute(holder);
        if (attr != null) attr.removeModifier(modId);
    }

    private static Holder<Attribute> getHolder(String id) {
        try {
            return BuiltInRegistries.ATTRIBUTE
                    .getHolder(ResourceLocation.parse(id))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}