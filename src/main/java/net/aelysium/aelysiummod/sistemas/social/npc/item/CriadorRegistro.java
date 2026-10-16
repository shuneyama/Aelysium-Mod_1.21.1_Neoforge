package net.aelysium.aelysiummod.sistemas.social.npc.item;

import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
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
import net.minecraft.core.Direction;
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
        return Component.translatable("mensagem.aelysiummod.npc.registro_de_vendas");
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
                    Component.translatable("mensagem.aelysiummod.npc.este_livro_nao_esta_vinculado_a_nenhum"), true);
            return InteractionResult.FAIL;
        }

        UUID npcUUID = tag.getUUID("LinkedNpcUUID");

        if (level instanceof ServerLevel serverLevel) {
            EntidadeNpcPersonalizado npc = findNpcByUUID(serverLevel, npcUUID);
            if (npc == null) {
                player.displayClientMessage(
                        Component.translatable("mensagem.aelysiummod.npc.npc_nao_encontrado_pode_ter_sido"), true);
                return InteractionResult.FAIL;
            }

            BlockPos pos = context.getClickedPos().above();
            Direction facing = context.getHorizontalDirection().getOpposite();
            placeLecternWithLog(serverLevel, pos, npc, facing);
            player.displayClientMessage(
                    Component.translatable("mensagem.aelysiummod.npc.lectern_de_registro_colocado"), true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static EntidadeNpcPersonalizado findNpcByUUID(ServerLevel level, UUID uuid) {
        for (var entity : level.getAllEntities()) {
            if (entity instanceof EntidadeNpcPersonalizado npc && npc.getUUID().equals(uuid)) {
                return npc;
            }
        }
        return null;
    }

    private void placeLecternWithLog(ServerLevel level, BlockPos pos, EntidadeNpcPersonalizado npc, Direction facing) {
        BlockState lecternState = Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, facing);
        level.setBlock(pos, lecternState, 3);

        if (level.getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(criarLivro(npc));
            LecternBlock.resetBookState(null, level, pos, level.getBlockState(pos), true);
        }
    }

    public static ItemStack criarLivro(EntidadeNpcPersonalizado npc) {
        ItemStack writtenBook = new ItemStack(Items.WRITTEN_BOOK);
        writtenBook.set(DataComponents.WRITTEN_BOOK_CONTENT, criarConteudoLivro(npc));

        CompoundTag vinculo = new CompoundTag();
        vinculo.putUUID("LinkedNpcUUID", npc.getUUID());
        vinculo.putString("NpcName", npc.getNpcName());
        writtenBook.set(DataComponents.CUSTOM_DATA, CustomData.of(vinculo));

        return writtenBook;
    }

    private static WrittenBookContent criarConteudoLivro(EntidadeNpcPersonalizado npc) {
        List<Filterable<Component>> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        page.append("§0").append(npc.getNpcName()).append("\n");
        page.append("§0§l═══════════\n\n");

        Map<String, Integer> log = npc.getTradeLog();
        if (log.isEmpty()) {
            page.append("§7Nenhuma venda registrada.");
            pages.add(Filterable.passThrough(Component.literal(page.toString())));
        } else {
            int lineCount = 5;
            for (Map.Entry<String, Integer> entry : log.entrySet()) {
                String line = "§0" + entry.getKey() + "\n  §7Vendido: §2" + entry.getValue() + " vezes\n\n";
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

        String titulo = npc.getNpcName();
        if (titulo.length() > 32) titulo = titulo.substring(0, 32);

        return new WrittenBookContent(
                Filterable.passThrough(titulo),
                "Server",
                0,
                pages,
                true
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.contains("LinkedNpcUUID")) {
            tooltipComponents.add(Component.translatableEscape("tooltip.aelysiummod.npc.vinculado_a", tag.getString("NpcName")));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.aelysiummod.npc.nao_vinculado_a_nenhum_npc"));
        }
        super.appendHoverText(stack, context, tooltipComponents, flag);
    }
}