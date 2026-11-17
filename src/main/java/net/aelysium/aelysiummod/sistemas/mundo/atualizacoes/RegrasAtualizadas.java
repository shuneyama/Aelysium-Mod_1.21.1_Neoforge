package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

public final class RegrasAtualizadas {

    public static final GameRules.Key<GameRules.BooleanValue> DESATIVAR_CHECAGEM_MOVIMENTO =
            GameRules.register("disablePlayerMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> FOGO_LONGE_DO_JOGADOR =
            GameRules.register("allowFireTicksAwayFromPlayer", GameRules.Category.UPDATES, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> TNT_EXPLODE =
            GameRules.register("tntExplodes", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<GameRules.BooleanValue> PERMITIR_NETHER =
            GameRules.register("allowEnteringNetherUsingPortals", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    private RegrasAtualizadas() {
    }

    public static void registrar() {
    }

    public static boolean fogoPermitido(ServerLevel mundo, BlockPos pos) {
        if (mundo.getGameRules().getBoolean(FOGO_LONGE_DO_JOGADOR)) return true;
        return mundo.getNearestPlayer(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 128.0D, false) != null;
    }
}
