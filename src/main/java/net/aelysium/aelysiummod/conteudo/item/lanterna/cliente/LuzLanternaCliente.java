package net.aelysium.aelysiummod.conteudo.item.lanterna.cliente;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.lanterna.LanternaItem;
import net.aelysium.aelysiummod.registro.ModItens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class LuzLanternaCliente {

    private static final int NIVEL_LUZ = 13;
    private static final int QUEDA_POR_BLOCO = 3;
    private static final double ALCANCE = 12.0;
    private static final double DISTANCIA_INICIAL = 2.0;
    private static final double PASSO = 0.5;
    private static final double DISTANCIA_MAXIMA_JOGADOR = 96.0;

    private static volatile Long2IntOpenHashMap luzes = new Long2IntOpenHashMap();

    private LuzLanternaCliente() {
    }

    public static int nivel(BlockPos pos) {
        Long2IntOpenHashMap atuais = luzes;
        return atuais.isEmpty() ? 0 : atuais.get(pos.asLong());
    }

    @SubscribeEvent
    public static void aoTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Long2IntOpenHashMap novas = new Long2IntOpenHashMap();

        if (level != null && minecraft.player != null) {
            for (Player jogador : level.players()) {
                if (jogador.isSpectator() || !seguraLanternaLigada(jogador)) continue;
                if (jogador.distanceTo(minecraft.player) > DISTANCIA_MAXIMA_JOGADOR) continue;
                calcularFeixe(level, jogador, novas);
            }
        }

        Long2IntOpenHashMap antigas = luzes;
        if (novas.equals(antigas)) return;

        luzes = novas;
        if (level != null) {
            marcarSecoes(minecraft.levelRenderer, antigas, novas);
        }
    }

    private static boolean seguraLanternaLigada(Player jogador) {
        return lanternaLigada(jogador.getMainHandItem()) || lanternaLigada(jogador.getOffhandItem());
    }

    private static boolean lanternaLigada(ItemStack stack) {
        return stack.is(ModItens.LANTERNA.get()) && LanternaItem.estaLigada(stack);
    }

    private static void calcularFeixe(ClientLevel level, Player jogador, Long2IntOpenHashMap destino) {
        Vec3 olho = jogador.getEyePosition();
        Vec3 direcao = jogador.getViewVector(1.0f);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Long2IntOpenHashMap feixe = new Long2IntOpenHashMap();
        LongArrayFIFOQueue fila = new LongArrayFIFOQueue();
        boolean encontrouAr = false;
        long ultimaPosicao = 0L;

        for (double distancia = PASSO; distancia <= ALCANCE; distancia += PASSO) {
            Vec3 ponto = olho.add(direcao.scale(distancia));
            cursor.set(ponto.x, ponto.y, ponto.z);
            if (level.getBlockState(cursor).isSolidRender(level, cursor)) break;

            ultimaPosicao = cursor.asLong();
            encontrouAr = true;
            if (distancia >= DISTANCIA_INICIAL && feixe.put(ultimaPosicao, NIVEL_LUZ) == 0) {
                fila.enqueue(ultimaPosicao);
            }
        }

        if (fila.isEmpty() && encontrouAr) {
            feixe.put(ultimaPosicao, NIVEL_LUZ);
            fila.enqueue(ultimaPosicao);
        }

        while (!fila.isEmpty()) {
            long atual = fila.dequeueLong();
            int proximoNivel = feixe.get(atual) - QUEDA_POR_BLOCO;
            if (proximoNivel <= 0) continue;

            for (Direction lado : Direction.values()) {
                long vizinho = BlockPos.offset(atual, lado);
                if (feixe.get(vizinho) >= proximoNivel) continue;

                cursor.set(vizinho);
                if (level.getBlockState(cursor).isSolidRender(level, cursor)) continue;

                feixe.put(vizinho, proximoNivel);
                fila.enqueue(vizinho);
            }
        }

        for (Long2IntMap.Entry entrada : feixe.long2IntEntrySet()) {
            destino.mergeInt(entrada.getLongKey(), entrada.getIntValue(), Math::max);
        }
    }

    private static void marcarSecoes(LevelRenderer renderizador, Long2IntOpenHashMap antigas, Long2IntOpenHashMap novas) {
        LongOpenHashSet secoes = new LongOpenHashSet();
        coletarDiferencas(antigas, novas, secoes);
        coletarDiferencas(novas, antigas, secoes);

        LongIterator iterador = secoes.iterator();
        while (iterador.hasNext()) {
            long secao = iterador.nextLong();
            renderizador.setSectionDirty(SectionPos.x(secao), SectionPos.y(secao), SectionPos.z(secao));
        }
    }

    private static void coletarDiferencas(Long2IntOpenHashMap origem, Long2IntOpenHashMap comparacao, LongOpenHashSet secoes) {
        for (Long2IntMap.Entry entrada : origem.long2IntEntrySet()) {
            long posicao = entrada.getLongKey();
            if (comparacao.get(posicao) == entrada.getIntValue()) continue;

            int x = BlockPos.getX(posicao);
            int y = BlockPos.getY(posicao);
            int z = BlockPos.getZ(posicao);
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dy = -1; dy <= 1; dy += 2) {
                    for (int dz = -1; dz <= 1; dz += 2) {
                        secoes.add(SectionPos.asLong(
                                SectionPos.blockToSectionCoord(x + dx),
                                SectionPos.blockToSectionCoord(y + dy),
                                SectionPos.blockToSectionCoord(z + dz)));
                    }
                }
            }
        }
    }
}
