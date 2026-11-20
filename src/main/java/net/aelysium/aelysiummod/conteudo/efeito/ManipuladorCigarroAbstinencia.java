package net.aelysium.aelysiummod.conteudo.efeito;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.item.cigarro.CigarroItem;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.aelysium.aelysiummod.registro.ModEfeitos;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorCigarroAbstinencia {

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof Player player)) return;
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        reagir(player, instance.getEffect());
    }

    @SubscribeEvent
    public static void onRemove(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof Player player)) return;
        reagir(player, event.getEffect());
    }

    private static void reagir(Player player, Holder<MobEffect> effect) {
        MobEffect valor = effect.value();

        if (valor instanceof CigarroAlivioEfeito) {
            if (player.hasEffect(ModEfeitos.CIGARRO_ABSTINENCIA)) {
                CigarroItem.aplicarClampAbstinencia(player);
            }
        } else if (valor instanceof CigarroAbstinenciaEfeito) {
            CigarroItem.removerClampAbstinencia(player);
        }
    }
}
