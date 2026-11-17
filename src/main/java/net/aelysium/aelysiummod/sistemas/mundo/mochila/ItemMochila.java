package net.aelysium.aelysiummod.sistemas.mundo.mochila;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;
import java.util.Optional;

public class ItemMochila extends BundleItem {

    private static final int COR_CHEIA = Mth.color(1.0F, 0.33F, 0.33F);
    private static final int COR_BARRA = Mth.color(0.44F, 0.53F, 1.0F);
    private static final int TICKS_APOS_PRIMEIRO = 10;
    private static final int DURACAO_MAXIMA = 200;

    public ItemMochila(Properties propriedades) {
        super(propriedades);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack propria, Slot slot, ClickAction acao, Player jogador) {
        if (propria.get(DataComponents.BUNDLE_CONTENTS) == null) return false;
        ItemStack outra = slot.getItem();
        ConteudoMochila.Editor editor = new ConteudoMochila.Editor(propria);
        if (acao == ClickAction.PRIMARY && !outra.isEmpty()) {
            if (editor.transferir(slot, jogador) > 0) {
                somInserir(jogador);
            } else {
                somFalha(jogador);
            }
            editor.aplicar(propria);
            transmitir(jogador);
            return true;
        }
        if (acao == ClickAction.SECONDARY && outra.isEmpty()) {
            ItemStack removida = editor.removerUm();
            if (removida != null) {
                ItemStack resto = slot.safeInsert(removida);
                if (resto.getCount() > 0) {
                    editor.inserir(resto);
                } else {
                    somRemover(jogador);
                }
            }
            editor.aplicar(propria);
            transmitir(jogador);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack propria, ItemStack outra, Slot slot, ClickAction acao, Player jogador, SlotAccess acesso) {
        if (acao == ClickAction.PRIMARY && outra.isEmpty()) {
            if (ConteudoMochila.selecionado(propria) != -1) propria.remove(ModMochilas.SELECIONADO.get());
            return false;
        }
        if (propria.get(DataComponents.BUNDLE_CONTENTS) == null) return false;
        ConteudoMochila.Editor editor = new ConteudoMochila.Editor(propria);
        if (acao == ClickAction.PRIMARY && !outra.isEmpty()) {
            if (slot.allowModification(jogador) && editor.inserir(outra) > 0) {
                somInserir(jogador);
            } else {
                somFalha(jogador);
            }
            editor.aplicar(propria);
            transmitir(jogador);
            return true;
        }
        if (acao == ClickAction.SECONDARY && outra.isEmpty()) {
            if (slot.allowModification(jogador)) {
                ItemStack removida = editor.removerUm();
                if (removida != null) {
                    somRemover(jogador);
                    acesso.set(removida);
                }
            }
            editor.aplicar(propria);
            transmitir(jogador);
            return true;
        }
        if (propria.has(ModMochilas.SELECIONADO.get())) propria.remove(ModMochilas.SELECIONADO.get());
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level mundo, Player jogador, InteractionHand mao) {
        ItemStack pilha = jogador.getItemInHand(mao);
        jogador.startUsingItem(mao);
        return InteractionResultHolder.consume(pilha);
    }

    @Override
    public void onUseTick(Level mundo, LivingEntity entidade, ItemStack pilha, int restante) {
        if (!(entidade instanceof Player jogador)) return;
        int duracao = getUseDuration(pilha, entidade);
        boolean primeiro = restante == duracao;
        if (primeiro || restante < duracao - TICKS_APOS_PRIMEIRO && restante % 2 == 0) {
            soltar(mundo, jogador, pilha);
        }
    }

    private void soltar(Level mundo, Player jogador, ItemStack pilha) {
        BundleContents conteudo = pilha.get(DataComponents.BUNDLE_CONTENTS);
        if (conteudo == null || conteudo.isEmpty()) return;
        ConteudoMochila.Editor editor = new ConteudoMochila.Editor(pilha);
        ItemStack removida = editor.removerUm();
        if (removida == null) return;
        editor.aplicar(pilha);
        somRemover(jogador);
        if (jogador instanceof ServerPlayer) {
            jogador.drop(removida, true);
        }
        mundo.playSound(null, jogador.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS,
                0.8F, 0.8F + mundo.getRandom().nextFloat() * 0.4F);
        jogador.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public int getUseDuration(ItemStack pilha, LivingEntity entidade) {
        return DURACAO_MAXIMA;
    }

    @Override
    public boolean isBarVisible(ItemStack pilha) {
        return pilha.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack pilha) {
        BundleContents conteudo = pilha.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate(conteudo.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack pilha) {
        BundleContents conteudo = pilha.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return conteudo.weight().compareTo(Fraction.ONE) >= 0 ? COR_CHEIA : COR_BARRA;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pilha) {
        if (pilha.has(DataComponents.HIDE_TOOLTIP) || pilha.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) return Optional.empty();
        BundleContents conteudo = pilha.get(DataComponents.BUNDLE_CONTENTS);
        if (conteudo == null) return Optional.empty();
        return Optional.of(new DicaMochila(conteudo, ConteudoMochila.selecionado(pilha)));
    }

    @Override
    public void appendHoverText(ItemStack pilha, TooltipContext contexto, List<net.minecraft.network.chat.Component> linhas, net.minecraft.world.item.TooltipFlag flag) {
    }

    private static void somRemover(Entity entidade) {
        entidade.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entidade.level().getRandom().nextFloat() * 0.4F);
    }

    private static void somInserir(Entity entidade) {
        entidade.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entidade.level().getRandom().nextFloat() * 0.4F);
    }

    private static void somFalha(Entity entidade) {
        entidade.playSound(net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.SonsAtualizados.MOCHILA_FALHA, 1.0F, 1.0F);
    }

    private static void transmitir(Player jogador) {
        if (jogador.containerMenu != null) {
            jogador.containerMenu.slotsChanged(jogador.getInventory());
        }
    }
}
