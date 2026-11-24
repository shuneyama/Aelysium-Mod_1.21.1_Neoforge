package net.aelysium.aelysiummod.sistemas.rp.skin.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.social.npc.cliente.CompatCpmNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AelysiumMod.MOD_ID, value = Dist.CLIENT)
public final class SkinsJogadoresCliente {
    private record Estado(String modelo, byte[] dados, ResourceLocation textura, boolean cpm) {
    }

    private static final Map<UUID, Estado> ESTADOS = new ConcurrentHashMap<>();
    private static final Set<UUID> CPM_APLICADO = new HashSet<>();
    private static final AtomicInteger CONTADOR = new AtomicInteger();

    private SkinsJogadoresCliente() {
    }

    public static void receber(UUID jogador, String modelo, byte[] dados) {
        Estado antigo = ESTADOS.remove(jogador);
        if (antigo != null && antigo.textura() != null) Minecraft.getInstance().getTextureManager().release(antigo.textura());
        if (dados == null || dados.length == 0) return;
        if ((dados[0] & 0xFF) == 83) {
            ESTADOS.put(jogador, new Estado(modelo, dados, null, true));
            return;
        }
        try {
            NativeImage imagem = NativeImage.read(new ByteArrayInputStream(dados));
            ResourceLocation local = ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "skin_jogador/" + jogador + "/" + CONTADOR.incrementAndGet());
            Minecraft.getInstance().getTextureManager().register(local, new DynamicTexture(imagem));
            ESTADOS.put(jogador, new Estado(modelo, dados, local, false));
        } catch (Exception e) {
            System.out.println("[SkinsJogadores] Falha ao carregar skin: " + e.getMessage());
        }
    }

    public static PlayerSkin sobrescrever(UUID jogador, PlayerSkin original) {
        Estado estado = ESTADOS.get(jogador);
        if (estado == null) return original;
        PlayerSkin.Model modelo = "slim".equals(estado.modelo()) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
        if (estado.textura() == null) {
            return new PlayerSkin(original.texture(), original.textureUrl(), original.capeTexture(), original.elytraTexture(), modelo, original.secure());
        }
        return new PlayerSkin(estado.textura(), null, original.capeTexture(), original.elytraTexture(), modelo, false);
    }

    @SubscribeEvent
    public static void aoTick(ClientTickEvent.Post evento) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!ModList.get().isLoaded("cpm")) return;
        for (AbstractClientPlayer jogador : mc.level.players()) {
            UUID id = jogador.getUUID();
            Estado estado = ESTADOS.get(id);
            try {
                if (estado != null && estado.cpm()) {
                    CompatCpmNpc.sincronizar(jogador.getGameProfile(), estado.dados());
                    CPM_APLICADO.add(id);
                } else if (CPM_APLICADO.remove(id)) {
                    CompatCpmNpc.sincronizar(jogador.getGameProfile(), null);
                }
            } catch (Throwable e) {
                CPM_APLICADO.remove(id);
            }
        }
    }

    @SubscribeEvent
    public static void aoSair(ClientPlayerNetworkEvent.LoggingOut evento) {
        for (Estado estado : ESTADOS.values()) {
            if (estado.textura() != null) Minecraft.getInstance().getTextureManager().release(estado.textura());
        }
        ESTADOS.clear();
        CPM_APLICADO.clear();
    }
}
