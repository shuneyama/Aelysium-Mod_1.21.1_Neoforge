package net.aelysium.aelysiummod.jade;

import net.aelysium.aelysiummod.time.TimeCorGerenciador;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadeIntegration implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Registra o provider para jogadores
        registration.registerEntityComponent(
                PlayerNameHider.INSTANCE,
                Player.class
        );
    }

    public enum PlayerNameHider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof Player player) {
                PlayerTeam team = player.getTeam();

                if (team != null && TimeCorGerenciador.isTeamHidden(team.getName())) {
                    // Remove o nome original e substitui por "???"
                    tooltip.clear();
                    tooltip.add(Component.literal("???")
                            .withStyle(ChatFormatting.OBFUSCATED)
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath("aelysiummod", "player_name_hider");
        }
    }
}