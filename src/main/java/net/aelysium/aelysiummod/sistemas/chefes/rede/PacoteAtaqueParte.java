package net.aelysium.aelysiummod.sistemas.chefes.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.chefes.base.multiparte.entidade.MultipartAwareEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacoteAtaqueParte(int entidade, String parte, InteractionHand mao, boolean agachado, boolean ataque) implements CustomPacketPayload {

    private static final double ALCANCE_EXTRA = 64.0;

    public static final Type<PacoteAtaqueParte> TIPO = new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "chefe_ataque_parte"));
    public static final StreamCodec<FriendlyByteBuf, PacoteAtaqueParte> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.entidade);
                buf.writeUtf(p.parte, 64);
                buf.writeEnum(p.mao);
                buf.writeBoolean(p.agachado);
                buf.writeBoolean(p.ataque);
            },
            buf -> new PacoteAtaqueParte(buf.readVarInt(), buf.readUtf(64), buf.readEnum(InteractionHand.class), buf.readBoolean(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TIPO;
    }

    public static void tratar(PacoteAtaqueParte pacote, IPayloadContext contexto) {
        if (!(contexto.player() instanceof ServerPlayer jogador)) return;
        Entity alvo = jogador.serverLevel().getEntity(pacote.entidade);
        if (!(alvo instanceof MultipartAwareEntity multiparte)) return;
        if (jogador.distanceToSqr(alvo) > ALCANCE_EXTRA * ALCANCE_EXTRA) return;
        jogador.setShiftKeyDown(pacote.agachado);
        if (pacote.ataque) {
            multiparte.setNextDamagedPart(pacote.parte);
            jogador.attack(alvo);
        } else {
            alvo.interact(jogador, pacote.mao);
        }
    }
}
