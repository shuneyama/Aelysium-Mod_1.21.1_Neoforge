package net.aelysium.aelysiummod.compatibilidade.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.compatibilidade.ItensVisiveisSemOp;

@EmiEntrypoint
public class PluginAelysiumEmi implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        if (!PonteAelysiumEmi.guardOk()) {
            registry.removeEmiStacks(stack ->
                    AelysiumMod.MOD_ID.equals(stack.getId().getNamespace()));
            return;
        }

        if (PonteAelysiumEmi.isOp()) return;

        registry.removeEmiStacks(stack ->
                AelysiumMod.MOD_ID.equals(stack.getId().getNamespace())
                        && !ItensVisiveisSemOp.estaLiberado(stack.getId().getPath()));
    }
}
