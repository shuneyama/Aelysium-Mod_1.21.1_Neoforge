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
    private int ticksArrastando = 0;
    private double alturaInicialSeguidor = 0.0;
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
        if (seguidor == null || jogador.isSpectator() || jogador.isPassenger() || jogador.isFallFlying()
                || jogador.isInWater() || seguidor.isInWater() || jogador.getAbilities().flying) {
            parar();
            return;
        }

        double queda = jogador.getY() - seguidor.getY();
        boolean escalando = ticksAbaixo > 0
                && seguidor.getY() - alturaInicialSeguidor > ConstantesMaozinha.SUBIDA_PARA_CANCELAR_ARRASTO;

        if (arrastando) {
            if (queda <= ConstantesMaozinha.ALTURA_QUEDA_SEGUIDOR || escalando) {
                parar();
                return;
            }
            ticksArrastando++;
            arrastar(jogador, seguidor, ticksArrastando);
            return;
        }

        double horizontal = Math.hypot(seguidor.getX() - jogador.getX(), seguidor.getZ() - jogador.getZ());
        boolean preso = queda > ConstantesMaozinha.ALTURA_QUEDA_SEGUIDOR
                && horizontal > ConstantesMaozinha.DISTANCIA_SEGUIDOR_PRESO
                && !escalando;
        if (!preso) {
            parar();
            return;
        }

        if (ticksAbaixo == 0) alturaInicialSeguidor = seguidor.getY();
        ticksAbaixo++;
        if (ticksAbaixo >= ConstantesMaozinha.TICKS_PARA_ARRASTAR_LIDER) {
            arrastando = true;
            ticksArrastando = 0;
        }
    }

    private static void arrastar(LocalPlayer jogador, Player seguidor, int ticksArrastando) {
        Vec3 movimento = jogador.getDeltaMovement();
        double dx = seguidor.getX() - jogador.getX();
        double dz = seguidor.getZ() - jogador.getZ();
        double horizontal = Math.hypot(dx, dz);

        if (horizontal < ConstantesMaozinha.DISTANCIA_ARRASTO_MINIMA) {
            jogador.setDeltaMovement(movimento.x * ConstantesMaozinha.FATOR_ARRASTO, movimento.y, movimento.z * ConstantesMaozinha.FATOR_ARRASTO);
            return;
        }

        double rampa = Math.min(1.0, ticksArrastando / (double) ConstantesMaozinha.TICKS_RAMPA_ARRASTO);
        double velocidade = Math.min(ConstantesMaozinha.VELOCIDADE_ARRASTO, horizontal * ConstantesMaozinha.FATOR_ARRASTO) * rampa;
        jogador.setDeltaMovement(dx / horizontal * velocidade, movimento.y, dz / horizontal * velocidade);
    }

    private void parar() {
        ticksAbaixo = 0;
        ticksArrastando = 0;
        arrastando = false;
    }

    private static Player seguidorDe(LocalPlayer jogador) {
        UUID idSeguidor = CacheMaozinha.seguidorDe(jogador.getUUID());
        if (idSeguidor == null) return null;
        return jogador.level().getPlayerByUUID(idSeguidor);
    }
}
