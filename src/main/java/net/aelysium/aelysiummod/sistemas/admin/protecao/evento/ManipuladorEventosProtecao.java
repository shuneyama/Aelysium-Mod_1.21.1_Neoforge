package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class ManipuladorEventosProtecao {

    @SubscribeEvent
    public void aoClicarBlocoEsquerdo(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;

        BlockPos pos = event.getPos();

        if (net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao.isAguardandoSelecaoRapida(jogador.getUUID())) {
            event.setCanceled(true);

            int etapa = net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao
                    .avancarEtapaSelecaoRapida(jogador.getUUID(), pos);

            if (etapa == 1) {
                jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.posicao_1_definida_em_quebre_outro", pos.getX(), pos.getY(), pos.getZ()));
            } else if (etapa == 2) {
                jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.posicao_2_definida_em", pos.getX(), pos.getY(), pos.getZ()));
                net.aelysium.aelysiummod.sistemas.admin.protecao.comando.ComandoProtecao.abrirTelaCriacao(jogador);
            }
            return;
        }

        if (!UtilitariosEventoProtecao.usandoVarinhaProtecao(jogador)) return;

        event.setCanceled(true);

        if (jogador.isShiftKeyDown()) {
            net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao.setPosicao2(jogador.getUUID(), pos);
            jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.posicao_2_definida_em", pos.getX(), pos.getY(), pos.getZ()));
        } else {
            net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao.setPosicao1(jogador.getUUID(), pos);
            jogador.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.protecao.posicao_1_definida_em", pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    @SubscribeEvent
    public void aoQuebrarBloco(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer jogador)) return;
        if (UtilitariosEventoProtecao.usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(jogador));
        if (regiao == null || regiao.isDono(jogador)) return;

        if (!regiao.getFlagValor(FlagRegiao.QUEBRAR_BLOCOS)) {
            event.setCanceled(true);
            if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_quebrar_blocos_nesta"));
            }
        }
    }

    @SubscribeEvent
    public void aoColocarBloco(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (UtilitariosEventoProtecao.usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(jogador));
        if (regiao == null || regiao.isDono(jogador)) return;

        if (!regiao.getFlagValor(FlagRegiao.COLOCAR_BLOCOS)) {
            event.setCanceled(true);
            if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_colocar_blocos_nesta"));
            }
        }
    }

    @SubscribeEvent
    public void aoInteragirBloco(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (UtilitariosEventoProtecao.usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(jogador));
        if (regiao == null || regiao.isDono(jogador)) return;

        boolean eContainer = jogador.level().getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider;
        boolean eMecanismo = ehMecanismo(jogador.level().getBlockState(pos));

        if (eContainer || eMecanismo) {
            FlagRegiao flagNecessaria = eContainer ? FlagRegiao.ABRIR_CONTAINERS : FlagRegiao.USAR_MECANISMOS;
            if (!regiao.getFlagValor(flagNecessaria)) {
                event.setUseBlock(TriState.FALSE);
                event.setUseItem(TriState.FALSE);
                event.setCanceled(true);
                if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                    jogador.sendSystemMessage(Component.translatable(eContainer
                            ? "mensagem.aelysiummod.protecao.sem_abrir_containers"
                            : "mensagem.aelysiummod.protecao.sem_usar_isso"));
                }
                return;
            }
        }

        if (event.getItemStack().getItem() instanceof net.minecraft.world.item.BlockItem
                && !regiao.getFlagValor(FlagRegiao.COLOCAR_BLOCOS)) {
            event.setCanceled(true);
            if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_colocar_blocos_nesta"));
            }
        }
    }

    private static boolean ehMecanismo(net.minecraft.world.level.block.state.BlockState state) {
        var b = state.getBlock();
        return b instanceof net.minecraft.world.level.block.DoorBlock
                || b instanceof net.minecraft.world.level.block.TrapDoorBlock
                || b instanceof net.minecraft.world.level.block.FenceGateBlock
                || b instanceof net.minecraft.world.level.block.ButtonBlock
                || b instanceof net.minecraft.world.level.block.LeverBlock
                || b instanceof net.minecraft.world.level.block.RepeaterBlock
                || b instanceof net.minecraft.world.level.block.ComparatorBlock
                || b instanceof net.minecraft.world.level.block.NoteBlock
                || b instanceof net.minecraft.world.level.block.CraftingTableBlock
                || b instanceof net.minecraft.world.level.block.CartographyTableBlock
                || b instanceof net.minecraft.world.level.block.SmithingTableBlock
                || b instanceof net.minecraft.world.level.block.FletchingTableBlock
                || b instanceof net.minecraft.world.level.block.LoomBlock
                || b instanceof net.minecraft.world.level.block.GrindstoneBlock
                || b instanceof net.minecraft.world.level.block.StonecutterBlock
                || b instanceof net.minecraft.world.level.block.EnchantingTableBlock
                || b instanceof net.minecraft.world.level.block.BeaconBlock
                || b instanceof net.minecraft.world.level.block.LecternBlock
                || b instanceof net.minecraft.world.level.block.RespawnAnchorBlock
                || b instanceof net.minecraft.world.level.block.CakeBlock
                || b instanceof net.minecraft.world.level.block.ComposterBlock
                || b instanceof net.minecraft.world.level.block.BellBlock
                || b instanceof net.minecraft.world.level.block.JukeboxBlock
                || b instanceof net.minecraft.world.level.block.DaylightDetectorBlock;
    }

    @SubscribeEvent
    public void aoUsarItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (UtilitariosEventoProtecao.usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = jogador.blockPosition();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(jogador));
        if (regiao == null || regiao.isDono(jogador)) return;

        boolean eComida = event.getItemStack().has(net.minecraft.core.component.DataComponents.FOOD);
        FlagRegiao flagNecessaria = eComida ? FlagRegiao.COMER : FlagRegiao.USAR_ITENS;

        if (!regiao.getFlagValor(flagNecessaria)) {
            event.setCanceled(true);
            if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.translatable(eComida
                        ? "mensagem.aelysiummod.protecao.sem_comer"
                        : "mensagem.aelysiummod.protecao.sem_usar_itens"));
            }
        }
    }
}
