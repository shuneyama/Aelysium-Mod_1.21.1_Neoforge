package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class CoracaoDoCreakingEntidade extends BlockEntity {

    private UUID criatura;
    private int espera;

    public CoracaoDoCreakingEntidade(BlockPos pos, BlockState estado) {
        super(ModJardimPalido.TIPO_CORACAO.get(), pos, estado);
    }

    public static void tick(Level mundo, BlockPos pos, BlockState estado, CoracaoDoCreakingEntidade entidade) {
        if (mundo instanceof ServerLevel servidor) entidade.servidor(servidor, pos, estado);
    }

    private void servidor(ServerLevel mundo, BlockPos pos, BlockState estado) {
        if ((mundo.getGameTime() + pos.asLong()) % 20L != 0L) return;
        EstadoCoracao novo = CoracaoDoCreaking.calcular(mundo, pos, estado.getValue(CoracaoDoCreaking.EIXO));
        if (novo != estado.getValue(CoracaoDoCreaking.ESTADO)) {
            estado = estado.setValue(CoracaoDoCreaking.ESTADO, novo);
            mundo.setBlock(pos, estado, 3);
        }
        if (criatura != null) {
            Entity e = mundo.getEntity(criatura);
            if (!(e instanceof Creaking c) || !c.isAlive()) {
                criatura = null;
                espera = 200;
                setChanged();
            }
        }
        if (novo != EstadoCoracao.AWAKE) return;
        RandomSource aleatorio = mundo.getRandom();
        if (aleatorio.nextInt(6) == 0) {
            mundo.playSound(null, pos, SonsJardimPalido.CORACAO_OCIOSO.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (criatura != null) return;
        if (espera > 0) {
            espera -= 20;
            return;
        }
        if (mundo.getDifficulty() == Difficulty.PEACEFUL || !mundo.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        Player jogador = mundo.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 32.0D, false);
        if (jogador == null) return;
        for (int i = 0; i < 8; i++) {
            int dx = aleatorio.nextInt(25) - 12;
            int dz = aleatorio.nextInt(25) - 12;
            if (dx * dx + dz * dz < 36) continue;
            for (int dy = 6; dy >= -8; dy--) {
                BlockPos alvo = pos.offset(dx, dy, dz);
                if (!mundo.getBlockState(alvo.below()).isFaceSturdy(mundo, alvo.below(), Direction.UP)) continue;
                AABB caixa = ModJardimPalido.CREAKING.get().getDimensions().makeBoundingBox(alvo.getX() + 0.5D, alvo.getY(), alvo.getZ() + 0.5D);
                if (!mundo.noCollision(caixa) || mundo.containsAnyLiquid(caixa)) continue;
                Creaking nova = ModJardimPalido.CREAKING.get().create(mundo);
                if (nova == null) return;
                nova.moveTo(alvo.getX() + 0.5D, alvo.getY(), alvo.getZ() + 0.5D, aleatorio.nextFloat() * 360.0F, 0.0F);
                nova.setCoracao(pos);
                mundo.addFreshEntity(nova);
                nova.playSound(SonsJardimPalido.CREAKING_NASCER.get(), 1.0F, 1.0F);
                mundo.playSound(null, pos, SonsJardimPalido.CORACAO_GERAR.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                criatura = nova.getUUID();
                setChanged();
                return;
            }
        }
        espera = 100;
    }

    public void criaturaFerida() {
        if (!(level instanceof ServerLevel mundo)) return;
        mundo.playSound(null, worldPosition, SonsJardimPalido.CORACAO_FERIDO.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        RandomSource aleatorio = mundo.getRandom();
        int quantidade = 2 + aleatorio.nextInt(3);
        for (int i = 0; i < 12 && quantidade > 0; i++) {
            BlockPos livre = worldPosition.offset(aleatorio.nextInt(7) - 3, aleatorio.nextInt(7) - 3, aleatorio.nextInt(7) - 3);
            BlockState atual = mundo.getBlockState(livre);
            if (!atual.isAir()) continue;
            Direction lado = Direction.getRandom(aleatorio);
            BlockPos apoio = livre.relative(lado);
            if (!mundo.getBlockState(apoio).isFaceSturdy(mundo, apoio, lado.getOpposite())) continue;
            mundo.setBlock(livre, ModJardimPalido.GRUMO_RESINA.get().defaultBlockState().setValue(MultifaceBlock.getFaceProperty(lado), true), 3);
            quantidade--;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag etiqueta, HolderLookup.Provider registros) {
        super.saveAdditional(etiqueta, registros);
        if (criatura != null) etiqueta.putUUID("Criatura", criatura);
        etiqueta.putInt("Espera", espera);
    }

    @Override
    protected void loadAdditional(CompoundTag etiqueta, HolderLookup.Provider registros) {
        super.loadAdditional(etiqueta, registros);
        criatura = etiqueta.hasUUID("Criatura") ? etiqueta.getUUID("Criatura") : null;
        espera = etiqueta.getInt("Espera");
    }
}
