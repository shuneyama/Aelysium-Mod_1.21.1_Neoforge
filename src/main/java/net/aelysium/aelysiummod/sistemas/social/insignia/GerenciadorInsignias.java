package net.aelysium.aelysiummod.sistemas.social.insignia;

import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.PerfilJogador;

public class GerenciadorInsignias {

    public static final ResourceLocation BADGES_FONT =
            ResourceLocation.fromNamespaceAndPath("aelysiummod", "badges");

    public static TipoInsignia getEffectiveBadge(ServerPlayer player) {
        if (player.getServer() == null) return null;

        String override = DadosInsignia.get(player.getServer()).getOverride(player.getUUID());
        if (override != null) {
            if (override.equals(DadosInsignia.NONE)) return null;
            return TipoInsignia.fromId(override);
        }

        if (player.hasPermissions(2)) return null;

        TipoRaca race = PerfilJogador.raca().raca(player.getUUID());
        return TipoInsignia.forRace(race);
    }

    public static MutableComponent getBadgeComponent(ServerPlayer player) {
        if (player.getServer() != null) {
            String substituicao = DadosInsignia.get(player.getServer()).getOverride(player.getUUID());
            net.aelysium.aelysiummod.nucleo.jogador.InsigniaPersonalizada personalizada = GerenciadorInsigniasPersonalizadas.porId(substituicao);
            if (personalizada != null) {
                return Component.literal(personalizada.glifos()).setStyle(Style.EMPTY.withFont(BADGES_FONT).withColor(0xFFFFFF));
            }
        }
        TipoInsignia badge = getEffectiveBadge(player);
        if (badge == null) return null;
        return Component.literal(badge.glyph())
                .setStyle(Style.EMPTY.withFont(BADGES_FONT).withColor(0xFFFFFF));
    }
}
