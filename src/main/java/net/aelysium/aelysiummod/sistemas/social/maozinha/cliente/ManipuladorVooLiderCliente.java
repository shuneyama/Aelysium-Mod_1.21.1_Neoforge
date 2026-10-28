package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

public class ManipuladorVooLiderCliente {

    @SubscribeEvent
    public void aoAtualizarMovimento(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer jogador)) return;
        if (!jogador.getAbilities().flying || jogador.isPassenger()) return;
        if (!CacheMaozinha.ehLider(jogador.getUUID())) return;

        Input entrada = event.getInput();
        entrada.forwardImpulse *= ConstantesMaozinha.FATOR_VOO_PUXANDO;
        entrada.leftImpulse *= ConstantesMaozinha.FATOR_VOO_PUXANDO;

        int direcaoVanilla = (entrada.jumping ? 1 : 0) - (entrada.shiftKeyDown ? 1 : 0);
        double impulsoVanilla = direcaoVanilla * jogador.getAbilities().getFlyingSpeed() * 3.0;

        double vertical;
        if (entrada.jumping && !entrada.shiftKeyDown) {
            vertical = ConstantesMaozinha.SUBIDA_VOO_PUXANDO;
        } else if (entrada.shiftKeyDown && !entrada.jumping) {
            vertical = -ConstantesMaozinha.DESCIDA_RAPIDA_VOO_PUXANDO;
        } else {
            vertical = -ConstantesMaozinha.DESCIDA_LENTA_VOO_PUXANDO;
        }

        Vec3 movimento = jogador.getDeltaMovement();
        jogador.setDeltaMovement(movimento.x, vertical - impulsoVanilla, movimento.z);
    }
}
