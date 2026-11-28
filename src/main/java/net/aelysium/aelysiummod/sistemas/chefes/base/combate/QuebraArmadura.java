package net.aelysium.aelysiummod.sistemas.chefes.base.combate;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

public final class QuebraArmadura {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "quebra_armadura_chefe");
    private static final Map<LivingEntity, Estado> ESTADOS = new WeakHashMap<>();

    private static final class Estado {
        private final ArrayDeque<Long> fins = new ArrayDeque<>();
        private long liberado;
    }

    private QuebraArmadura() {
    }

    public static void aplicar(LivingEntity alvo) {
        int maximo = ConfiguracaoChefes.inteiro(ConfiguracaoChefes.QUEBRA + ".pontos_maximos");
        int porGolpe = ConfiguracaoChefes.inteiro(ConfiguracaoChefes.QUEBRA + ".pontos_por_golpe");
        if (maximo <= 0 || porGolpe <= 0) return;

        long agora = alvo.level().getGameTime();
        Estado estado = ESTADOS.computeIfAbsent(alvo, chave -> new Estado());
        if (agora < estado.liberado) return;

        long fim = agora + Math.max(ConfiguracaoChefes.inteiro(ConfiguracaoChefes.QUEBRA + ".duracao_segundos"), 1) * 20L;
        for (int i = 0; i < porGolpe; i++) {
            if (estado.fins.size() >= maximo) estado.fins.pollFirst();
            estado.fins.addLast(fim);
        }
        estado.liberado = agora + Math.round(ConfiguracaoChefes.numero(ConfiguracaoChefes.QUEBRA + ".intervalo_minimo_segundos") * 20);
        atualizar(alvo, estado.fins.size());
        alvo.level().playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.7F, 0.8F);
    }

    public static void tick() {
        if (ESTADOS.isEmpty()) return;
        Iterator<Map.Entry<LivingEntity, Estado>> iterador = ESTADOS.entrySet().iterator();
        while (iterador.hasNext()) {
            Map.Entry<LivingEntity, Estado> entrada = iterador.next();
            LivingEntity alvo = entrada.getKey();
            Estado estado = entrada.getValue();
            if (alvo == null || alvo.isRemoved()) {
                iterador.remove();
                continue;
            }
            long agora = alvo.level().getGameTime();
            int antes = estado.fins.size();
            while (!estado.fins.isEmpty() && estado.fins.peekFirst() <= agora) estado.fins.pollFirst();
            if (estado.fins.size() != antes) atualizar(alvo, estado.fins.size());
            if (estado.fins.isEmpty() && agora >= estado.liberado) iterador.remove();
        }
    }

    private static void atualizar(LivingEntity alvo, int pontos) {
        AttributeInstance armadura = alvo.getAttribute(Attributes.ARMOR);
        if (armadura == null) return;
        if (pontos <= 0) armadura.removeModifier(ID);
        else armadura.addOrUpdateTransientModifier(new AttributeModifier(ID, -pontos, AttributeModifier.Operation.ADD_VALUE));
    }
}
