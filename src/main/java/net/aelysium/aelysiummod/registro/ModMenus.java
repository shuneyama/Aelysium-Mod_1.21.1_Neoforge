package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.aelysium.aelysiummod.conteudo.item.carteira.CarteiraMenu;
import net.aelysium.aelysiummod.sistemas.social.npc.MenuMercadorNpc;
import net.aelysium.aelysiummod.sistemas.admin.vip.MenuSacolaVip;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AelysiumMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CarteiraMenu>> CARTEIRA_MENU =
            MENUS.register("carteira", () -> new MenuType<>(CarteiraMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuMercadorNpc>> MERCADOR_NPC =
            MENUS.register("mercador_npc", () -> new MenuType<>(MenuMercadorNpc::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSacolaVip>> SACOLA_VIP =
            MENUS.register("sacola_vip", () -> new MenuType<>(MenuSacolaVip::new, FeatureFlags.VANILLA_SET));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
