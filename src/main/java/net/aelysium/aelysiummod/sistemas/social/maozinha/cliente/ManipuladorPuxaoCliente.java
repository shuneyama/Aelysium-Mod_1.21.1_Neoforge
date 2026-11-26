package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.UUID;

public class ManipuladorPuxaoCliente {

    private long ultimoQuadro = 0L;

    @SubscribeEvent
    public void aoAtualizarMovimento(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer jogador)) return;
        if (liderDe(jogador) == null) return;

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
    public void antesDoQuadro(RenderFrameEvent.Pre event) {
        long agora = System.nanoTime();
        double segundos = ultimoQuadro == 0L ? 0.0 : Math.min((agora - ultimoQuadro) / 1.0E9, 0.25);
        ultimoQuadro = agora;

        LocalPlayer jogador = Minecraft.getInstance().player;
        if (jogador == null) return;
        Player lider = liderDe(jogador);
        if (lider == null) return;

        float parcial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float frente = OrientacaoLiderMaozinha.yaw(lider, parcial);
        float relativo = Mth.wrapDegrees(jogador.getYRot() - frente);
        float limite = ConstantesMaozinha.LIMITE_VISAO_GRAUS;
        if (Math.abs(relativo) <= limite) return;

        float excesso = relativo - Math.signum(relativo) * limite;
        float novoExcesso = (float) (excesso * Math.exp(-ConstantesMaozinha.RETORNO_VISAO_POR_SEGUNDO * segundos));
        novoExcesso = Mth.clamp(novoExcesso, -ConstantesMaozinha.EXCESSO_MAXIMO_VISAO_GRAUS, ConstantesMaozinha.EXCESSO_MAXIMO_VISAO_GRAUS);
        float ajuste = novoExcesso - excesso;

        jogador.setYRot(jogador.getYRot() + ajuste);
        jogador.yRotO += ajuste;
        jogador.setYHeadRot(jogador.getYHeadRot() + ajuste);
        jogador.yHeadRotO += ajuste;
    }

    private static Player liderDe(LocalPlayer jogador) {
        UUID idLider = CacheMaozinha.liderDe(jogador.getUUID());
        if (idLider == null) return null;
        return jogador.level().getPlayerByUUID(idLider);
    }
}
