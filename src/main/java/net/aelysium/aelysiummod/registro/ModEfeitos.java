package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.aelysium.aelysiummod.conteudo.efeito.ArmaduraRachadaEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CaecitasEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CegueiraAbissalEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.ChoqueEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CigarroAbstinenciaEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CigarroAlivioEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CigarroCoracoesEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CigarroDanoEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.CongelamentoEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.ExaustaoAbismoEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.GenesisCarregadoEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.MorteCanonicaEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.MorteCanonicaEventoEfeito;
import net.aelysium.aelysiummod.conteudo.efeito.TotemEsgotadoEfeito;

public class ModEfeitos {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, AelysiumMod.MOD_ID);

    public static final Holder<MobEffect> CONGELAMENTO =
            MOB_EFFECTS.register("congelamento",
                    () -> new CongelamentoEfeito(MobEffectCategory.NEUTRAL, 0x00D8FF));

    public static final DeferredHolder<MobEffect, CegueiraAbissalEfeito> CEGUEIRA_ABISSAL =
            MOB_EFFECTS.register("cegueira_abissal", CegueiraAbissalEfeito::new);

    public static final DeferredHolder<MobEffect, CaecitasEfeito> CAECITAS =
            MOB_EFFECTS.register("caecitas", CaecitasEfeito::new);

    public static final DeferredHolder<MobEffect, CigarroDanoEfeito> CIGARRO_DANO =
            MOB_EFFECTS.register("cansaco", CigarroDanoEfeito::new);

    public static final DeferredHolder<MobEffect, CigarroCoracoesEfeito> CIGARRO_CORACOES =
            MOB_EFFECTS.register("vigor", CigarroCoracoesEfeito::new);

    public static final DeferredHolder<MobEffect, CigarroAbstinenciaEfeito> CIGARRO_ABSTINENCIA =
            MOB_EFFECTS.register("abstinencia", CigarroAbstinenciaEfeito::new);

    public static final DeferredHolder<MobEffect, CigarroAlivioEfeito> CIGARRO_ALIVIO =
            MOB_EFFECTS.register("alivio", CigarroAlivioEfeito::new);

    public static final DeferredHolder<MobEffect, MorteCanonicaEfeito> MORTE_CANONICA =
            MOB_EFFECTS.register("morte_canonica", MorteCanonicaEfeito::new);

    public static final DeferredHolder<MobEffect, MorteCanonicaEventoEfeito> MORTE_CANONICA_EVENTO =
            MOB_EFFECTS.register("morte_canonica_evento", MorteCanonicaEventoEfeito::new);

    public static final DeferredHolder<MobEffect, TotemEsgotadoEfeito> TOTEM_ESGOTADO =
            MOB_EFFECTS.register("totem_esgotado", TotemEsgotadoEfeito::new);

    public static final DeferredHolder<MobEffect, ChoqueEfeito> CHOQUE =
            MOB_EFFECTS.register("choque", ChoqueEfeito::new);

    public static final DeferredHolder<MobEffect, GenesisCarregadoEfeito> GENESIS_CARREGADO =
            MOB_EFFECTS.register("genesis_carregado", GenesisCarregadoEfeito::new);

    public static final DeferredHolder<MobEffect, ArmaduraRachadaEfeito> ARMADURA_RACHADA =
            MOB_EFFECTS.register("armadura_rachada", ArmaduraRachadaEfeito::new);

    public static final DeferredHolder<MobEffect, ExaustaoAbismoEfeito> EXAUSTAO_ABISMO =
            MOB_EFFECTS.register("exaustao_abismo", ExaustaoAbismoEfeito::new);

    public static final DeferredHolder<MobEffect, net.aelysium.aelysiummod.conteudo.efeito.CalorEfeito> CALOR =
            MOB_EFFECTS.register("calor", net.aelysium.aelysiummod.conteudo.efeito.CalorEfeito::new);
    public static final DeferredHolder<MobEffect, net.aelysium.aelysiummod.conteudo.efeito.FrioEfeito> FRIO =
            MOB_EFFECTS.register("frio", net.aelysium.aelysiummod.conteudo.efeito.FrioEfeito::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}



