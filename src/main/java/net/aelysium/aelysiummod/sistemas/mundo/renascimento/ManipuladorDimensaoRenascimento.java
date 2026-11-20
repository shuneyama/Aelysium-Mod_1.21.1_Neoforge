package net.aelysium.aelysiummod.sistemas.mundo.renascimento;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

import java.nio.file.Files;
import java.nio.file.Path;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorDimensaoRenascimento {

    @SubscribeEvent
    public static void onRespawnPosition(PlayerRespawnPositionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        boolean semPontoValido = player.getRespawnPosition() == null
                || event.getOriginalDimensionTransition().missingRespawnBlock();
        if (!semPontoValido) return;

        if (!ConfiguracaoDimensaoRenascimento.definido()) return;
        ResourceKey<Level> alvo = ConfiguracaoDimensaoRenascimento.getDimensaoPadrao();

        ServerLevel destino = player.getServer().getLevel(alvo);
        if (destino == null) return;

        event.setDimensionTransition(criarTransicao(destino, player));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getServer() == null) return;

        if (!ConfiguracaoDimensaoRenascimento.definido()) return;
        ResourceKey<Level> alvo = ConfiguracaoDimensaoRenascimento.getDimensaoPadrao();
        if (player.serverLevel().dimension() != Level.OVERWORLD) return;
        if (player.getRespawnPosition() != null) return;

        Path arquivo = player.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .resolve(player.getStringUUID() + ".dat");
        if (Files.exists(arquivo)) return;

        ServerLevel destino = player.getServer().getLevel(alvo);
        if (destino == null) return;

        player.getServer().execute(() ->
                player.changeDimension(criarTransicao(destino, player))
        );
    }

    private static DimensionTransition criarTransicao(ServerLevel destino, ServerPlayer player) {
        return new DimensionTransition(
                destino,
                new Vec3(ConfiguracaoDimensaoRenascimento.getX(), ConfiguracaoDimensaoRenascimento.getY(), ConfiguracaoDimensaoRenascimento.getZ()),
                Vec3.ZERO,
                ConfiguracaoDimensaoRenascimento.getYaw(),
                0.0F,
                true,
                DimensionTransition.DO_NOTHING
        );
    }
}
