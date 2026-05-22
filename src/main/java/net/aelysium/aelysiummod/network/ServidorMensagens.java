package net.aelysium.aelysiummod.network;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.banlist.network.BanlistNetwork;
import net.aelysium.aelysiummod.banlist.network.ClientBanData;
import net.aelysium.aelysiummod.client.LuaClientRenderer;
import net.aelysium.aelysiummod.jade.OpPlayersSyncPacket;
import net.aelysium.aelysiummod.holograma.network.HologramaPackets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ServidorMensagens {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AelysiumMod.MOD_ID).versioned("1").optional();

        registrar.playToClient(OpPlayersSyncPacket.TYPE, OpPlayersSyncPacket.STREAM_CODEC, OpPlayersSyncPacket::handle);

        registrar.playToClient(BanlistNetwork.SyncBannedItemsPacket.TYPE, BanlistNetwork.SyncBannedItemsPacket.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientBanData.setBannedItems(payload.bannedItems())));

        registrar.playToClient(BanlistNetwork.SyncAllowedItemsPacket.TYPE, BanlistNetwork.SyncAllowedItemsPacket.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientBanData.setAllowedItems(payload.allowedItems())));

        registrar.playToClient(AelysiumNetwork.BalloonPacket.TYPE, AelysiumNetwork.BalloonPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.network.ClientOnlyHandlers");
                        h.getMethod("handleBalloonPacket", AelysiumNetwork.BalloonPacket.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(AelysiumNetwork.OpenBalloonGuiPacket.TYPE, AelysiumNetwork.OpenBalloonGuiPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.network.ClientOnlyHandlers");
                        h.getMethod("handleOpenBalloonGui", AelysiumNetwork.OpenBalloonGuiPacket.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(AelysiumNetwork.BalloonConfigPacket.TYPE, AelysiumNetwork.BalloonConfigPacket.STREAM_CODEC, AelysiumNetwork::handleBalloonConfig);

        registrar.playToClient(AelysiumNetwork.LuaSyncPacket.TYPE, AelysiumNetwork.LuaSyncPacket.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> LuaClientRenderer.luaCliente = payload.tipoLua()));

        registrar.playToClient(AelysiumNetwork.NickSyncPacket.TYPE, AelysiumNetwork.NickSyncPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.network.ClientOnlyHandlers");
                        h.getMethod("handleNickSync", AelysiumNetwork.NickSyncPacket.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(AelysiumNetwork.NickEditorOpenPacket.TYPE, AelysiumNetwork.NickEditorOpenPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.network.ClientOnlyHandlers");
                        h.getMethod("handleNickEditorOpen", AelysiumNetwork.NickEditorOpenPacket.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToServer(AelysiumNetwork.NickEditorSavePacket.TYPE, AelysiumNetwork.NickEditorSavePacket.STREAM_CODEC, AelysiumNetwork::handleNickEditorSave);

        registrar.playToClient(ValkyriaFlightPacket.TYPE, ValkyriaFlightPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.network.ClientOnlyHandlers");
                        h.getMethod("handleValkyriaFlight", ValkyriaFlightPacket.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.npc.network.NpcPackets.OpenNpcEditorS2C.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.OpenNpcEditorS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.npc.client.NpcClientHandlers");
                        h.getMethod("handleOpenEditor",
                                net.aelysium.aelysiummod.npc.network.NpcPackets.OpenNpcEditorS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.npc.network.NpcPackets.SyncNpcSkinS2C.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.SyncNpcSkinS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.npc.client.NpcClientHandlers");
                        h.getMethod("handleSyncSkin",
                                net.aelysium.aelysiummod.npc.network.NpcPackets.SyncNpcSkinS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.npc.network.NpcPackets.SyncNpcTradesS2C.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.SyncNpcTradesS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.npc.client.NpcClientHandlers");
                        h.getMethod("handleSyncTrades",
                                net.aelysium.aelysiummod.npc.network.NpcPackets.SyncNpcTradesS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(FormaDivinaPacket.TYPE, FormaDivinaPacket.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.client.FormaDivinaCache");
                        h.getMethod("set", java.util.UUID.class, boolean.class, String.class, int.class)
                                .invoke(null, payload.uuid(), payload.ativa(), payload.deusId(), payload.cor());
                    } catch (Exception e) { throw new RuntimeException(e); }
                }));

        registrar.playToServer(FormaDivinaActivatePacket.TYPE, FormaDivinaActivatePacket.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer j)
                        net.aelysium.aelysiummod.deus.FormaDivina.tentar(j);
                }));

        registrar.playToServer(VanishActivatePacket.TYPE, VanishActivatePacket.STREAM_CODEC,
                (pkt, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        net.aelysium.aelysiummod.deus.DeusData data =
                                net.aelysium.aelysiummod.deus.DeusData.get(player.serverLevel());
                        net.aelysium.aelysiummod.deus.DeusType deus = data.getDeus(player.getUUID());
                        if (deus != net.aelysium.aelysiummod.deus.DeusType.NONE) {
                            net.aelysium.aelysiummod.deus.VanishTransitionHandler.startTransition(player, deus);
                        }
                    }
                }));

        registrar.playToServer(
                net.aelysium.aelysiummod.protecao.network.PacoteCriarRegiao.TYPE,
                net.aelysium.aelysiummod.protecao.network.PacoteCriarRegiao.CODEC,
                net.aelysium.aelysiummod.protecao.network.PacoteCriarRegiao::handle);

        registrar.playToServer(
                net.aelysium.aelysiummod.protecao.network.PacoteEditarRegiao.TYPE,
                net.aelysium.aelysiummod.protecao.network.PacoteEditarRegiao.CODEC,
                net.aelysium.aelysiummod.protecao.network.PacoteEditarRegiao::handle);

        registrar.playToClient(
                net.aelysium.aelysiummod.protecao.network.PacoteAbrirTela.TYPE,
                net.aelysium.aelysiummod.protecao.network.PacoteAbrirTela.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.protecao.network.ProtecaoClientHandlers");
                        h.getMethod("handleAbrirTela",
                                net.aelysium.aelysiummod.protecao.network.PacoteAbrirTela.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.protecao.network.PacoteAbrirTelaEdicao.TYPE,
                net.aelysium.aelysiummod.protecao.network.PacoteAbrirTelaEdicao.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.protecao.network.ProtecaoClientHandlers");
                        h.getMethod("handleAbrirTelaEdicao",
                                net.aelysium.aelysiummod.protecao.network.PacoteAbrirTelaEdicao.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                net.aelysium.aelysiummod.protecao.network.PacoteListaRegioes.TYPE,
                net.aelysium.aelysiummod.protecao.network.PacoteListaRegioes.CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.protecao.network.ProtecaoClientHandlers");
                        h.getMethod("handleListaRegioes",
                                net.aelysium.aelysiummod.protecao.network.PacoteListaRegioes.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                HologramaPackets.SpawnHologramaS2C.TYPE,
                HologramaPackets.SpawnHologramaS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.holograma.network.HologramaClientHandler");
                        h.getMethod("handleSpawn",
                                HologramaPackets.SpawnHologramaS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                HologramaPackets.DestroyHologramaS2C.TYPE,
                HologramaPackets.DestroyHologramaS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.holograma.network.HologramaClientHandler");
                        h.getMethod("handleDestroy",
                                HologramaPackets.DestroyHologramaS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });

        registrar.playToClient(
                HologramaPackets.UpdateHologramaS2C.TYPE,
                HologramaPackets.UpdateHologramaS2C.STREAM_CODEC,
                (payload, ctx) -> {
                    try {
                        Class<?> h = Class.forName("net.aelysium.aelysiummod.holograma.network.HologramaClientHandler");
                        h.getMethod("handleUpdate",
                                HologramaPackets.UpdateHologramaS2C.class,
                                net.neoforged.neoforge.network.handling.IPayloadContext.class
                        ).invoke(null, payload, ctx);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });


        registrar.playToServer(
                net.aelysium.aelysiummod.protecao.network.PacoteDeletarRegiao.TYPE,
                net.aelysium.aelysiummod.protecao.network.PacoteDeletarRegiao.CODEC,
                net.aelysium.aelysiummod.protecao.network.PacoteDeletarRegiao::handle);

        registrar.playToServer(
                net.aelysium.aelysiummod.npc.network.NpcPackets.UpdateNpcTradesC2S.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.UpdateNpcTradesC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.npc.network.NpcPackets::handleUpdateTrades);

        registrar.playToServer(
                net.aelysium.aelysiummod.npc.network.NpcPackets.UpdateNpcSettingsC2S.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.UpdateNpcSettingsC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.npc.network.NpcPackets::handleUpdateSettings);

        registrar.playToServer(
                net.aelysium.aelysiummod.npc.network.NpcPackets.UploadNpcSkinC2S.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.UploadNpcSkinC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.npc.network.NpcPackets::handleUploadSkin);

        registrar.playToServer(
                net.aelysium.aelysiummod.npc.network.NpcPackets.RemoveNpcC2S.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.RemoveNpcC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.npc.network.NpcPackets::handleRemoveNpc);

        registrar.playToServer(
                net.aelysium.aelysiummod.npc.network.NpcPackets.LinkLogBookC2S.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.LinkLogBookC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.npc.network.NpcPackets::handleLinkLogBook);

        registrar.playToServer(
                net.aelysium.aelysiummod.npc.network.NpcPackets.UpdateNpcEquipmentC2S.TYPE,
                net.aelysium.aelysiummod.npc.network.NpcPackets.UpdateNpcEquipmentC2S.STREAM_CODEC,
                net.aelysium.aelysiummod.npc.network.NpcPackets::handleUpdateEquipment);
    }
}