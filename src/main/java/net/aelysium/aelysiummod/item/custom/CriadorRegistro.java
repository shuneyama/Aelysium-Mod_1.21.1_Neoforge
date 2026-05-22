package net.aelysium.aelysiummod.item.custom;

import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.network.Filterable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CriadorRegistro extends Item {

    public CriadorRegistro(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("NpcName")) {
            return Component.literal("§e✦ Registro de Vendas — " + tag.getString("NpcName"));
        }
        return Component.literal("§e✦ Registro de Vendas");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null || level.isClientSide) return InteractionResult.PASS;
        if (!player.hasPermissions(2) || !player.isCreative()) return InteractionResult.PASS;

        ItemStack bookStack = context.getItemInHand();
        CustomData customData = bookStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.contains("LinkedNpcUUID")) {
            player.displayClientMessage(
                    Component.literal("§cEste livro não está vinculado a nenhum NPC!"), true);
            return InteractionResult.FAIL;
        }

        UUID npcUUID = tag.getUUID("LinkedNpcUUID");

        if (level instanceof ServerLevel serverLevel) {
            CustomNpcEntity npc = findNpcByUUID(serverLevel, npcUUID);
            if (npc == null) {
                player.displayClientMessage(
                        Component.literal("§cNPC não encontrado! Pode ter sido removido."), true);
                return InteractionResult.FAIL;
            }

            BlockPos pos = context.getClickedPos().above();
            placeLecternWithLog(serverLevel, pos, npc);
            player.displayClientMessage(
                    Component.literal("§a\uD83D\uDCD6 Lectern de registro colocado!"), true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private CustomNpcEntity findNpcByUUID(ServerLevel level, UUID uuid) {
        for (var entity : level.getAllEntities()) {
            if (entity instanceof CustomNpcEntity npc && npc.getUUID().equals(uuid)) {
                return npc;
            }
        }
        return null;
    }

    private void placeLecternWithLog(ServerLevel level, BlockPos pos, CustomNpcEntity npc) {
        BlockState lecternState = Blocks.LECTERN.defaultBlockState();
        level.setBlock(pos, lecternState, 3);

        ItemStack writtenBook = new ItemStack(Items.WRITTEN_BOOK);

        List<Filterable<Component>> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        page.append("§l═══════════\n");
        page.append("§6§lRegistro de Vendas\n");
        page.append("§rNPC: §e").append(npc.getNpcName()).append("\n");
        page.append("§l═══════════\n\n");

        Map<String, Integer> log = npc.getTradeLog();
        if (log.isEmpty()) {
            page.append("§7Nenhuma venda registrada.");
            pages.add(Filterable.passThrough(Component.literal(page.toString())));
        } else {
            int lineCount = 5;
            for (Map.Entry<String, Integer> entry : log.entrySet()) {
                String line = "§fItem: §b" + entry.getKey() + "\n  §7Vendido: §a" + entry.getValue() + " vezes\n\n";
                lineCount += 3;
                if (lineCount > 14) {
                    pages.add(Filterable.passThrough(Component.literal(page.toString())));
                    page = new StringBuilder();
                    lineCount = 3;
                }
                page.append(line);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            page.append("\n§7Última atualização:\n  §8").append(timestamp);
            pages.add(Filterable.passThrough(Component.literal(page.toString())));
        }

        WrittenBookContent bookContent = new WrittenBookContent(
                Filterable.passThrough("Registro de Vendas - " + npc.getNpcName()),
                "Server",
                0,
                pages,
                true
        );
        writtenBook.set(DataComponents.WRITTEN_BOOK_CONTENT, bookContent);

        if (level.getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(writtenBook);
            LecternBlock.resetBookState(null, level, pos, level.getBlockState(pos), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("LinkedNpcUUID")) {
            tooltipComponents.add(Component.literal("§aVinculado a: §e" + tag.getString("NpcName")));
            tooltipComponents.add(Component.literal("§7Clique no chão para gerar o lectern"));
        } else {
            tooltipComponents.add(Component.literal("§cNão vinculado a nenhum NPC"));
            tooltipComponents.add(Component.literal("§7Use a aba Configurações do editor de NPC"));
        }
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}