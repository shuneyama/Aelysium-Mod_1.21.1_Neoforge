package net.aelysium.aelysiummod.sistemas.mundo.jardimpalido;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SonsJardimPalido {

    public static final DeferredRegister<SoundEvent> SONS = DeferredRegister.create(Registries.SOUND_EVENT, AelysiumMod.MOD_ID);

    private static DeferredHolder<SoundEvent, SoundEvent> som(String nome) {
        return SONS.register(nome, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, nome)));
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_AMBIENTE = som("entity.creaking.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_ATAQUE = som("entity.creaking.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_MORTE = som("entity.creaking.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_CONGELAR = som("entity.creaking.freeze");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_DESCONGELAR = som("entity.creaking.unfreeze");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_NASCER = som("entity.creaking.spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_PASSO = som("entity.creaking.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_BALANCAR = som("entity.creaking.sway");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_TORCER = som("entity.creaking.twitch");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_ATIVAR = som("entity.creaking.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAKING_DESATIVAR = som("entity.creaking.deactivate");

    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_QUEBRAR = som("block.creaking_heart.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_CAIR = som("block.creaking_heart.fall");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_ACERTAR = som("block.creaking_heart.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_FERIDO = som("block.creaking_heart.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_OCIOSO = som("block.creaking_heart.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_COLOCAR = som("block.creaking_heart.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_PASSO = som("block.creaking_heart.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CORACAO_GERAR = som("block.creaking_heart.spawn");

    public static final SoundType TIPO_CORACAO = new DeferredSoundType(1.0F, 1.0F, CORACAO_QUEBRAR, CORACAO_PASSO, CORACAO_COLOCAR, CORACAO_ACERTAR, CORACAO_CAIR);

    private SonsJardimPalido() {
    }

    public static void registrar(IEventBus barramento) {
        SONS.register(barramento);
    }
}
