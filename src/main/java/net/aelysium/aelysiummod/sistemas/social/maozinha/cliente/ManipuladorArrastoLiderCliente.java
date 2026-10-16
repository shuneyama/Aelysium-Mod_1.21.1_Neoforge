package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

import java.util.UUID;

public class ManipuladorArrastoLiderCliente {

    private int ticksAbaixo = 0;
    private boolean arrastando = false;

    @SubscribeEvent
    public void aoAtualizarMovimento(MovementInputUpdateEvent event) {
        if (!arrastando || !(event.getEntity() instanceof LocalPlayer jogador)) return;

        Input entrada = event.getInput();
        entrada.forwardImpulse = 0.0F;
        entrada.leftImpulse = 0.0F;
        entrada.up = false;
        entrada.down = false;
        entrada.left = false;
        entrada.right = false;
        entrada.jumping = false;
        entrada.shiftKeyDown = false;
        jogador.setSprinting(false);
    }

    @SubscribeEvent
    public void aoTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer jogador = minecraft.player;
        if (jogador == null || minecraft.isPaused()) {
            parar();
            return;
        }

        Player seguidor = seguidorDe(jogador);
        if (seguidor == null || jogador.isSpectator() || jogador.isPassenger() || jogador.isFallFlying()) {
            parar();
            return;
        }

        if (jogador.getY() - seguidor.getY() > ConstantesMaozinha.ALTURA_QUEDA_SEGUIDOR) {
            ticksAbaixo++;
        } else {
            parar();
            return;
        }

        arrastando = ticksAbaixo >= ConstantesMaozinha.TICKS_PARA_ARRASTAR_LIDER;
        if (arrastando) arrastar(jogador, seguidor);
    }

    private static void arrastar(LocalPlayer jogador, Player seguidor) {
        Vec3 movimento = jogador.getDeltaMovement();
        double dx = seguidor.getX() - jogador.getX();
        double dz = seguidor.getZ() - jogador.getZ();
        double horizontal = Math.hypot(dx, dz);

        if (horizontal < ConstantesMaozinha.DISTANCIA_ARRASTO_MINIMA) {
            jogador.setDeltaMovement(movimento.x * ConstantesMaozinha.FATOR_ARRASTO, movimento.y, movimento.z * ConstantesMaozinha.FATOR_ARRASTO);
            return;
        }

        double velocidade = Math.min(ConstantesMaozinha.VELOCIDADE_ARRASTO, horizontal * ConstantesMaozinha.FATOR_ARRASTO);
        jogador.setDeltaMovement(dx / horizontal * velocidade, movimento.y, dz / horizontal * velocidade);
    }

    private void parar() {
        ticksAbaixo = 0;
        arrastando = false;
    }

    private static Player seguidorDe(LocalPlayer jogador) {
        UUID idSeguidor = CacheMaozinha.seguidorDe(jogador.getUUID());
        if (idSeguidor == null) return null;
        return jogador.level().getPlayerByUUID(idSeguidor);
    }
}
