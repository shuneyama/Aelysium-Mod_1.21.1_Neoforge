package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

public class ManipuladorEventosAmbienteProtecao {

    @SubscribeEvent
    public void aoPisotearCultura(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel().isClientSide()) return;

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(event.getPos(), UtilitariosEventoProtecao.dimDe(event.getLevel()));
        if (regiao == null) return;

        if (!regiao.getFlagValor(FlagRegiao.PISOTEAR_CULTURA)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void aoMobGriefar(EntityMobGriefingEvent event) {
        if (!event.canGrief()) return;
        if (event.getEntity().level().isClientSide()) return;

        Regiao regiao = GerenciadorRegioes.getInstance()
                .getRegiaoMaisEspecifica(event.getEntity().blockPosition(), UtilitariosEventoProtecao.dimDe(event.getEntity()));
        if (regiao == null) return;

        if (!regiao.getFlagValor(FlagRegiao.MOB_GRIEFING)) {
            event.setCanGrief(false);
        }
    }

    @SubscribeEvent
    public void aoFogoEspalhar(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockState(pos).getBlock() instanceof FireBlock)) return;

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(event.getLevel()));
        if (regiao != null && !regiao.getFlagValor(FlagRegiao.FOGO_ESPALHAR)) {
            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void aoTeleportar(EntityTeleportEvent event) {
        BlockPos posDestino = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(posDestino, UtilitariosEventoProtecao.dimDe(event.getEntity()));
        if (regiao == null) return;

        if (event.getEntity() instanceof ServerPlayer jogador) {
            if (regiao.isDono(jogador)) return;
            if (!regiao.getFlagValor(FlagRegiao.TELEPORTE)) {
                event.setCanceled(true);
                if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                    jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_teleportar_para_esta"));
                }
            }
        } else {
            if (event.getEntity().level().isClientSide) return;
            if (!regiao.getFlagValor(FlagRegiao.TELEPORTE)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void aoCrescerBloco(CropGrowEvent.Pre event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(event.getLevel()));
        if (regiao == null) return;

        if (!regiao.getFlagValor(FlagRegiao.CRESCIMENTO)) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }

    @SubscribeEvent
    public void aoUsarOssoBranco(BonemealEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer jogador)) return;

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(event.getPos(), UtilitariosEventoProtecao.dimDe(event.getLevel()));
        if (regiao == null || regiao.isDono(jogador)) return;

        if (!regiao.getFlagValor(FlagRegiao.CRESCIMENTO)) {
            event.setSuccessful(false);
            if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_usar_osso_branco_nesta"));
            }
        }
    }

    @SubscribeEvent
    public void aoBrotarMuda(BlockGrowFeatureEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(event.getLevel()));
        if (regiao == null) return;

        if (!regiao.getFlagValor(FlagRegiao.CRESCIMENTO)) {
            event.setCanceled(true);
        }
    }
}
