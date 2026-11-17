package net.aelysium.aelysiummod.sistemas.mundo.mochila;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

import java.util.ArrayList;
import java.util.List;

public final class ConteudoMochila {

    public static final int GRADE_X = 4;
    public static final int GRADE_MAX = 12;

    private ConteudoMochila() {
    }

    public static Fraction pesoUnidade(ItemStack pilha) {
        return new BundleContents(List.of(pilha.copyWithCount(1))).weight();
    }

    public static int quantidadeMostrar(int tamanho) {
        int disponiveis = tamanho > GRADE_MAX ? GRADE_MAX - 1 : GRADE_MAX;
        int naLinha = tamanho % GRADE_X;
        int vazios = naLinha == 0 ? 0 : GRADE_X - naLinha;
        return Math.min(tamanho, disponiveis - vazios);
    }

    public static int selecionado(ItemStack mochila) {
        return mochila.getOrDefault(ModMochilas.SELECIONADO.get(), -1);
    }

    public static int quantidadeMostrar(ItemStack mochila) {
        return quantidadeMostrar(mochila.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).size());
    }

    public static void definirSelecao(ItemStack mochila, int indice) {
        Editor editor = new Editor(mochila);
        editor.definir(indice);
        editor.aplicar(mochila);
    }

    public static final class Editor {
        private final List<ItemStack> itens = new ArrayList<>();
        private Fraction peso;
        private int selecionado;

        public Editor(ItemStack mochila) {
            BundleContents conteudo = mochila.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            conteudo.itemsCopy().forEach(itens::add);
            this.peso = conteudo.weight();
            this.selecionado = selecionado(mochila);
            if (foraDosLimites(selecionado)) this.selecionado = -1;
        }

        private boolean foraDosLimites(int indice) {
            return indice < 0 || indice >= itens.size();
        }

        public void definir(int indice) {
            this.selecionado = foraDosLimites(indice) ? -1 : indice;
        }

        public void alternar(int indice) {
            this.selecionado = this.selecionado != indice && !foraDosLimites(indice) ? indice : -1;
        }

        private int procurar(ItemStack pilha) {
            if (!pilha.isStackable()) return -1;
            for (int i = 0; i < itens.size(); i++) {
                if (ItemStack.isSameItemSameComponents(itens.get(i), pilha)) return i;
            }
            return -1;
        }

        private int maximoAdicionar(ItemStack pilha) {
            Fraction restante = Fraction.ONE.subtract(peso);
            return Math.max(restante.divideBy(pesoUnidade(pilha)).intValue(), 0);
        }

        public int inserir(ItemStack pilha) {
            if (pilha.isEmpty() || !pilha.getItem().canFitInsideContainerItems()) return 0;
            int quantidade = Math.min(pilha.getCount(), maximoAdicionar(pilha));
            if (quantidade == 0) return 0;
            peso = peso.add(pesoUnidade(pilha).multiplyBy(Fraction.getFraction(quantidade, 1)));
            int indice = procurar(pilha);
            if (indice != -1) {
                ItemStack removida = itens.remove(indice);
                itens.add(0, removida.copyWithCount(removida.getCount() + quantidade));
                pilha.shrink(quantidade);
            } else {
                itens.add(0, pilha.split(quantidade));
            }
            selecionado = -1;
            return quantidade;
        }

        public int transferir(Slot slot, Player jogador) {
            ItemStack outra = slot.getItem();
            if (outra.isEmpty() || !outra.getItem().canFitInsideContainerItems()) return 0;
            int maximo = maximoAdicionar(outra);
            return inserir(slot.safeTake(outra.getCount(), maximo, jogador));
        }

        public ItemStack removerUm() {
            if (itens.isEmpty()) return null;
            int indice = foraDosLimites(selecionado) ? 0 : selecionado;
            ItemStack removida = itens.remove(indice).copy();
            peso = new BundleContents(itens).weight();
            selecionado = -1;
            return removida;
        }

        public void aplicar(ItemStack mochila) {
            mochila.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.copyOf(itens)));
            if (selecionado < 0) {
                mochila.remove(ModMochilas.SELECIONADO.get());
            } else {
                mochila.set(ModMochilas.SELECIONADO.get(), selecionado);
            }
        }
    }
}
