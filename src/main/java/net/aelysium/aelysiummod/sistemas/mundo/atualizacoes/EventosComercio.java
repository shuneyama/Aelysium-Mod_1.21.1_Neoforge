package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class EventosComercio {

    @SubscribeEvent
    public static void bibliotecario(VillagerTradesEvent evento) {
        if (evento.getType() != VillagerProfession.LIBRARIAN) return;
        List<VillagerTrades.ItemListing> mestre = evento.getTrades().get(5);
        if (mestre == null) return;
        mestre.clear();
        mestre.add(new VillagerTrades.ItemsForEmeralds(Items.RED_CANDLE, 3, 1, 12, 30));
        mestre.add(new VillagerTrades.ItemsForEmeralds(Items.YELLOW_CANDLE, 3, 1, 12, 30));
    }

    @SubscribeEvent
    public static void mercador(WandererTradesEvent evento) {
        evento.getGenericTrades().add(new VillagerTrades.ItemsForEmeralds(Items.NAME_TAG, 1, 1, 5, 1));
    }

    @SubscribeEvent
    public static void cartografo(VillagerTradesEvent evento) {
        if (evento.getType() != VillagerProfession.CARTOGRAPHER) return;
        Int2ObjectMap<VillagerTrades.ItemListing[]> novos = VillagerTrades.EXPERIMENTAL_TRADES.get(VillagerProfession.CARTOGRAPHER);
        if (novos == null) return;
        for (int nivel = 1; nivel <= 5; nivel++) {
            List<VillagerTrades.ItemListing> lista = evento.getTrades().get(nivel);
            VillagerTrades.ItemListing[] experimentais = novos.get(nivel);
            if (lista == null || experimentais == null) continue;
            lista.clear();
            lista.addAll(List.of(experimentais));
        }
    }
}
