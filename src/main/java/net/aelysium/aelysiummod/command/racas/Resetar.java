package net.aelysium.aelysiummod.command.racas;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Resetar {

    public static int aplicar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
        String player = p.getName().getString();

        p.removeAllEffects();

        p.getAbilities().mayfly = false;
        p.onUpdateAbilities();

        String[] comandos = {
                "team leave " + player,
                "attribute " + player + " apothic_attributes:experience_gained base set 1",
                "attribute " + player + " irons_spellbooks:fire_magic_resist base set 1",
                "attribute " + player + " irons_spellbooks:ice_magic_resist base set 1",
                "attribute " + player + " irons_spellbooks:lightning_magic_resist base set 1",
                "attribute " + player + " irons_spellbooks:mana_regen base set 1",
                "attribute " + player + " irons_spellbooks:nature_magic_resist base set 1",
                "attribute " + player + " irons_spellbooks:spell_power base set 1",
                "attribute " + player + " minecraft:generic.armor base set 0",
                "attribute " + player + " minecraft:generic.armor_toughness base set 0",
                "attribute " + player + " minecraft:generic.attack_damage base set 1",
                "attribute " + player + " minecraft:generic.attack_speed base set 4",
                "attribute " + player + " minecraft:generic.burning_time base set 1",
                "attribute " + player + " minecraft:generic.fall_damage_multiplier base set 1",
                "attribute " + player + " minecraft:generic.luck base set 0",
                "attribute " + player + " minecraft:generic.max_health base set 20",
                "attribute " + player + " minecraft:generic.movement_speed base set 0.1",
                "attribute " + player + " minecraft:player.submerged_mining_speed base set 0.2",
                "attribute " + player + " neoforge:swim_speed base set 1"
        };

        for (String cmd : comandos) {
            p.getServer().getCommands().performPrefixedCommand(ctx.getSource(), cmd);
        }

        ctx.getSource().sendSuccess(() ->
                Component.literal(p.getName().getString() + " perdeu sua origem :c"), true);

        return 1;
    }
}
