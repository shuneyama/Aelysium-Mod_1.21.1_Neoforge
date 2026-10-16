package net.aelysium.aelysiummod.compatibilidade.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class IntegracaoJade implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(
                OcultadorNomeJogador.INSTANCE,
                Player.class
        );
    }

    public enum OcultadorNomeJogador implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof Player) {
                Player viewer = accessor.getPlayer();


                boolean viewerHasOp = viewer != null && GerenciadorJogadoresOp.isOp(viewer.getUUID());

                if (!viewerHasOp) {
                    tooltip.clear();
                    tooltip.add(Component.literal("???")
                            .withStyle(ChatFormatting.OBFUSCATED)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath("aelysiummod", "internal_hidden");
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public int getDefaultPriority() {
            return Integer.MAX_VALUE;
        }
    }
}