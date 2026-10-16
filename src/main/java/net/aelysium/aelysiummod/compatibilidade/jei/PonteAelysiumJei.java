package net.aelysium.aelysiummod.compatibilidade.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.compatibilidade.ItensVisiveisSemOp;
import net.aelysium.aelysiummod.nucleo.guarda.AelysiumGuarda;
import net.aelysium.aelysiummod.compatibilidade.jade.GerenciadorJogadoresOp;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PonteAelysiumJei {

    private static IJeiRuntime runtime;
    private static int ultimoEstado = -1;

    public static void setRuntime(IJeiRuntime novoRuntime) {
        runtime = novoRuntime;
        ultimoEstado = -1;
        aplicarEstado();
    }

    public static void limparRuntime() {
        runtime = null;
    }

    public static void atualizarSeNecessario() {
        aplicarEstado();
    }

    private static void aplicarEstado() {
        if (runtime == null) return;

        int estado = estadoAtual();
        if (estado == ultimoEstado) return;
        ultimoEstado = estado;

        List<ItemStack> visiveis = new ArrayList<>();
        List<ItemStack> ocultos = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!AelysiumMod.MOD_ID.equals(id.getNamespace())) continue;

            boolean deveMostrar = switch (estado) {
                case 2 -> true;
                case 1 -> ItensVisiveisSemOp.estaLiberado(id.getPath());
                default -> false;
            };

            (deveMostrar ? visiveis : ocultos).add(new ItemStack(item));
        }

        IIngredientManager gerenciador = runtime.getIngredientManager();
        if (!ocultos.isEmpty()) gerenciador.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ocultos);
        if (!visiveis.isEmpty()) gerenciador.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, visiveis);
    }

    private static int estadoAtual() {
        if (!AelysiumGuarda.isAutorizadoAgora()) return 0;

        Player jogador = Minecraft.getInstance().player;
        boolean op = jogador != null && GerenciadorJogadoresOp.isOp(jogador.getUUID());
        return op ? 2 : 1;
    }

    private PonteAelysiumJei() {
    }
}
