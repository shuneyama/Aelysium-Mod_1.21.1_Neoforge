package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class EventosAtualizacoes {

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID)
    public static class Mod {

        @SubscribeEvent
        public static void raridades(ModifyDefaultComponentsEvent evento) {
            for (Item item : BuiltInRegistries.ITEM) {
                var id = BuiltInRegistries.ITEM.getKey(item);
                if (!"minecraft".equals(id.getNamespace())) continue;
                Rarity raridade = RaridadesAtualizadas.de(id.getPath());
                if (raridade != null) {
                    evento.modify(item, construtor -> construtor.set(DataComponents.RARITY, raridade));
                }
            }
        }

        @SubscribeEvent
        public static void abaCriativa(BuildCreativeModeTabContentsEvent evento) {
            if (evento.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                evento.accept(net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas.MOCHILA.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                net.aelysium.aelysiummod.sistemas.mundo.mochila.ModMochilas.COLORIDAS.values().forEach(item -> evento.accept(item.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
            }
            if (evento.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                ItemStack anterior = new ItemStack(Items.GUSTER_BANNER_PATTERN);
                ItemStack masoned = new ItemStack(ModItens.FIELD_MASONED_BANNER_PATTERN.get());
                ItemStack bordure = new ItemStack(ModItens.BORDURE_INDENTED_BANNER_PATTERN.get());
                evento.insertAfter(anterior, masoned, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                evento.insertAfter(masoned, bordure, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    @EventBusSubscriber(modid = AelysiumMod.MOD_ID)
    public static class Jogo {

        @SubscribeEvent
        public static void quedaNoSlime(LivingFallEvent evento) {
            LivingEntity entidade = evento.getEntity();
            if (entidade.isSuppressingBounce() && entidade.level().getBlockState(entidade.getOnPos()).is(Blocks.SLIME_BLOCK)) {
                evento.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void ricochetearNaBorda(EntityTickEvent.Pre evento) {
            Entity entidade = evento.getEntity();
            if (entidade.level().isClientSide || !(entidade instanceof AbstractArrow || entidade instanceof FishingHook)) return;
            Vec3 movimento = entidade.getDeltaMovement();
            if (movimento.lengthSqr() < 1.0E-4D) return;
            WorldBorder borda = entidade.level().getWorldBorder();
            Vec3 posicao = entidade.position();
            Vec3 proxima = posicao.add(movimento);
            if (borda.isWithinBounds(posicao.x, posicao.z) && !borda.isWithinBounds(proxima.x, proxima.z)) {
                entidade.setDeltaMovement(movimento.scale(-0.2D));
                entidade.hurtMarked = true;
            }
        }

        @SubscribeEvent
        public static void dicas(ItemTooltipEvent evento) {
            ItemStack pilha = evento.getItemStack();
            List<Component> linhas = evento.getToolTip();
            if (pilha.getItem() instanceof BannerPatternItem) {
                String chave = pilha.getDescriptionId() + ".desc";
                linhas.removeIf(linha -> linha.getContents() instanceof TranslatableContents t && t.getKey().equals(chave));
            } else if (pilha.getItem() instanceof SmithingTemplateItem) {
                for (int i = 1; i < linhas.size(); i++) {
                    if (linhas.get(i).getContents() instanceof TranslatableContents t
                            && (t.getKey().startsWith("trim_pattern.") || t.getKey().startsWith("upgrade."))) {
                        linhas.set(i, Component.translatable("item.minecraft.smithing_template").withStyle(ChatFormatting.GRAY));
                        break;
                    }
                }
            }
        }
    }
}
