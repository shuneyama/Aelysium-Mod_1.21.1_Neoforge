package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.sistemas.admin.protecao.item.VarinhaProtecao;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class UtilitariosEventoProtecao {

    static boolean usandoVarinhaProtecao(ServerPlayer jogador) {
        return jogador.getMainHandItem().getItem() instanceof VarinhaProtecao && jogador.hasPermissions(2);
    }

    static ResourceLocation dimDe(Level level) {
        return level.dimension().location();
    }

    static ResourceLocation dimDe(LevelAccessor level) {
        if (level instanceof Level l) return l.dimension().location();
        return null;
    }

    static ResourceLocation dimDe(Entity entity) {
        return entity.level().dimension().location();
    }
}
