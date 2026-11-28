package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ChaoArmadilha {
    private static final int BUSCA_CHAO = 4;
    private static final int ESPERA_OCUPADO = 20;
    private static final Map<ServerLevel, Map<BlockPos, Pendente>> PENDENTES = new HashMap<>();

    private record Pendente(BlockState original, BlockState colocado, long fim) {}

    public record Armadilha(BlockState bloco, ParticleOptions particula, SoundEvent som) {}

    private ChaoArmadilha() {
    }

    public static int registrarAtaque(LivingEntity chefe, AlvosChefe alvos, String prefixo, int contador, Armadilha armadilha) {
        if (chefe.getHealth() >= chefe.getMaxHealth() * ConfiguracaoChefes.numero(prefixo + ".vida_abaixo_de")) return contador;
        int intervalo = ConfiguracaoChefes.inteiro(prefixo + ".a_cada_ataques");
        if (intervalo <= 0) return 0;
        if (++contador < intervalo) return contador;
        int quantidade = Math.max(ConfiguracaoChefes.inteiro(prefixo + ".alvos"), 1);
        double raio = ConfiguracaoChefes.numero(prefixo + ".raio");
        double duracao = ConfiguracaoChefes.numero(prefixo + ".duracao_segundos");
        for (LivingEntity alvo : alvos.alvos(quantidade)) {
            criar(chefe.level(), alvo, raio, duracao, armadilha);
        }
        return 0;
    }

    public static void criar(Level level, LivingEntity alvo, double raio, double duracaoSegundos, Armadilha armadilha) {
        if (!(level instanceof ServerLevel nivel) || raio <= 0) return;
        BlockPos chao = encontrarChao(nivel, alvo);
        if (chao == null) return;

        long fim = nivel.getGameTime() + Math.max(Math.round(duracaoSegundos * 20), 1);
        Map<BlockPos, Pendente> pendentes = PENDENTES.computeIfAbsent(nivel, chave -> new HashMap<>());
        double centroX = alvo.getX();
        double centroZ = alvo.getZ();
        double raioQuadrado = raio * raio;
        int minX = Mth.floor(centroX - raio);
        int maxX = Mth.floor(centroX + raio);
        int minZ = Mth.floor(centroZ - raio);
        int maxZ = Mth.floor(centroZ + raio);
        boolean mudou = false;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double dx = x + 0.5 - centroX;
                double dz = z + 0.5 - centroZ;
                if (dx * dx + dz * dz > raioQuadrado) continue;
                BlockPos pos = new BlockPos(x, chao.getY(), z);
                BlockState original = nivel.getBlockState(pos);
                if (!podeConverter(nivel, pos, original)) continue;
                nivel.setBlockAndUpdate(pos, armadilha.bloco());
                pendentes.put(pos, new Pendente(original, armadilha.bloco(), fim));
                nivel.sendParticles(armadilha.particula(), x + 0.5, chao.getY() + 1.05, z + 0.5, 4, 0.3, 0.1, 0.3, 0.02);
                mudou = true;
            }
        }

        if (mudou) {
            nivel.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), armadilha.som(), SoundSource.HOSTILE, 1.5F, 0.8F);
        }
    }

    public static void tick(MinecraftServer servidor) {
        if (PENDENTES.isEmpty() || servidor.getTickCount() % 10 != 0) return;
        Iterator<Map.Entry<ServerLevel, Map<BlockPos, Pendente>>> niveis = PENDENTES.entrySet().iterator();
        while (niveis.hasNext()) {
            Map.Entry<ServerLevel, Map<BlockPos, Pendente>> entrada = niveis.next();
            ServerLevel nivel = entrada.getKey();
            long agora = nivel.getGameTime();
            Iterator<Map.Entry<BlockPos, Pendente>> blocos = entrada.getValue().entrySet().iterator();
            while (blocos.hasNext()) {
                Map.Entry<BlockPos, Pendente> bloco = blocos.next();
                Pendente pendente = bloco.getValue();
                BlockPos pos = bloco.getKey();
                if (agora < pendente.fim() || !nivel.isLoaded(pos)) continue;
                if (ocupado(nivel, pos)) {
                    bloco.setValue(new Pendente(pendente.original(), pendente.colocado(), agora + ESPERA_OCUPADO));
                    continue;
                }
                restaurar(nivel, pos, pendente);
                blocos.remove();
            }
            if (entrada.getValue().isEmpty()) niveis.remove();
        }
    }

    public static void restaurarTudo() {
        for (Map.Entry<ServerLevel, Map<BlockPos, Pendente>> entrada : PENDENTES.entrySet()) {
            for (Map.Entry<BlockPos, Pendente> bloco : entrada.getValue().entrySet()) {
                restaurar(entrada.getKey(), bloco.getKey(), bloco.getValue());
            }
        }
        PENDENTES.clear();
    }

    private static void restaurar(ServerLevel nivel, BlockPos pos, Pendente pendente) {
        if (nivel.getBlockState(pos) != pendente.colocado()) return;
        nivel.setBlockAndUpdate(pos, pendente.original());
    }

    private static boolean ocupado(ServerLevel nivel, BlockPos pos) {
        return !nivel.getEntitiesOfClass(LivingEntity.class, new AABB(pos), LivingEntity::isAlive).isEmpty();
    }

    private static BlockPos encontrarChao(ServerLevel nivel, LivingEntity alvo) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(alvo.getX(), alvo.getY() - 0.05, alvo.getZ()).mutable();
        for (int i = 0; i <= BUSCA_CHAO; i++) {
            BlockState estado = nivel.getBlockState(pos);
            if (!estado.isAir() && !estado.getCollisionShape(nivel, pos).isEmpty()) return pos.immutable();
            pos.move(0, -1, 0);
        }
        return null;
    }

    private static boolean podeConverter(ServerLevel nivel, BlockPos pos, BlockState estado) {
        if (estado.isAir() || estado.hasBlockEntity()) return false;
        if (ehArmadilhaPendente(nivel, pos)) return false;
        if (estado.getDestroySpeed(nivel, pos) < 0) return false;
        if (!estado.getFluidState().isEmpty()) return false;
        return estado.isCollisionShapeFullBlock(nivel, pos);
    }

    private static boolean ehArmadilhaPendente(ServerLevel nivel, BlockPos pos) {
        Map<BlockPos, Pendente> pendentes = PENDENTES.get(nivel);
        return pendentes != null && pendentes.containsKey(pos);
    }
}
