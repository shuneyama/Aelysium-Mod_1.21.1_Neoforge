package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;

public class VarredorMobsRegiao {

    private static final int INTERVALO_TICKS = 20;

    private int contador = 0;

    @SubscribeEvent
    public void aoTickServidor(ServerTickEvent.Post event) {
        contador++;
        if (contador < INTERVALO_TICKS) return;
        contador = 0;

        MinecraftServer servidor = event.getServer();

        for (Regiao regiao : GerenciadorRegioes.getInstance().getTodasRegioes()) {
            boolean bloquearMonstros = !regiao.getFlagValor(FlagRegiao.SPAWN_MONSTROS);
            boolean bloquearAnimais = !regiao.getFlagValor(FlagRegiao.SPAWN_ANIMAIS);
            if (!bloquearMonstros && !bloquearAnimais) continue;

            ServerLevel nivel = servidor.getLevel(ResourceKey.create(Registries.DIMENSION, regiao.getDimensao()));
            if (nivel == null) continue;

            AABB area = new AABB(
                    regiao.getPosicaoMinima().getX(), regiao.getPosicaoMinima().getY(), regiao.getPosicaoMinima().getZ(),
                    regiao.getPosicaoMaxima().getX() + 1, regiao.getPosicaoMaxima().getY() + 1, regiao.getPosicaoMaxima().getZ() + 1
            );

            List<Mob> mobs = nivel.getEntitiesOfClass(Mob.class, area);
            for (Mob mob : mobs) {
                if (mob instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) continue;

                MobCategory categoria = mob.getType().getCategory();
                boolean eMonstro = categoria == MobCategory.MONSTER;
                boolean eAnimal = categoria == MobCategory.CREATURE || categoria == MobCategory.WATER_CREATURE
                        || categoria == MobCategory.AMBIENT || categoria == MobCategory.AXOLOTLS;

                if ((eMonstro && bloquearMonstros) || (eAnimal && bloquearAnimais)) {
                    mob.discard();
                }
            }
        }
    }
}
