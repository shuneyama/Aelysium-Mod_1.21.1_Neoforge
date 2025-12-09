package net.aelysium.aelysiummod.jade;

import io.netty.buffer.ByteBuf;
import net.aelysium.aelysiummod.time.TimeCorGerenciador;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record HiddenTeamSyncPacket(Set<String> hiddenTeams) implements CustomPacketPayload {

    public static final Type<HiddenTeamSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("aelysiummod", "hidden_team_sync"));

    public static final StreamCodec<ByteBuf, HiddenTeamSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
            HiddenTeamSyncPacket::hiddenTeams,
            HiddenTeamSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HiddenTeamSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Sincroniza os times ocultos do servidor para o cliente
            // Primeiro limpa os times ocultos antigos, depois adiciona os novos
            TimeCorGerenciador.getHiddenTeams().forEach(teamName ->
                    TimeCorGerenciador.setTeamHidden(teamName, false)
            );
            packet.hiddenTeams().forEach(teamName ->
                    TimeCorGerenciador.setTeamHidden(teamName, true)
            );
        });
    }
}