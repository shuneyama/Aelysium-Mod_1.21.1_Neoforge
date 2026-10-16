package net.aelysium.aelysiummod.conteudo.bloco;

import net.aelysium.aelysiummod.registro.ModParticulas;
import net.aelysium.aelysiummod.sistemas.mundo.lua.GerenciadorLua;
import net.aelysium.aelysiummod.sistemas.mundo.lua.TipoLua;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

public class DamaNoite extends FlowerBlock {

    public static final BooleanProperty LUA_VERMELHA = BooleanProperty.create("lua_vermelha");

    private int particleCooldown = 0;

    public DamaNoite(Holder<MobEffect> effect, float seconds, Properties properties) {
        super(effect, seconds, properties);
        registerDefaultState(stateDefinition.any().setValue(LUA_VERMELHA, false));
    }

    public static boolean luaVermelhaAtiva() {
        return GerenciadorLua.luaAtual == TipoLua.VERMELHA;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LUA_VERMELHA);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState estado = super.getStateForPlacement(context);
        return estado == null ? null : estado.setValue(LUA_VERMELHA, luaVermelhaAtiva());
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        sincronizar(state, level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        sincronizar(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && state.getValue(LUA_VERMELHA) != luaVermelhaAtiva()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    private void sincronizar(BlockState state, ServerLevel level, BlockPos pos) {
        if (!state.is(this)) return;
        boolean alvo = luaVermelhaAtiva();
        if (state.getValue(LUA_VERMELHA) != alvo) {
            level.setBlock(pos, state.setValue(LUA_VERMELHA, alvo), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!level.isClientSide) return;

        if (particleCooldown > 0) {
            particleCooldown--;
            return;
        }

        AABB bb = state.getShape(level, pos).bounds();
        double centerX = pos.getX() + (bb.minX + bb.maxX) / 2.0;
        double centerY = pos.getY() + bb.maxY;
        double centerZ = pos.getZ() + (bb.minZ + bb.maxZ) / 2.0;

        double radius = 0.1875;
        double offsetX = (random.nextDouble() * 2 - 1) * radius;
        double offsetZ = (random.nextDouble() * 2 - 1) * radius;

        double speedY = 0.01 + random.nextDouble() * 0.01;
        double speedX = (random.nextDouble() * 2 - 1) * 0.005;
        double speedZ = (random.nextDouble() * 2 - 1) * 0.005;

        var particula = state.getValue(LUA_VERMELHA)
                ? ModParticulas.DAMA_VERMELHA_DA_NOITE_PARTICULA.get()
                : ModParticulas.DAMA_DA_NOITE_PARTICULA.get();

        level.addParticle(particula,
                centerX + offsetX,
                centerY + 0.15,
                centerZ + offsetZ,
                speedX, speedY, speedZ);

        particleCooldown = 5;
    }
}
