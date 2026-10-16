package net.aelysium.aelysiummod.sistemas.mundo.dificuldade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public class ManipuladorGolpesDificuldade {

    private static final ResourceLocation ID_CORROSAO_ARMADURA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "dificuldade_corrosao_armadura");
    private static final ResourceLocation ID_CORROSAO_DUREZA = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "dificuldade_corrosao_dureza");

    private static final Map<UUID, Long> PROXIMO_GOLPE_ESPECIAL = new HashMap<>();
    private static final Map<UUID, Long> FIM_CORROSAO = new HashMap<>();

    private enum Especial { CRITICO, EFEITO, CORROSAO, DESARME }

    @SubscribeEvent
    public static void aoReceberDano(LivingIncomingDamageEvent evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jogador)) return;
        if (jogador.isCreative() || jogador.isSpectator()) return;
        if (!(evento.getSource().getEntity() instanceof Mob atacante)) return;

        int nivelGlobal = DadosDificuldade.get(jogador.server).getNivel();
        if (nivelGlobal <= 0) return;

        int nivel = AplicadorDificuldade.nivelAplicado(atacante);
        if (nivel <= 0) return;

        if (evento.getSource().getDirectEntity() != atacante) {
            evento.setAmount((float) (evento.getAmount() * (1.0 + PerfilDificuldade.bonusDanoProjetil(nivel))));
        }

        double penetracao = PerfilDificuldade.penetracaoArmadura(nivel);
        evento.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reducao) -> (float) (reducao * (1.0 - penetracao)));

        if (GerenciadorFantasmasDificuldade.golpeVeioDeFantasma(atacante)
                && ControleVooDificuldade.estaVoando(jogador)
                && jogador.getRandom().nextDouble() < PerfilDificuldade.chanceCancelarVoo()) {
            ControleVooDificuldade.bloquear(jogador, PerfilDificuldade.duracaoCancelamentoVooTicks());
        }

        sortearGolpeEspecial(evento, jogador, nivel);
    }

    @SubscribeEvent
    public static void aoTickJogador(PlayerTickEvent.Post evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jogador)) return;

        ControleVooDificuldade.aoTick(jogador);

        Long fim = FIM_CORROSAO.get(jogador.getUUID());
        if (fim != null && jogador.level().getGameTime() >= fim) {
            removerCorrosao(jogador);
        }

        if (jogador.tickCount % 2 == 0) GerenciadorFantasmasDificuldade.guiarFantasmas(jogador);

        if (jogador.tickCount % 40 == 0) {
            int nivel = DadosDificuldade.get(jogador.server).getNivel();
            if (nivel > 0) GerenciadorFantasmasDificuldade.aoTickJogador(jogador, nivel);
        }
    }

    @SubscribeEvent
    public static void aoSairDoMundo(PlayerEvent.PlayerLoggedOutEvent evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jogador)) return;
        removerCorrosao(jogador);
        PROXIMO_GOLPE_ESPECIAL.remove(jogador.getUUID());
        ControleVooDificuldade.limpar(jogador.getUUID());
        GerenciadorFantasmasDificuldade.limpar(jogador.getUUID());
    }

    private static void sortearGolpeEspecial(LivingIncomingDamageEvent evento, ServerPlayer jogador, int nivel) {
        long agora = jogador.level().getGameTime();
        Long proximo = PROXIMO_GOLPE_ESPECIAL.get(jogador.getUUID());
        if (proximo != null && agora < proximo) return;
        if (jogador.getRandom().nextDouble() >= PerfilDificuldade.chanceGolpeEspecial(nivel)) return;

        List<Especial> sorteio = new ArrayList<>();
        adicionarPeso(sorteio, Especial.CRITICO, PerfilDificuldade.pesoCritico());
        adicionarPeso(sorteio, Especial.EFEITO, PerfilDificuldade.pesoEfeito());
        if (nivel >= PerfilDificuldade.nivelMinimoCorrosao()) adicionarPeso(sorteio, Especial.CORROSAO, PerfilDificuldade.pesoCorrosao());
        if (nivel >= PerfilDificuldade.nivelMinimoDesarme() && possuiItemNasMaos(jogador)) adicionarPeso(sorteio, Especial.DESARME, PerfilDificuldade.pesoDesarme());
        if (sorteio.isEmpty()) return;

        PROXIMO_GOLPE_ESPECIAL.put(jogador.getUUID(), agora + PerfilDificuldade.recargaGolpeEspecialTicks(nivel));

        switch (sorteio.get(jogador.getRandom().nextInt(sorteio.size()))) {
            case CRITICO -> aplicarCritico(evento, jogador, nivel);
            case EFEITO -> aplicarEfeito(jogador, nivel);
            case CORROSAO -> aplicarCorrosao(jogador, nivel);
            case DESARME -> aplicarDesarme(jogador);
        }
    }

    private static void adicionarPeso(List<Especial> lista, Especial especial, int peso) {
        for (int i = 0; i < peso; i++) lista.add(especial);
    }

    private static boolean possuiItemNasMaos(ServerPlayer jogador) {
        return !jogador.getMainHandItem().isEmpty() || !jogador.getOffhandItem().isEmpty();
    }

    private static void aplicarCritico(LivingIncomingDamageEvent evento, ServerPlayer jogador, int nivel) {
        evento.setAmount(evento.getAmount() * PerfilDificuldade.multiplicadorGolpeCritico(nivel));
    }

    private static void aplicarEfeito(ServerPlayer jogador, int nivel) {
        List<Holder<MobEffect>> efeitos = new ArrayList<>();
        if (nivel >= PerfilDificuldade.nivelMinimoFraqueza()) efeitos.add(MobEffects.WEAKNESS);
        if (nivel >= PerfilDificuldade.nivelMinimoLentidao()) efeitos.add(MobEffects.MOVEMENT_SLOWDOWN);
        if (nivel >= PerfilDificuldade.nivelMinimoFadiga()) efeitos.add(MobEffects.DIG_SLOWDOWN);
        if (nivel >= PerfilDificuldade.nivelMinimoFome()) efeitos.add(MobEffects.HUNGER);
        if (nivel >= PerfilDificuldade.nivelMinimoPoison()) efeitos.add(MobEffects.POISON);
        if (nivel >= PerfilDificuldade.nivelMinimoEscuridao()) efeitos.add(MobEffects.DARKNESS);
        if (nivel >= PerfilDificuldade.nivelMinimoCegueira()) efeitos.add(MobEffects.BLINDNESS);
        if (nivel >= PerfilDificuldade.nivelMinimoWither()) efeitos.add(MobEffects.WITHER);

        if (efeitos.isEmpty()) return;

        Holder<MobEffect> escolhido = efeitos.get(jogador.getRandom().nextInt(efeitos.size()));
        int duracao = PerfilDificuldade.duracaoEfeitoTicks(nivel);
        boolean curto = escolhido.equals(MobEffects.BLINDNESS) || escolhido.equals(MobEffects.WITHER) || escolhido.equals(MobEffects.POISON);
        if (curto) duracao = Math.min(duracao, PerfilDificuldade.duracaoMaximaEfeitoPerigosoTicks());
        int amplificador = curto || escolhido.equals(MobEffects.DARKNESS) || escolhido.equals(MobEffects.HUNGER) ? 0 : PerfilDificuldade.amplificadorEfeito(nivel);

        jogador.addEffect(new MobEffectInstance(escolhido, duracao, amplificador));
    }

    private static void aplicarCorrosao(ServerPlayer jogador, int nivel) {
        removerModificadores(jogador);
        AttributeInstance armadura = jogador.getAttribute(Attributes.ARMOR);
        AttributeInstance dureza = jogador.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armadura != null) {
            armadura.addTransientModifier(new AttributeModifier(ID_CORROSAO_ARMADURA, -PerfilDificuldade.reducaoArmaduraJogador(nivel), AttributeModifier.Operation.ADD_VALUE));
        }
        if (dureza != null) {
            dureza.addTransientModifier(new AttributeModifier(ID_CORROSAO_DUREZA, -PerfilDificuldade.reducaoDurezaJogador(nivel), AttributeModifier.Operation.ADD_VALUE));
        }
        FIM_CORROSAO.put(jogador.getUUID(), jogador.level().getGameTime() + PerfilDificuldade.duracaoCorrosaoTicks(nivel));
    }

    private static void aplicarDesarme(ServerPlayer jogador) {
        List<EquipmentSlot> slots = new ArrayList<>();
        if (!jogador.getMainHandItem().isEmpty()) slots.add(EquipmentSlot.MAINHAND);
        if (!jogador.getOffhandItem().isEmpty()) slots.add(EquipmentSlot.OFFHAND);
        if (slots.isEmpty()) return;

        EquipmentSlot slot = slots.get(jogador.getRandom().nextInt(slots.size()));
        ItemStack item = jogador.getItemBySlot(slot).copy();
        jogador.setItemSlot(slot, ItemStack.EMPTY);
        ItemEntity entidade = jogador.drop(item, true);
        if (entidade != null) entidade.setPickUpDelay(PerfilDificuldade.tempoPickupAposDesarmarTicks());
    }

    private static void removerCorrosao(ServerPlayer jogador) {
        FIM_CORROSAO.remove(jogador.getUUID());
        removerModificadores(jogador);
    }

    private static void removerModificadores(ServerPlayer jogador) {
        AttributeInstance armadura = jogador.getAttribute(Attributes.ARMOR);
        AttributeInstance dureza = jogador.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armadura != null) armadura.removeModifier(ID_CORROSAO_ARMADURA);
        if (dureza != null) dureza.removeModifier(ID_CORROSAO_DUREZA);
    }
}
