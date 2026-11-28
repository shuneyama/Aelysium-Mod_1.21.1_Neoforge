package net.aelysium.aelysiummod.sistemas.chefes.rede;

import net.aelysium.aelysiummod.sistemas.chefes.cliente.TratadorPacotesChefes;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.llibrary.rede.AnimationMessage;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede.MessageFreezeEffect;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede.MessageLinkEntities;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.rede.MessageUpdateBossBar;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RedeChefes {

    private RedeChefes() {
    }

    public static void registrar(PayloadRegistrar registrar) {
        registrar.playToClient(PacoteCuraFlor.TIPO, PacoteCuraFlor.CODEC, (p, c) -> c.enqueueWork(() -> TratadorPacotesChefes.curaFlor(p)));
        registrar.playToClient(PacoteColocarFlor.TIPO, PacoteColocarFlor.CODEC, (p, c) -> c.enqueueWork(() -> TratadorPacotesChefes.colocarFlor(p)));
        registrar.playToClient(PacoteEspinhosFlor.TIPO, PacoteEspinhosFlor.CODEC, (p, c) -> c.enqueueWork(() -> TratadorPacotesChefes.espinhosFlor(p)));
        registrar.playToServer(PacoteAtaqueParte.TIPO, PacoteAtaqueParte.CODEC, (p, c) -> c.enqueueWork(() -> PacoteAtaqueParte.tratar(p, c)));
        registrar.playToClient(AnimationMessage.TYPE, AnimationMessage.STREAM_CODEC, AnimationMessage::handleClient);
        registrar.playToClient(MessageFreezeEffect.TYPE, MessageFreezeEffect.STREAM_CODEC, MessageFreezeEffect::handleClient);
        registrar.playToClient(MessageLinkEntities.TYPE, MessageLinkEntities.STREAM_CODEC, MessageLinkEntities::handleClient);
        registrar.playToClient(MessageUpdateBossBar.TYPE, MessageUpdateBossBar.STREAM_CODEC, MessageUpdateBossBar::handleClient);
    }

    public static void enviarPerto(CustomPacketPayload pacote, ServerLevel nivel, Vec3 posicao) {
        PacketDistributor.sendToPlayersTrackingChunk(nivel, new ChunkPos(net.minecraft.core.BlockPos.containing(posicao)), pacote);
    }
}
