package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import net.aelysium.aelysiummod.sistemas.social.npc.cliente.AbaTrocasNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.cliente.GerenciadorSkinNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.aelysium.aelysiummod.sistemas.social.npc.DadosTrocaNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ManipuladoresNpcCliente {

    public static void handleOpenEditor(PacotesNpc.AbrirEditorNpcS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(packet.entityId());
            if (entity instanceof EntidadeNpcPersonalizado npc) {
                mc.setScreen(new AbaTrocasNpc(npc, packet.npcData(), null));
                return;
            }
            net.minecraft.nbt.CompoundTag dados = packet.npcData();
            if (!dados.contains("NpcUuid")) return;
            EntidadeNpcPersonalizado distante = new EntidadeNpcPersonalizado(
                    mc.level, dados.getDouble("PosX"), dados.getDouble("PosY"), dados.getDouble("PosZ"));
            distante.setId(packet.entityId());
            distante.setUUID(java.util.UUID.fromString(dados.getString("NpcUuid")));
            distante.setNpcName(dados.getString("NpcName"));
            distante.setModelType(dados.getString("ModelType"));
            mc.setScreen(new AbaTrocasNpc(distante, dados, null));
        });
    }

    public static void handleArquivoNpc(PacotesNpc.ArquivoNpcS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            String sugestao = packet.nome().replaceAll("\u00a7.", "").replaceAll("[^A-Za-z0-9_-]", "_");
            if (sugestao.isBlank()) sugestao = "npc";
            final String nomeSugerido = sugestao + ".nbt";
            final byte[] dados = packet.dados();
            new Thread(() -> {
                try {
                    String caminho = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_saveFileDialog(
                            "Salvar NPC", nomeSugerido, null, "NPC (*.nbt)");
                    if (caminho == null) return;
                    if (!caminho.toLowerCase(java.util.Locale.ROOT).endsWith(".nbt")) caminho += ".nbt";
                    java.nio.file.Files.write(java.nio.file.Path.of(caminho), dados);
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player.displayClientMessage(
                                    net.minecraft.network.chat.Component.translatable("gui.aelysiummod.npc.preset_salvo"), true);
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }, "NpcFileSave").start();
        });
    }

    public static void handleSyncSkin(PacotesNpc.SincSkinNpcS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GerenciadorSkinNpc.registerSkin(
                    packet.entityId(), packet.skinBytes(),
                    "vanilla", packet.modelType());
        });
    }

    public static void handleSyncTrades(PacotesNpc.SincTrocasNpcS2C packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(packet.entityId());
            if (entity instanceof EntidadeNpcPersonalizado npc) {
                ListTag tradesTag = packet.tradesData().getList("Trades", 10);
                List<DadosTrocaNpc> trades = new ArrayList<>();
                for (int i = 0; i < tradesTag.size(); i++)
                    trades.add(DadosTrocaNpc.load(tradesTag.getCompound(i),
                            mc.level.registryAccess()));
                npc.setTrades(trades);
            }
        });
    }
}