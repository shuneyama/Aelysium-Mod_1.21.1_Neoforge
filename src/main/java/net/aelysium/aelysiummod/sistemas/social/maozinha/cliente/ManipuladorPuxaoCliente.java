package net.aelysium.aelysiummod.sistemas.social.maozinha.cliente;

import net.aelysium.aelysiummod.sistemas.social.maozinha.ConstantesMaozinha;
import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacoteDeslocamentoMaozinha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class ManipuladorPuxaoCliente {

    private static int ticksPuloSolicitado = 0;
    private static boolean liderVoando = false;

    private boolean pulouNesteSalto = false;
    private boolean saltoEspelhado = false;
    private int ticksNoChao = 0;
    private int ticksDesdePulo = 0;
    private int ticksBloqueioDeteccao = 0;
    private double alturaRelativaNoChao = 0.0;
    private long ultimoQuadro = 0L;

    public static void definirLiderVoando(boolean voando) {
        liderVoando = voando;
    }

    public static void solicitarPulo() {
        ticksPuloSolicitado = ConstantesMaozinha.VALIDADE_PULO_SOLICITADO_TICKS;
    }

    @SubscribeEvent
    public void aoAtualizarMovimento(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer jogador)) return;
        Player lider = liderDe(jogador);
        if (lider == null) return;

        boolean nadar = lider.isSwimming() && jogador.isInWater();

        Input entrada = event.getInput();
        entrada.forwardImpulse = nadar ? ConstantesMaozinha.IMPULSO_MINIMO_NADO : 0.0F;
        entrada.leftImpulse = 0.0F;
        entrada.up = false;
        entrada.down = false;
        entrada.left = false;
        entrada.right = false;
        entrada.jumping = false;
        entrada.shiftKeyDown = false;
        jogador.setSprinting(nadar);
    }

    @SubscribeEvent
    public void aoTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer jogador = minecraft.player;
        if (jogador == null || minecraft.isPaused()) return;

        Player lider = liderDe(jogador);
        if (lider == null || jogador.isSpectator() || jogador.isPassenger() || jogador.isFallFlying()) {
            pulouNesteSalto = false;
            saltoEspelhado = false;
            ticksNoChao = 0;
            ticksPuloSolicitado = 0;
            ticksBloqueioDeteccao = 0;
            if (lider == null) liderVoando = false;
            return;
        }

        grudar(jogador, lider);
        enviarDeslocamento(jogador, lider);
        if (ticksPuloSolicitado > 0) ticksPuloSolicitado--;
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

    private void grudar(LocalPlayer jogador, Player lider) {
        Vec3 alvo = GeometriaMaozinha.ancoraSeguidor(lider, jogador);
        Vec3 diferenca = alvo.subtract(jogador.position());
        Vec3 velocidadeLider = GeometriaMaozinha.velocidade(lider);

        double passoX = velocidadeLider.x + diferenca.x * ConstantesMaozinha.FATOR_APROXIMACAO;
        double passoZ = velocidadeLider.z + diferenca.z * ConstantesMaozinha.FATOR_APROXIMACAO;
        double limite = ConstantesMaozinha.PASSO_MAXIMO + velocidadeLider.length();
        double horizontal = Math.hypot(passoX, passoZ);
        if (horizontal > limite) {
            passoX *= limite / horizontal;
            passoZ *= limite / horizontal;
        }

        ticksDesdePulo++;
        if (ticksBloqueioDeteccao > 0) ticksBloqueioDeteccao--;

        boolean liderSubindo = lider.getY() - lider.yo > ConstantesMaozinha.SUBIDA_MINIMA_LIDER;
        if (jogador.onGround()) {
            ticksNoChao++;
            boolean assentou = ticksNoChao >= ConstantesMaozinha.TICKS_NO_CHAO_PARA_PULAR_DE_NOVO
                    || (ticksNoChao >= 2 && !liderSubindo && diferenca.y <= ConstantesMaozinha.ALTURA_PARA_PULAR_COM_LIDER);
            if (assentou) pulouNesteSalto = false;
        } else {
            ticksNoChao = 0;
        }

        boolean avisado = ticksPuloSolicitado > 0 && ticksNoChao >= 1;
        boolean detectado = ticksBloqueioDeteccao == 0 && !pulouNesteSalto
                && liderSubindo && diferenca.y > ConstantesMaozinha.ALTURA_PARA_PULAR_COM_LIDER;
        boolean sozinho = ticksBloqueioDeteccao == 0 && !pulouNesteSalto
                && !liderSubindo && diferenca.y > ConstantesMaozinha.ALTURA_PARA_PULAR_SOZINHO;

        double passoY = jogador.getDeltaMovement().y;
        if (jogador.onGround() && (avisado || detectado || sozinho)) {
            passoY = ConstantesMaozinha.IMPULSO_PULO;
            pulouNesteSalto = true;
            saltoEspelhado = avisado || detectado;
            ticksNoChao = 0;
            ticksDesdePulo = 0;
            ticksPuloSolicitado = 0;
            if (saltoEspelhado) ticksBloqueioDeteccao = ConstantesMaozinha.TICKS_BLOQUEIO_APOS_PULO;
        }

        if ((jogador.isInWater() || liderVoando) && ticksDesdePulo > 0) {
            passoY = Mth.clamp(diferenca.y * ConstantesMaozinha.FATOR_ALTURA_AGUA,
                    -ConstantesMaozinha.VELOCIDADE_VERTICAL_AGUA, ConstantesMaozinha.VELOCIDADE_VERTICAL_AGUA);
        }

        jogador.setDeltaMovement(passoX, passoY, passoZ);
        jogador.resetFallDistance();
    }

    private void enviarDeslocamento(LocalPlayer jogador, Player lider) {
        Vec3 relativo = jogador.position().subtract(GeometriaMaozinha.basePredita(lider));

        if (saltoEspelhado && (ticksDesdePulo < 2 || !jogador.onGround())) {
            relativo = new Vec3(relativo.x, alturaRelativaNoChao, relativo.z);
        } else {
            saltoEspelhado = false;
            if (jogador.onGround() && lider.onGround()) alturaRelativaNoChao = relativo.y;
        }

        PacketDistributor.sendToServer(new PacoteDeslocamentoMaozinha(jogador.getUUID(),
                (float) relativo.x, (float) relativo.y, (float) relativo.z));
    }

    private static Player liderDe(LocalPlayer jogador) {
        UUID idLider = CacheMaozinha.liderDe(jogador.getUUID());
        if (idLider == null) return null;
        return jogador.level().getPlayerByUUID(idLider);
    }
}
