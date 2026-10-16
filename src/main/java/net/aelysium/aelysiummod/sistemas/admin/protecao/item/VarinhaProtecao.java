package net.aelysium.aelysiummod.sistemas.admin.protecao.item;

import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteAbrirTela;
import net.aelysium.aelysiummod.sistemas.admin.protecao.rede.PacoteListaRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorSelecao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class VarinhaProtecao extends Item {

    public VarinhaProtecao(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer jogador)) return InteractionResult.PASS;
        if (!jogador.hasPermissions(2)) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_tem_permissao_para_usar_este"));
            return InteractionResult.FAIL;
        }
        return executarBotaoDireito(jogador);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer jogador)) return InteractionResultHolder.pass(stack);
        if (!jogador.hasPermissions(2)) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_tem_permissao_para_usar_este"));
            return InteractionResultHolder.fail(stack);
        }
        InteractionResult result = executarBotaoDireito(jogador);
        return result == InteractionResult.SUCCESS
                ? InteractionResultHolder.success(stack)
                : InteractionResultHolder.fail(stack);
    }

    private InteractionResult executarBotaoDireito(ServerPlayer jogador) {
        if (jogador.isShiftKeyDown()) {
            return abrirListaRegioes(jogador);
        }
        return abrirGUICriacao(jogador);
    }

    private InteractionResult abrirGUICriacao(ServerPlayer jogador) {
        if (!GerenciadorSelecao.temSelecaoCompleta(jogador.getUUID())) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_precisa_selecionar_duas_posicoes"));
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.botao_esquerdo_em_bloco_posicao_1"));
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.agachado_botao_esquerdo_em_bloco"));
            return InteractionResult.FAIL;
        }
        BlockPos min = GerenciadorSelecao.getMinimo(jogador.getUUID());
        BlockPos max = GerenciadorSelecao.getMaximo(jogador.getUUID());
        PacketDistributor.sendToPlayer(jogador, new PacoteAbrirTela(min, max));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult abrirListaRegioes(ServerPlayer jogador) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (gerenciador.getTodasRegioes().isEmpty()) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.nenhuma_regiao_protegida_foi_criada"));
            return InteractionResult.FAIL;
        }
        List<PacoteListaRegioes.RegiaoResumo> lista = new ArrayList<>();
        for (Regiao regiao : gerenciador.getTodasRegioes()) {
            List<UUID> donos = new ArrayList<>(regiao.getDonos());
            List<String> donosNomes = new ArrayList<>();
            for (UUID uuid : donos) {
                donosNomes.add(PerfilJogador.apelido().nomeExibicao(jogador.server, uuid));
            }

            lista.add(new PacoteListaRegioes.RegiaoResumo(
                    regiao.getNome(), regiao.getPosicaoMinima(), regiao.getPosicaoMaxima(),
                    new HashMap<>(regiao.getFlags()), donos, donosNomes, regiao.isDonoOps(),
                    regiao.getMensagemEntrada(), regiao.getMensagemSaida()
            ));
        }
        PacketDistributor.sendToPlayer(jogador, new PacoteListaRegioes(lista));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.aelysiummod.protecao.ferramenta_de_protecao_de_areas"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.aelysiummod.protecao.botao_esquerdo_em_bloco_posicao_1"));
        tooltip.add(Component.translatable("tooltip.aelysiummod.protecao.agachado_botao_esquerdo_em_bloco"));
        tooltip.add(Component.translatable("tooltip.aelysiummod.protecao.botao_direito_criar_regiao"));
        tooltip.add(Component.translatable("tooltip.aelysiummod.protecao.agachado_botao_direito_lista_de"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}