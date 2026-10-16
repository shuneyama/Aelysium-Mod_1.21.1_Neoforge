package net.aelysium.aelysiummod.sistemas.social.npc.item;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class AtualizadorRegistroVendas {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.LECTERN)) return;
        if (!(level.getBlockEntity(pos) instanceof LecternBlockEntity lectern)) return;

        ItemStack livroAtual = lectern.getBook();
        if (livroAtual.isEmpty()) return;

        CustomData customData = livroAtual.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("LinkedNpcUUID")) return;

        UUID npcUUID = tag.getUUID("LinkedNpcUUID");
        EntidadeNpcPersonalizado npc = CriadorRegistro.findNpcByUUID(serverLevel, npcUUID);
        if (npc == null) return;

        lectern.setBook(CriadorRegistro.criarLivro(npc));
    }

    private AtualizadorRegistroVendas() {
    }
}
