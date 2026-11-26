package net.aelysium.aelysiummod.sistemas.social.maozinha;

import net.aelysium.aelysiummod.sistemas.social.maozinha.rede.PacoteEstadoMaozinha;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GerenciadorMaozinha {

    public enum Motivo {
        VOLUNTARIA,
        DISTANCIA,
        ESTADO,
        DESCONEXAO
    }

    public record Oferta(UUID ofertante, UUID alvo, int expiraEm) {}

    private static final Map<UUID, Oferta> ofertas = new HashMap<>();
    private static final Map<UUID, UUID> segurandoPorLider = new HashMap<>();
    private static final Map<UUID, UUID> liderPorSeguidor = new HashMap<>();
    private static final Set<UUID> teleportesPendentes = new HashSet<>();

    public static void acionar(ServerPlayer jogador, int alvoId) {
        UUID id = jogador.getUUID();

        if (estaSegurando(id)) {
            desfazer(jogador.server, id, Motivo.VOLUNTARIA);
            return;
        }

        ServerPlayer alvo = resolverAlvo(jogador, alvoId);
        ServerPlayer ofertante = resolverOfertante(jogador, alvo);
        if (ofertante != null) {
            aceitar(jogador, ofertante);
            return;
        }

        if (alvo != null) {
            oferecer(jogador, alvo);
            return;
        }

        if (ofertas.containsKey(id)) {
            cancelarOferta(jogador);
            return;
        }

        jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.sem_alvo"), true);
    }

    private static ServerPlayer resolverAlvo(ServerPlayer jogador, int alvoId) {
        if (alvoId < 0) return null;
        Entity entidade = jogador.level().getEntity(alvoId);
        if (!(entidade instanceof ServerPlayer alvo) || alvo == jogador) return null;
        if (alvo.isSpectator() || alvo.isRemoved()) return null;
        if (alvo.distanceToSqr(jogador) > ConstantesMaozinha.ALCANCE_OFERTA * ConstantesMaozinha.ALCANCE_OFERTA) return null;
        return alvo;
    }

    private static ServerPlayer resolverOfertante(ServerPlayer jogador, ServerPlayer alvo) {
        ServerPlayer escolhido = null;
        for (Oferta oferta : ofertas.values()) {
            if (!oferta.alvo().equals(jogador.getUUID())) continue;
            ServerPlayer candidato = jogador.server.getPlayerList().getPlayer(oferta.ofertante());
            if (candidato == null || !podeAceitar(jogador, candidato)) continue;
            if (alvo != null && candidato == alvo) return candidato;
            if (escolhido == null) escolhido = candidato;
        }
        return escolhido;
    }

    private static boolean podeAceitar(ServerPlayer jogador, ServerPlayer ofertante) {
        return ofertante.level() == jogador.level()
                && !ofertante.isSpectator()
                && ofertante.distanceToSqr(jogador) <= ConstantesMaozinha.ALCANCE_ACEITE * ConstantesMaozinha.ALCANCE_ACEITE;
    }

    private static void oferecer(ServerPlayer jogador, ServerPlayer alvo) {
        if (estaSegurando(alvo.getUUID())) {
            jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.alvo_ocupado", alvo.getDisplayName()), true);
            return;
        }

        Oferta anterior = ofertas.get(jogador.getUUID());
        if (anterior != null) {
            if (anterior.alvo().equals(alvo.getUUID())) return;
            cancelarOferta(jogador);
        }

        int expiraEm = jogador.server.getTickCount() + ConstantesMaozinha.VALIDADE_OFERTA_TICKS;
        ofertas.put(jogador.getUUID(), new Oferta(jogador.getUUID(), alvo.getUUID(), expiraEm));
        transmitir(jogador.getUUID(), alvo.getUUID(), FaseMaozinha.OFERTA);

        jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.oferta_enviada", alvo.getDisplayName()), true);
        avisarOferta(alvo, jogador, ConstantesMaozinha.VALIDADE_OFERTA_TICKS);

        som(jogador, SoundEvents.ALLAY_ITEM_GIVEN, 1.0F, 1.2F);
        som(alvo, SoundEvents.ALLAY_ITEM_TAKEN, 1.0F, 1.2F);
    }

    private static void avisarOferta(ServerPlayer alvo, ServerPlayer ofertante, int ticksRestantes) {
        int segundos = Math.max(1, (ticksRestantes + 19) / 20);
        alvo.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.oferta_recebida",
                ofertante.getDisplayName(), Component.keybind("key.aelysiummod.maozinha"), segundos), true);
    }

    private static void cancelarOferta(ServerPlayer jogador) {
        Oferta oferta = ofertas.remove(jogador.getUUID());
        if (oferta == null) return;
        transmitir(oferta.ofertante(), oferta.alvo(), FaseMaozinha.DESFEITA);

        jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.oferta_cancelada"), true);
        ServerPlayer alvo = jogador.server.getPlayerList().getPlayer(oferta.alvo());
        if (alvo != null) {
            alvo.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.oferta_retirada", jogador.getDisplayName()), true);
        }
    }

    private static void aceitar(ServerPlayer seguidor, ServerPlayer lider) {
        UUID idSeguidor = seguidor.getUUID();
        UUID idLider = lider.getUUID();

        if (estaSegurando(idLider)) {
            seguidor.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.alvo_ocupado", lider.getDisplayName()), true);
            retirarOfertasDe(idLider);
            return;
        }

        retirarOfertasDe(idLider);
        retirarOfertasDe(idSeguidor);
        retirarOfertasPara(idLider);
        retirarOfertasPara(idSeguidor);

        segurandoPorLider.put(idLider, idSeguidor);
        liderPorSeguidor.put(idSeguidor, idLider);
        transmitir(idLider, idSeguidor, FaseMaozinha.SEGURANDO);
        montar(seguidor, lider);

        Component tecla = Component.keybind("key.aelysiummod.maozinha");
        lider.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.aceita_lider", seguidor.getDisplayName(), tecla), true);
        seguidor.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.aceita_seguidor", lider.getDisplayName(), tecla), true);

        som(lider, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.4F);
        som(seguidor, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.4F);
    }

    private static void retirarOfertasDe(UUID ofertante) {
        Oferta oferta = ofertas.remove(ofertante);
        if (oferta != null) transmitir(oferta.ofertante(), oferta.alvo(), FaseMaozinha.DESFEITA);
    }

    private static void retirarOfertasPara(UUID alvo) {
        Iterator<Oferta> iterador = ofertas.values().iterator();
        while (iterador.hasNext()) {
            Oferta oferta = iterador.next();
            if (!oferta.alvo().equals(alvo)) continue;
            iterador.remove();
            transmitir(oferta.ofertante(), oferta.alvo(), FaseMaozinha.DESFEITA);
        }
    }

    public static void desfazer(MinecraftServer servidor, UUID participante, Motivo motivo) {
        UUID lider = segurandoPorLider.containsKey(participante) ? participante : liderPorSeguidor.get(participante);
        if (lider == null) return;
        UUID seguidor = segurandoPorLider.remove(lider);
        if (seguidor == null) return;
        liderPorSeguidor.remove(seguidor);

        ServerPlayer jogadorLider = servidor.getPlayerList().getPlayer(lider);
        ServerPlayer jogadorSeguidor = servidor.getPlayerList().getPlayer(seguidor);
        if (jogadorSeguidor != null && jogadorLider != null && jogadorSeguidor.getVehicle() == jogadorLider) {
            FormacaoMaozinha.marcarSoltura(seguidor);
            jogadorSeguidor.stopRiding();
            FormacaoMaozinha.consumirSoltura(seguidor);
            avisarVeiculo(jogadorLider);
        }
        transmitir(lider, seguidor, FaseMaozinha.DESFEITA);
        notificarSoltura(jogadorLider, jogadorSeguidor, participante.equals(lider), motivo);
        notificarSoltura(jogadorSeguidor, jogadorLider, participante.equals(seguidor), motivo);

        if (jogadorLider != null) som(jogadorLider, SoundEvents.WOOL_BREAK, 0.8F, 1.1F);
        if (jogadorSeguidor != null) som(jogadorSeguidor, SoundEvents.WOOL_BREAK, 0.8F, 1.1F);
    }

    private static void notificarSoltura(ServerPlayer destinatario, ServerPlayer outro, boolean foiOAutor, Motivo motivo) {
        if (destinatario == null) return;
        Component nome = outro != null ? outro.getDisplayName() : Component.translatable("mensagem.aelysiummod.maozinha.alguem");
        String chave = switch (motivo) {
            case VOLUNTARIA -> foiOAutor ? "soltou_voce" : "soltou_outro";
            case DISTANCIA -> "escapou_distancia";
            case ESTADO -> "interrompida";
            case DESCONEXAO -> "soltou_outro";
        };
        destinatario.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha." + chave, nome), true);
    }

    public static void limparJogador(ServerPlayer jogador, Motivo motivo) {
        UUID id = jogador.getUUID();
        desfazer(jogador.server, id, motivo);
        retirarOfertasDe(id);
        retirarOfertasPara(id);
    }

    public static void limparTudo() {
        ofertas.clear();
        segurandoPorLider.clear();
        liderPorSeguidor.clear();
        teleportesPendentes.clear();
        FormacaoMaozinha.limparServidor();
    }

    public static void sincronizar(ServerPlayer jogador) {
        for (Oferta oferta : ofertas.values()) {
            PacketDistributor.sendToPlayer(jogador, new PacoteEstadoMaozinha(oferta.ofertante(), oferta.alvo(), FaseMaozinha.OFERTA));
        }
        for (Map.Entry<UUID, UUID> par : segurandoPorLider.entrySet()) {
            PacketDistributor.sendToPlayer(jogador, new PacoteEstadoMaozinha(par.getKey(), par.getValue(), FaseMaozinha.SEGURANDO));
        }
    }

    public static ServerPlayer parceiroDe(ServerPlayer jogador) {
        UUID id = jogador.getUUID();
        UUID parceiro = segurandoPorLider.containsKey(id) ? segurandoPorLider.get(id) : liderPorSeguidor.get(id);
        return parceiro != null ? jogador.server.getPlayerList().getPlayer(parceiro) : null;
    }

    public static UUID liderDe(UUID seguidor) {
        return liderPorSeguidor.get(seguidor);
    }

    public static boolean estaSegurando(UUID id) {
        return segurandoPorLider.containsKey(id) || liderPorSeguidor.containsKey(id);
    }

    public static void marcarTeleporteComando(ServerPlayer jogador) {
        if (estaSegurando(jogador.getUUID())) teleportesPendentes.add(jogador.getUUID());
    }

    private static void levarParceirosTeleportados(MinecraftServer servidor) {
        if (teleportesPendentes.isEmpty()) return;
        List<UUID> pendentes = new ArrayList<>(teleportesPendentes);
        teleportesPendentes.clear();

        for (UUID id : pendentes) {
            ServerPlayer jogador = servidor.getPlayerList().getPlayer(id);
            if (jogador == null || !jogador.isAlive()) continue;
            ServerPlayer parceiro = parceiroDe(jogador);
            if (parceiro == null || !parceiro.isAlive()) continue;

            parceiro.teleportTo(jogador.serverLevel(), jogador.getX(), jogador.getY(), jogador.getZ(),
                    parceiro.getYRot(), parceiro.getXRot());
        }
    }

    public static void tick(MinecraftServer servidor) {
        levarParceirosTeleportados(servidor);
        if (ofertas.isEmpty() && segurandoPorLider.isEmpty()) return;
        int agora = servidor.getTickCount();

        expirarOfertas(servidor, agora);
        lembrarOfertas(servidor, agora);
        validarLacos(servidor);
    }

    private static boolean virouDeCostas(ServerPlayer ofertante, ServerPlayer alvo) {
        double dx = alvo.getX() - ofertante.getX();
        double dz = alvo.getZ() - ofertante.getZ();
        if (dx * dx + dz * dz < 1.0E-4) return false;
        float direcao = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
        return Math.abs(Mth.wrapDegrees(direcao - ofertante.getYRot())) > ConstantesMaozinha.LIMITE_ANGULO_OFERTA_GRAUS;
    }

    private static void lembrarOfertas(MinecraftServer servidor, int agora) {
        for (Oferta oferta : ofertas.values()) {
            int restante = oferta.expiraEm() - agora;
            if (restante % ConstantesMaozinha.INTERVALO_LEMBRETE_OFERTA_TICKS != 0) continue;
            ServerPlayer ofertante = servidor.getPlayerList().getPlayer(oferta.ofertante());
            ServerPlayer alvo = servidor.getPlayerList().getPlayer(oferta.alvo());
            if (ofertante != null && alvo != null) avisarOferta(alvo, ofertante, restante);
        }
    }

    private static void expirarOfertas(MinecraftServer servidor, int agora) {
        List<Oferta> vencidas = new ArrayList<>();
        List<ServerPlayer> deCostas = new ArrayList<>();
        for (Oferta oferta : ofertas.values()) {
            ServerPlayer ofertante = servidor.getPlayerList().getPlayer(oferta.ofertante());
            ServerPlayer alvo = servidor.getPlayerList().getPlayer(oferta.alvo());
            boolean invalida = ofertante == null || alvo == null
                    || ofertante.level() != alvo.level()
                    || ofertante.distanceToSqr(alvo) > ConstantesMaozinha.ALCANCE_ACEITE * ConstantesMaozinha.ALCANCE_ACEITE;
            if (invalida || agora >= oferta.expiraEm()) {
                vencidas.add(oferta);
            } else if (virouDeCostas(ofertante, alvo)) {
                deCostas.add(ofertante);
            }
        }

        for (ServerPlayer ofertante : deCostas) {
            cancelarOferta(ofertante);
        }

        for (Oferta oferta : vencidas) {
            ofertas.remove(oferta.ofertante());
            transmitir(oferta.ofertante(), oferta.alvo(), FaseMaozinha.DESFEITA);
            ServerPlayer ofertante = servidor.getPlayerList().getPlayer(oferta.ofertante());
            ServerPlayer alvo = servidor.getPlayerList().getPlayer(oferta.alvo());
            if (ofertante != null) {
                Component nome = alvo != null ? alvo.getDisplayName() : Component.translatable("mensagem.aelysiummod.maozinha.alguem");
                ofertante.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.oferta_expirou", nome), true);
            }
            if (alvo != null && ofertante != null) {
                alvo.displayClientMessage(Component.translatable("mensagem.aelysiummod.maozinha.oferta_perdida", ofertante.getDisplayName()), true);
            }
        }
    }

    private static void validarLacos(MinecraftServer servidor) {
        List<UUID> lideres = new ArrayList<>(segurandoPorLider.keySet());
        for (UUID idLider : lideres) {
            UUID idSeguidor = segurandoPorLider.get(idLider);
            if (idSeguidor == null) continue;

            ServerPlayer lider = servidor.getPlayerList().getPlayer(idLider);
            ServerPlayer seguidor = servidor.getPlayerList().getPlayer(idSeguidor);
            if (lider == null || seguidor == null) {
                desfazer(servidor, idLider, Motivo.DESCONEXAO);
                continue;
            }

            if (!podeContinuar(lider) || !podeContinuar(seguidor) || lider.level() != seguidor.level()) {
                desfazer(servidor, idLider, Motivo.ESTADO);
                continue;
            }

            double maxima = ConstantesMaozinha.DISTANCIA_MAXIMA;
            if (lider.distanceToSqr(seguidor) > maxima * maxima) {
                desfazer(servidor, idLider, Motivo.DISTANCIA);
                continue;
            }

            if (lider.isPassenger() || (seguidor.isPassenger() && seguidor.getVehicle() != lider)) {
                desfazer(servidor, idLider, Motivo.ESTADO);
                continue;
            }

            if (seguidor.getVehicle() != lider && !montar(seguidor, lider)) {
                desfazer(servidor, idLider, Motivo.ESTADO);
                continue;
            }

            seguidor.resetFallDistance();
        }
    }

    private static boolean montar(ServerPlayer seguidor, ServerPlayer lider) {
        if (!seguidor.startRiding(lider, true)) return false;
        avisarVeiculo(lider);
        return true;
    }

    private static void avisarVeiculo(ServerPlayer lider) {
        lider.connection.send(new ClientboundSetPassengersPacket(lider));
    }

    private static boolean podeContinuar(ServerPlayer jogador) {
        return jogador.isAlive()
                && !jogador.isSpectator()
                && !jogador.isSleeping()
                && !jogador.isFallFlying()
                && !jogador.hasDisconnected();
    }

    private static void transmitir(UUID lider, UUID alvo, FaseMaozinha fase) {
        PacketDistributor.sendToAllPlayers(new PacoteEstadoMaozinha(lider, alvo, fase));
    }

    private static void som(ServerPlayer jogador, SoundEvent som, float volume, float tom) {
        jogador.level().playSound(null, jogador.getX(), jogador.getY(), jogador.getZ(), som, SoundSource.PLAYERS, volume, tom);
    }
}
