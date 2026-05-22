package net.aelysium.aelysiummod.item.custom;

import net.aelysium.aelysiummod.protecao.network.PacoteAbrirTela;
import net.aelysium.aelysiummod.protecao.network.PacoteListaRegioes;
import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.aelysium.aelysiummod.protecao.regiao.SelecaoManager;
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
            jogador.sendSystemMessage(Component.literal("§cVocê não tem permissão para usar este item!"));
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
            jogador.sendSystemMessage(Component.literal("§cVocê não tem permissão para usar este item!"));
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
        if (!SelecaoManager.temSelecaoCompleta(jogador.getUUID())) {
            jogador.sendSystemMessage(Component.literal("§cVocê precisa selecionar duas posições primeiro!"));
            jogador.sendSystemMessage(Component.literal("§7Botão Esquerdo§7 em bloco → §ePosição 1"));
            jogador.sendSystemMessage(Component.literal("§7Agachado + Botão Esquerdo §7em bloco → §ePosição 2"));
            return InteractionResult.FAIL;
        }
        BlockPos min = SelecaoManager.getMinimo(jogador.getUUID());
        BlockPos max = SelecaoManager.getMaximo(jogador.getUUID());
        PacketDistributor.sendToPlayer(jogador, new PacoteAbrirTela(min, max));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult abrirListaRegioes(ServerPlayer jogador) {
        GerenciadorRegioes gerenciador = GerenciadorRegioes.getInstance();
        if (gerenciador.getTodasRegioes().isEmpty()) {
            jogador.sendSystemMessage(Component.literal("§eNenhuma região protegida foi criada ainda."));
            return InteractionResult.FAIL;
        }
        List<PacoteListaRegioes.RegiaoResumo> lista = new ArrayList<>();
        for (Regiao regiao : gerenciador.getTodasRegioes()) {
            lista.add(new PacoteListaRegioes.RegiaoResumo(
                    regiao.getNome(), regiao.getPosicaoMinima(), regiao.getPosicaoMaxima(),
                    new HashMap<>(regiao.getFlags()), new ArrayList<>(regiao.getDonos()),
                    regiao.getMensagemEntrada(), regiao.getMensagemSaida()
            ));
        }
        PacketDistributor.sendToPlayer(jogador, new PacoteListaRegioes(lista));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Ferramenta de Proteção de Áreas"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§eBotão Esquerdo §7em bloco → §fPosição 1"));
        tooltip.add(Component.literal("§eAgachado + Botão Esquerdo §7em bloco → §fPosição 2"));
        tooltip.add(Component.literal("§eBotão direito §7→ §fCriar região"));
        tooltip.add(Component.literal("§eAgachado + Botão direito §7→ §fLista de regiões"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}