package net.aelysium.aelysiummod.sistemas.mundo.portal;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.registro.ModBlocos;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class AtivadorPortalLar {

    private static final int MAX_POCA = 16;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;

        Level level = item.level();
        if (level.isClientSide) return;

        ItemStack stack = item.getItem();
        if (stack.getItem() != ModItens.CHAVE_QUE_NAO_VAI.get()) return;
        if (!item.isInWater()) return;

        BlockPos origem = item.blockPosition();
        if (!level.getBlockState(origem).is(Blocks.WATER)) return;
        if (!level.getFluidState(origem).isSource()) return;

        Set<BlockPos> poca = mapearPoca(level, origem);
        if (poca == null) return;

        if (!estruturaValida(level, poca)) return;

        ativarPortal(level, poca, item);
    }

    private static Set<BlockPos> mapearPoca(Level level, BlockPos origem) {
        Set<BlockPos> poca = new HashSet<>();
        Deque<BlockPos> fila = new ArrayDeque<>();
        fila.add(origem);
        poca.add(origem);

        while (!fila.isEmpty()) {
            BlockPos atual = fila.poll();
            for (BlockPos vizinho : new BlockPos[]{atual.north(), atual.south(), atual.east(), atual.west()}) {
                if (poca.contains(vizinho)) continue;
                if (vizinho.getY() != origem.getY()) continue;
                if (!level.getBlockState(vizinho).is(Blocks.WATER)) continue;
                if (!level.getFluidState(vizinho).isSource()) continue;

                poca.add(vizinho);
                fila.add(vizinho);
                if (poca.size() > MAX_POCA) return null;
            }
        }

        return poca;
    }

    private static boolean estruturaValida(Level level, Set<BlockPos> poca) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        int y = 0;
        for (BlockPos pos : poca) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            y = pos.getY();
        }

        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int z = minZ - 1; z <= maxZ + 1; z++) {
                boolean dentroX = x >= minX && x <= maxX;
                boolean dentroZ = z >= minZ && z <= maxZ;
                if (dentroX && dentroZ) continue;

                boolean quina = !dentroX && !dentroZ;
                BlockPos base = new BlockPos(x, y, z);

                boolean ok = quina
                        ? temPodzolComMushroom(level, base)
                        : temPodzolComFlorOuSamambaia(level, base);
                if (!ok) return false;
            }
        }

        return true;
    }

    private static boolean temPodzolComMushroom(Level level, BlockPos base) {
        if (!level.getBlockState(base).is(Blocks.PODZOL)) return false;
        return level.getBlockState(base.above()).getBlock() instanceof MushroomBlock;
    }

    private static boolean temPodzolComFlorOuSamambaia(Level level, BlockPos base) {
        if (!level.getBlockState(base).is(Blocks.PODZOL)) return false;
        BlockState acima = level.getBlockState(base.above());
        return acima.getBlock() instanceof FlowerBlock || acima.is(Blocks.FERN);
    }

    private static void ativarPortal(Level level, Set<BlockPos> poca, ItemEntity item) {
        List<BlockPos> blocos = new ArrayList<>(poca);
        for (BlockPos pos : blocos) {
            level.setBlock(pos, ModBlocos.LAR_PORTAL.get().defaultBlockState(), 3);
        }

        BlockPos centro = blocos.get(0);
        level.playSound(null, centro, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            for (BlockPos pos : blocos) {
                for (int i = 0; i < 20; i++) {
                    double ox = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
                    double oy = pos.getY() + 0.3 + level.random.nextDouble() * 0.7;
                    double oz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
                    serverLevel.sendParticles(BlocoPortalLar.corAleatoria(level.random), ox, oy, oz, 1, 0.0, 0.05, 0.0, 0.1);
                }

                for (int i = 0; i < 12; i++) {
                    double ox = pos.getX() + 0.5;
                    double oy = pos.getY() + 0.5;
                    double oz = pos.getZ() + 0.5;
                    serverLevel.sendParticles(ParticleTypes.POOF, ox, oy, oz, 1,
                            (level.random.nextDouble() - 0.5) * 0.6,
                            level.random.nextDouble() * 0.5,
                            (level.random.nextDouble() - 0.5) * 0.6, 0.12);
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, ox, oy, oz, 1,
                            (level.random.nextDouble() - 0.5) * 0.4,
                            level.random.nextDouble() * 0.4,
                            (level.random.nextDouble() - 0.5) * 0.4, 0.08);
                }
            }
        }

        Entity dono = item.getOwner();
        if (dono instanceof ServerPlayer player) {
            ItemStack devolucao = item.getItem().copy();
            if (!player.getInventory().add(devolucao)) {
                player.drop(devolucao, false);
            }
        }

        item.discard();
    }

    private AtivadorPortalLar() {
    }
}
