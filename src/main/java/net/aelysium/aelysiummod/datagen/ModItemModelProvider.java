package net.aelysium.aelysiummod.datagen;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.item.ModItens;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AelysiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItens.CIANE_CRU.get());
        basicItem(ModItens.CIANE_FRAGMENTO.get());
        basicItem(ModItens.CIANE_BARRA.get());

        basicItem(ModItens.CHAVERACHA1.get());
        basicItem(ModItens.CHAVERACHA2.get());
        basicItem(ModItens.CHAVERACHA3.get());
        basicItem(ModItens.CHAVERACHA4.get());
        basicItem(ModItens.CHAVERACHA5.get());
        basicItem(ModItens.CHAVERACHA6.get());
        basicItem(ModItens.CHAVERACHA7.get());
        basicItem(ModItens.CHAVERACHA8.get());

        basicItem(ModItens.ELEMENTO_FOGO.get());
        basicItem(ModItens.ELEMENTO_GELO.get());
        basicItem(ModItens.ELEMENTO_NATUREZA.get());
        basicItem(ModItens.ELEMENTO_RAIO.get());
        basicItem(ModItens.ELEMENTO_SACRA.get());
        basicItem(ModItens.ELEMENTO_SANGUE.get());
        basicItem(ModItens.ELEMENTO_VAZIO.get());

        basicItem(ModItens.CIANE_CAPACETE.get());
        basicItem(ModItens.CIANE_PEITORAL.get());
        basicItem(ModItens.CIANE_CALCINHA.get());
        basicItem(ModItens.CIANE_BOTAS.get());
    }
}