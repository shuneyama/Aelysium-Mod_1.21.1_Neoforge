package net.aelysium.aelysiummod.compatibilidade.jade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.filhotes.aquatico.IdadeAquatica;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CrescimentoAquaticoJade implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final String CHAVE = "AelysiumCrescimentoAquatico";

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag dados = accessor.getServerData();
        if (!dados.contains(CHAVE)) return;
        int segundos = Math.max(0, dados.getInt(CHAVE) / 20);
        String tempo = String.format("%d:%02d", segundos / 60, segundos % 60);
        tooltip.add(Component.translatable("jade.mobgrowth.time", tempo));
    }

    @Override
    public void appendServerData(CompoundTag dados, EntityAccessor accessor) {
        if (!IdadeAquatica.ehFilhote(accessor.getEntity())) return;
        dados.putInt(CHAVE, accessor.getEntity().getPersistentData().getInt(IdadeAquatica.CHAVE));
    }

    @Override
    public boolean shouldRequestData(EntityAccessor accessor) {
        return IdadeAquatica.ehFilhote(accessor.getEntity());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "crescimento_aquatico");
    }
}
