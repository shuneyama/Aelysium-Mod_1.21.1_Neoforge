package net.aelysium.aelysiummod.npc.client;

import net.aelysium.aelysiummod.menu.gui.NpcTradesTab;
import net.aelysium.aelysiummod.npc.client.skin.NpcSkinManager;
import net.aelysium.aelysiummod.npc.entity.CustomNpcEntity;
import net.aelysium.aelysiummod.npc.entity.NpcTradeData;
import net.aelysium.aelysiummod.npc.network.NpcPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NpcClientHandlers {

    public static void handleOpenEditor(NpcPackets.OpenNpcEditorS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(packet.entityId());
            if (entity instanceof CustomNpcEntity npc) {
                mc.setScreen(new NpcTradesTab(npc, packet.npcData(), null));
            }
        });
    }

    public static void handleSyncSkin(NpcPackets.SyncNpcSkinS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            System.out.println("[NPC-Client] handleSyncSkin received:");
            System.out.println("[NPC-Client]   entityId="  + packet.entityId());
            System.out.println("[NPC-Client]   skinMode="  + packet.skinMode());
            System.out.println("[NPC-Client]   modelType=" + packet.modelType());
            System.out.println("[NPC-Client]   dataSize="  +
                    (packet.skinBytes() != null ? packet.skinBytes().length : "null"));

            NpcSkinManager.registerSkin(
                    packet.entityId(), packet.skinBytes(),
                    "vanilla", packet.modelType());

            System.out.println("[NPC-Client]   Skin registered. hasSkin="
                    + NpcSkinManager.hasSkin(packet.entityId()));
        });
    }

    public static void handleSyncTrades(NpcPackets.SyncNpcTradesS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(packet.entityId());
            if (entity instanceof CustomNpcEntity npc) {
                ListTag tradesTag = packet.tradesData().getList("Trades", 10);
                List<NpcTradeData> trades = new ArrayList<>();
                for (int i = 0; i < tradesTag.size(); i++)
                    trades.add(NpcTradeData.load(tradesTag.getCompound(i),
                            mc.level.registryAccess()));
                npc.setTrades(trades);
            }
        });
    }
}