package net.aelysium.aelysiummod.sistemas.chefes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.florvazio.RegistroFlorVazio;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class ModChefes {

    private static final DeferredRegister<CreativeModeTab> ABAS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AelysiumMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ABA_CHEFES = ABAS.register("aelysium_chefes_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.aelysiummod.aelysium_chefes_tab"))
                    .icon(() -> RegistroFlorVazio.OVO_FLOR_DO_VAZIO.get().getDefaultInstance())
                    .displayItems((parametros, saida) -> itens().forEach(item -> saida.accept(item.get())))
                    .build());

    private ModChefes() {
    }

    private static List<DeferredItem<? extends Item>> itens() {
        List<DeferredItem<? extends Item>> itens = new ArrayList<>(RegistroFlorVazio.ITENS_ABA);
        itens.addAll(RegistroFrostmaw.ITENS_ABA);
        return itens;
    }

    public static void registrar(IEventBus barramento) {
        RegistroFlorVazio.registrar(barramento);
        RegistroFrostmaw.registrar(barramento);
        ABAS.register(barramento);
    }
}
