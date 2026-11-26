package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.sistemas.social.maozinha.GerenciadorMaozinha;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class ControleRegiaoJogador {

    private static final Map<UUID, String> ultimaRegiaoJogador = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cooldownMensagem = new ConcurrentHashMap<>();
    private static final Set<UUID> voarSuprimidoPorRegiao = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, PosicaoPermitida> ultimaPosicaoPermitida = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000;
    private static final double MARGEM_EXPULSAO = 0.6;

    private record PosicaoPermitida(Vec3 posicao, ResourceLocation dimensao) {}

    static boolean podeMostrarMensagem(UUID uuid) {
        long agora = System.currentTimeMillis();
        Long ultimo = cooldownMensagem.get(uuid);
        if (ultimo == null || agora - ultimo >= COOLDOWN_MS) {
            cooldownMensagem.put(uuid, agora);
            return true;
        }
        return false;
    }

    public static boolean podeVoarAqui(ServerPlayer jogador) {
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(jogador.blockPosition(), UtilitariosEventoProtecao.dimDe(jogador));
        if (regiao == null || regiao.isDono(jogador)) return true;
        return regiao.getFlagValor(FlagRegiao.VOAR);
    }

    private static boolean verificarFronteiras(ServerPlayer jogador) {
        ResourceLocation dimensao = UtilitariosEventoProtecao.dimDe(jogador);
        BlockPos pos = jogador.blockPosition();
        PosicaoPermitida permitida = ultimaPosicaoPermitida.get(jogador.getUUID());

        Regiao regiaoAtual = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, dimensao);
        if (regiaoAtual != null && entradaProibida(jogador, regiaoAtual)) {
            boolean valida = permitida != null && permitida.dimensao().equals(dimensao)
                    && !regiaoAtual.contemPosicao(BlockPos.containing(permitida.posicao()), dimensao);
            devolver(jogador, valida ? permitida.posicao() : pontoForaDaRegiao(jogador.position(), regiaoAtual),
                    "mensagem.aelysiummod.protecao.voce_nao_pode_entrar_nesta_regiao");
            return true;
        }

        String nomeAnterior = ultimaRegiaoJogador.get(jogador.getUUID());
        Regiao regiaoAnterior = nomeAnterior != null ? GerenciadorRegioes.getInstance().getRegiao(nomeAnterior) : null;
        if (regiaoAnterior != null && regiaoAnterior.getDimensao().equals(dimensao)
                && !regiaoAnterior.contemPosicao(pos, dimensao) && saidaProibida(jogador, regiaoAnterior)) {
            boolean valida = permitida != null && permitida.dimensao().equals(dimensao)
                    && regiaoAnterior.contemPosicao(BlockPos.containing(permitida.posicao()), dimensao);
            devolver(jogador, valida ? permitida.posicao() : pontoDentroDaRegiao(jogador.position(), regiaoAnterior),
                    "mensagem.aelysiummod.protecao.voce_nao_pode_sair_desta_regiao");
            return true;
        }

        ultimaPosicaoPermitida.put(jogador.getUUID(), new PosicaoPermitida(jogador.position(), dimensao));
        return false;
    }

    private static boolean entradaProibida(ServerPlayer jogador, Regiao regiao) {
        return !regiao.getFlagValor(FlagRegiao.ENTRAR_REGIAO)
                && !regiao.isDono(jogador)
                && !regiao.getNome().equals(ultimaRegiaoJogador.get(jogador.getUUID()));
    }

    private static boolean saidaProibida(ServerPlayer jogador, Regiao regiao) {
        return !regiao.getFlagValor(FlagRegiao.SAIR_REGIAO) && !regiao.isDono(jogador);
    }

    private static void devolver(ServerPlayer jogador, Vec3 destino, String chaveMensagem) {
        if (GerenciadorMaozinha.estaSegurando(jogador.getUUID())) {
            GerenciadorMaozinha.desfazer(jogador.server, jogador.getUUID(), GerenciadorMaozinha.Motivo.ESTADO);
        }
        if (jogador.isPassenger()) jogador.stopRiding();
        jogador.teleportTo(destino.x, destino.y, destino.z);
        jogador.setDeltaMovement(Vec3.ZERO);

        if (podeMostrarMensagem(jogador.getUUID())) {
            jogador.sendSystemMessage(Component.translatable(chaveMensagem));
        }
    }

    private static Vec3 pontoDentroDaRegiao(Vec3 posicao, Regiao regiao) {
        BlockPos minimo = regiao.getPosicaoMinima();
        BlockPos maximo = regiao.getPosicaoMaxima();
        return new Vec3(
                limitar(posicao.x, minimo.getX(), maximo.getX() + 1),
                limitar(posicao.y, minimo.getY(), maximo.getY() + 1),
                limitar(posicao.z, minimo.getZ(), maximo.getZ() + 1)
        );
    }

    private static double limitar(double valor, double minimo, double maximo) {
        if (maximo - minimo <= MARGEM_EXPULSAO * 2) return (minimo + maximo) / 2.0;
        return Math.max(minimo + MARGEM_EXPULSAO, Math.min(maximo - MARGEM_EXPULSAO, valor));
    }

    private static Vec3 pontoForaDaRegiao(Vec3 posicao, Regiao regiao) {
        BlockPos minimo = regiao.getPosicaoMinima();
        BlockPos maximo = regiao.getPosicaoMaxima();
        double paraOeste = posicao.x - minimo.getX();
        double paraLeste = maximo.getX() + 1 - posicao.x;
        double paraNorte = posicao.z - minimo.getZ();
        double paraSul = maximo.getZ() + 1 - posicao.z;
        double menor = Math.min(Math.min(paraOeste, paraLeste), Math.min(paraNorte, paraSul));

        if (menor == paraOeste) return new Vec3(minimo.getX() - MARGEM_EXPULSAO, posicao.y, posicao.z);
        if (menor == paraLeste) return new Vec3(maximo.getX() + 1 + MARGEM_EXPULSAO, posicao.y, posicao.z);
        if (menor == paraNorte) return new Vec3(posicao.x, posicao.y, minimo.getZ() - MARGEM_EXPULSAO);
        return new Vec3(posicao.x, posicao.y, maximo.getZ() + 1 + MARGEM_EXPULSAO);
    }

    public static void verificarMovimentoJogador(ServerPlayer jogador) {
        if (verificarFronteiras(jogador)) return;

        BlockPos pos = jogador.blockPosition();
        Regiao regiaoAtual = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(jogador));

        if (regiaoAtual != null && !regiaoAtual.getFlagValor(FlagRegiao.FOME)) {
            jogador.getFoodData().setFoodLevel(20);
            jogador.getFoodData().setSaturation(5.0f);
        }

        if (regiaoAtual != null && !regiaoAtual.isDono(jogador)
                && !regiaoAtual.getFlagValor(FlagRegiao.VOAR)
                && !jogador.isCreative() && !jogador.isSpectator()
                && jogador.getAbilities().mayfly) {
            voarSuprimidoPorRegiao.add(jogador.getUUID());
            jogador.getAbilities().mayfly = false;
            jogador.getAbilities().flying = false;
            jogador.onUpdateAbilities();
        }

        String nomeRegiaoAtual = regiaoAtual != null ? regiaoAtual.getNome() : null;
        String ultimaRegiao = ultimaRegiaoJogador.get(jogador.getUUID());

        if (ultimaRegiao != null && nomeRegiaoAtual == null) {
            Regiao regiaoSaiu = GerenciadorRegioes.getInstance().getRegiao(ultimaRegiao);
            if (regiaoSaiu != null) aoSairRegiao(jogador, regiaoSaiu);
            ultimaRegiaoJogador.remove(jogador.getUUID());
        } else if (ultimaRegiao == null && nomeRegiaoAtual != null) {
            aoEntrarRegiao(jogador, regiaoAtual);
            ultimaRegiaoJogador.put(jogador.getUUID(), nomeRegiaoAtual);
        } else if (ultimaRegiao != null && !ultimaRegiao.equals(nomeRegiaoAtual)) {
            Regiao regiaoSaiu = GerenciadorRegioes.getInstance().getRegiao(ultimaRegiao);
            if (regiaoSaiu != null) aoSairRegiao(jogador, regiaoSaiu);
            if (regiaoAtual != null) aoEntrarRegiao(jogador, regiaoAtual);
            ultimaRegiaoJogador.put(jogador.getUUID(), nomeRegiaoAtual);
        }
    }

    private static String getNomeExibicao(ServerPlayer jogador) {
        String nick = PerfilJogador.apelido().apelido(jogador.getServer(), jogador.getUUID());
        if (nick != null && !nick.isEmpty()) return nick;
        return jogador.getName().getString();
    }

    private static void aoEntrarRegiao(ServerPlayer jogador, Regiao regiao) {
        if (!regiao.getMensagemEntrada().isEmpty()) {
            String mensagem = regiao.getMensagemEntrada()
                    .replace("{jogador}", getNomeExibicao(jogador))
                    .replace("{regiao}", regiao.getNome());
            jogador.connection.send(new ClientboundSetTitleTextPacket(CodigosEstilo.componente(mensagem)));
        }

        if (regiao.getFlagValor(FlagRegiao.AVISAR_DONO_ENTRADA)) {
            for (UUID donoUUID : regiao.getDonos()) {
                ServerPlayer dono = jogador.server.getPlayerList().getPlayer(donoUUID);
                if (dono != null && !dono.getUUID().equals(jogador.getUUID())) {
                    dono.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.entrou_na_regiao", getNomeExibicao(jogador), regiao.getNome()));
                }
            }
        }

    }

    private static void aoSairRegiao(ServerPlayer jogador, Regiao regiao) {
        if (!regiao.getMensagemSaida().isEmpty()) {
            String mensagem = regiao.getMensagemSaida()
                    .replace("{jogador}", getNomeExibicao(jogador))
                    .replace("{regiao}", regiao.getNome());
            jogador.connection.send(new ClientboundSetTitleTextPacket(CodigosEstilo.componente(mensagem)));
        }

        if (regiao.getFlagValor(FlagRegiao.AVISAR_DONO_SAIDA)) {
            for (UUID donoUUID : regiao.getDonos()) {
                ServerPlayer dono = jogador.server.getPlayerList().getPlayer(donoUUID);
                if (dono != null && !dono.getUUID().equals(jogador.getUUID())) {
                    dono.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.saiu_da_regiao", getNomeExibicao(jogador), regiao.getNome()));
                }
            }
        }

        if (voarSuprimidoPorRegiao.remove(jogador.getUUID())) {
            boolean aindaTemHabilidadeDeVoar =
                    PerfilJogador.raca().raca(jogador.getUUID()) == net.aelysium.aelysiummod.nucleo.jogador.TipoRaca.VALKYRIA
                            || PerfilJogador.habilidades().temVoo(jogador);
            if (aindaTemHabilidadeDeVoar && !jogador.isCreative() && !jogador.isSpectator() && !jogador.getAbilities().mayfly) {
                jogador.getAbilities().mayfly = true;
                jogador.onUpdateAbilities();
            }
        }
    }

    public static void liberarAposMorte(ServerPlayer jogador) {
        ResourceLocation dimensao = UtilitariosEventoProtecao.dimDe(jogador);
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(jogador.blockPosition(), dimensao);
        if (regiao != null) {
            ultimaRegiaoJogador.put(jogador.getUUID(), regiao.getNome());
        } else {
            ultimaRegiaoJogador.remove(jogador.getUUID());
        }
        ultimaPosicaoPermitida.put(jogador.getUUID(), new PosicaoPermitida(jogador.position(), dimensao));
    }

    public static void limparJogador(UUID uuid) {
        voarSuprimidoPorRegiao.remove(uuid);
        ultimaRegiaoJogador.remove(uuid);
        ultimaPosicaoPermitida.remove(uuid);
        cooldownMensagem.remove(uuid);
    }
}
