package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class EventosJogabilidade {

    private static final Set<String> SEM_FERRAMENTA = Set.of(
            "bell", "lantern", "soul_lantern", "iron_door",
            "heavy_weighted_pressure_plate", "light_weighted_pressure_plate",
            "polished_blackstone_pressure_plate", "stone_pressure_plate",
            "brewing_stand", "ender_chest");

    @SubscribeEvent
    public static void coleiraFilhote(BabyEntitySpawnEvent evento) {
        var filho = evento.getChild();
        if (filho == null) return;
        Level mundo = filho.level();
        if (filho instanceof Wolf lobo && lobo.isTame()
                && evento.getParentA() instanceof Wolf a && evento.getParentB() instanceof Wolf b) {
            lobo.setCollarColor(misturar(mundo, a.getCollarColor(), b.getCollarColor()));
        } else if (filho instanceof Cat gato && gato.isTame()
                && evento.getParentA() instanceof Cat a && evento.getParentB() instanceof Cat b) {
            gato.setCollarColor(misturar(mundo, a.getCollarColor(), b.getCollarColor()));
        }
    }

    private static DyeColor misturar(Level mundo, DyeColor a, DyeColor b) {
        CraftingInput entrada = CraftingInput.of(2, 1, List.of(new ItemStack(DyeItem.byColor(a)), new ItemStack(DyeItem.byColor(b))));
        return mundo.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, entrada, mundo)
                .map(receita -> receita.value().assemble(entrada, mundo.registryAccess()))
                .map(ItemStack::getItem)
                .filter(DyeItem.class::isInstance)
                .map(item -> ((DyeItem) item).getDyeColor())
                .orElseGet(() -> mundo.getRandom().nextBoolean() ? a : b);
    }

    @SubscribeEvent
    public static void colheita(PlayerEvent.HarvestCheck evento) {
        BlockState estado = evento.getTargetBlock();
        var id = BuiltInRegistries.BLOCK.getKey(estado.getBlock());
        if (!"minecraft".equals(id.getNamespace())) return;
        String caminho = id.getPath();
        if (SEM_FERRAMENTA.contains(caminho) || caminho.endsWith("copper_door")) {
            evento.setCanHarvest(true);
        }
    }

    @SubscribeEvent
    public static void tnt(ExplosionEvent.Start evento) {
        if (evento.getLevel().isClientSide()) return;
        if ((evento.getExplosion().getDirectSourceEntity() instanceof net.minecraft.world.entity.item.PrimedTnt
                || evento.getExplosion().getDirectSourceEntity() instanceof net.minecraft.world.entity.vehicle.MinecartTNT)
                && !evento.getLevel().getGameRules().getBoolean(net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.RegrasAtualizadas.TNT_EXPLODE)) {
            evento.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void nether(EntityTravelToDimensionEvent evento) {
        if (evento.getDimension() == Level.NETHER && !evento.getEntity().level().isClientSide()
                && !evento.getEntity().level().getGameRules().getBoolean(net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.RegrasAtualizadas.PERMITIR_NETHER)) {
            evento.setCanceled(true);
        }
    }
}
