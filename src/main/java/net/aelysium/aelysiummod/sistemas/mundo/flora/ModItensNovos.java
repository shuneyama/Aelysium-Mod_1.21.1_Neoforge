package net.aelysium.aelysiummod.sistemas.mundo.flora;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItensNovos {

    public static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);

    public static final ResourceKey<JukeboxSong> TEARS = ResourceKey.create(Registries.JUKEBOX_SONG,
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "tears"));

    public static final DeferredItem<Item> DISCO_TEARS = ITENS.register("music_disc_tears",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(TEARS)));

    public static final DeferredItem<Item> ARMADURA_NETHERITE_CAVALO = ITENS.register("netherite_horse_armor",
            () -> new AnimalArmorItem(ArmorMaterials.NETHERITE, AnimalArmorItem.BodyType.EQUESTRIAN, false,
                    new Item.Properties().stacksTo(1).fireResistant()));

    private ModItensNovos() {
    }

    public static void registrar(IEventBus barramento) {
        ITENS.register(barramento);
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID)
    public static class Aba {
        @SubscribeEvent
        public static void aba(BuildCreativeModeTabContentsEvent evento) {
            if (evento.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                evento.accept(DISCO_TEARS);
            }
            if (evento.getTabKey() == CreativeModeTabs.COMBAT) {
                evento.accept(ARMADURA_NETHERITE_CAVALO);
            }
        }
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID)
    public static class Eventos {
        @SubscribeEvent
        public static void morte(LivingDeathEvent evento) {
            if (!(evento.getEntity() instanceof Ghast ghast) || ghast.level().isClientSide()) return;
            if (evento.getSource().getDirectEntity() instanceof LargeFireball && evento.getSource().getEntity() instanceof Player) {
                ghast.level().addFreshEntity(new ItemEntity(ghast.level(), ghast.getX(), ghast.getY(), ghast.getZ(), new ItemStack(DISCO_TEARS.get())));
            }
        }
    }
}
