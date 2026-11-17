package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class InstrumentosCobre {

    private static final Holder<SoundEvent> TROMPETE = som("trumpet");
    private static final Holder<SoundEvent> TROMPETE_EXPOSTO = som("trumpet_exposed");
    private static final Holder<SoundEvent> TROMPETE_ENVELHECIDO = som("trumpet_weathered");
    private static final Holder<SoundEvent> TROMPETE_OXIDADO = som("trumpet_oxidized");

    private InstrumentosCobre() {
    }

    private static Holder<SoundEvent> som(String nome) {
        return Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "block.note_block." + nome)));
    }

    public static Holder<SoundEvent> para(Block bloco) {
        if (bloco == Blocks.COPPER_BLOCK || bloco == Blocks.WAXED_COPPER_BLOCK) return TROMPETE;
        if (bloco == Blocks.EXPOSED_COPPER || bloco == Blocks.WAXED_EXPOSED_COPPER) return TROMPETE_EXPOSTO;
        if (bloco == Blocks.WEATHERED_COPPER || bloco == Blocks.WAXED_WEATHERED_COPPER) return TROMPETE_ENVELHECIDO;
        if (bloco == Blocks.OXIDIZED_COPPER || bloco == Blocks.WAXED_OXIDIZED_COPPER) return TROMPETE_OXIDADO;
        return null;
    }
}
