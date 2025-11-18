package net.aelysium.aelysiummod.command.racas;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class Elvarin {

    public static int aplicar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(ctx, "player");
        var cfg = Elvarin_Config.DATA;

        if (cfg == null) {
            ctx.getSource().sendFailure(Component.literal("Config não carregada."));
            return 0;
        }

        // Time
        if (cfg.team.enabled) {
            p.getServer().getCommands().performPrefixedCommand(ctx.getSource(), "team add " + cfg.team.name);
            p.getServer().getCommands().performPrefixedCommand(ctx.getSource(), "team join " + cfg.team.name + " " + p.getName().getString());
        }

        // Status
        if (cfg.status.enabled) {
            for (var a : cfg.status.list) {
                var attrib = Elvarin_Config.getAttribute(a.attribute);
                if (attrib != null && p.getAttributes().hasAttribute(attrib)) {
                    p.getAttribute(attrib).setBaseValue(a.value);
                }
            }
        }

        // Atributos
        if (cfg.attributes.enabled) {
            for (var a : cfg.attributes.list) {
                var attrib = Elvarin_Config.getAttribute(a.attribute);
                if (attrib != null && p.getAttributes().hasAttribute(attrib)) {
                    p.getAttribute(attrib).setBaseValue(a.value);
                }
            }
        }

        // Efeitos
        if (cfg.effects.enabled) {
            for (var e : cfg.effects.list) {
                var effectHolder = Elvarin_Config.getEffect(e.effect);
                if (effectHolder != null) {
                    p.addEffect(new MobEffectInstance(effectHolder, e.duration * 20, e.amplifier));
                }
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal(p.getName().getString() + " se identificou como Elvarin <3"), true);
        return 1;
    }
}
