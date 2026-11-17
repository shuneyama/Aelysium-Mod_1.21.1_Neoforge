package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ComandoRotacionar {

    @SubscribeEvent
    public static void registrar(RegisterCommandsEvent evento) {
        CommandDispatcher<CommandSourceStack> despachante = evento.getDispatcher();
        despachante.register(Commands.literal("rotate")
                .requires(fonte -> fonte.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.entities())
                        .then(Commands.argument("rotation", RotationArgument.rotation())
                                .executes(ctx -> girar(ctx)))
                        .then(Commands.literal("facing")
                                .then(Commands.literal("entity")
                                        .then(Commands.argument("facingEntity", EntityArgument.entity())
                                                .executes(ctx -> olharEntidade(ctx, EntityAnchorArgument.Anchor.FEET))
                                                .then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor())
                                                        .executes(ctx -> olharEntidade(ctx, EntityAnchorArgument.getAnchor(ctx, "facingAnchor"))))))
                                .then(Commands.argument("facingLocation", Vec3Argument.vec3())
                                        .executes(ctx -> olharLocal(ctx))))));
    }

    private static int girar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> alvos = EntityArgument.getEntities(ctx, "target");
        var rotacao = RotationArgument.getRotation(ctx, "rotation");
        for (Entity alvo : alvos) {
            Vec2 resultado = rotacao.getRotation(ctx.getSource().withRotation(alvo.getRotationVector()));
            aplicar(alvo, resultado.y, resultado.x);
        }
        return resposta(ctx.getSource(), alvos.size());
    }

    private static int olharLocal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> alvos = EntityArgument.getEntities(ctx, "target");
        Vec3 local = Vec3Argument.getVec3(ctx, "facingLocation");
        for (Entity alvo : alvos) {
            alvo.lookAt(EntityAnchorArgument.Anchor.EYES, local);
            sincronizar(alvo);
        }
        return resposta(ctx.getSource(), alvos.size());
    }

    private static int olharEntidade(CommandContext<CommandSourceStack> ctx, EntityAnchorArgument.Anchor ancora) throws CommandSyntaxException {
        Collection<? extends Entity> alvos = EntityArgument.getEntities(ctx, "target");
        Entity foco = EntityArgument.getEntity(ctx, "facingEntity");
        for (Entity alvo : alvos) {
            alvo.lookAt(EntityAnchorArgument.Anchor.EYES, ancora.apply(foco));
            sincronizar(alvo);
        }
        return resposta(ctx.getSource(), alvos.size());
    }

    private static void aplicar(Entity alvo, float yaw, float pitch) {
        alvo.setYRot(yaw);
        alvo.setXRot(pitch);
        if (alvo instanceof LivingEntity vivo) {
            vivo.setYHeadRot(yaw);
            vivo.setYBodyRot(yaw);
        }
        sincronizar(alvo);
    }

    private static void sincronizar(Entity alvo) {
        if (alvo instanceof ServerPlayer jogador) {
            jogador.connection.teleport(jogador.getX(), jogador.getY(), jogador.getZ(), jogador.getYRot(), jogador.getXRot());
        }
    }

    private static int resposta(CommandSourceStack fonte, int total) {
        fonte.sendSuccess(() -> Component.translatable("commands.aelysiummod.rotate.success", total), true);
        return total;
    }
}
