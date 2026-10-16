package net.aelysium.aelysiummod.sistemas.social.npc.cliente;

import com.mojang.authlib.GameProfile;
import net.aelysium.aelysiummod.sistemas.social.npc.EntidadeNpcPersonalizado;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class JogadorNpcFalso extends AbstractClientPlayer {

    private static final Map<UUID, JogadorNpcFalso> CACHE = new ConcurrentHashMap<>();

    private final EntidadeNpcPersonalizado npcEntity;
    private ResourceLocation customSkinTexture;
    private PlayerSkin.Model skinModel = PlayerSkin.Model.WIDE;

    private JogadorNpcFalso(ClientLevel level, GameProfile profile, EntidadeNpcPersonalizado npc) {
        super(level, profile);
        this.npcEntity = npc;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public static JogadorNpcFalso getOrCreate(EntidadeNpcPersonalizado npc) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;

        JogadorNpcFalso resultado = CACHE.compute(npc.getUUID(), (uuid, existing) -> {
            if (existing != null && existing.level() == level) {
                existing.syncFromNpc(npc);
                return existing;
            }
            GameProfile profile = new GameProfile(npc.getUUID(), "");
            JogadorNpcFalso fake = new JogadorNpcFalso(level, profile, npc);
            fake.syncFromNpc(npc);
            return fake;
        });
        ResourceLocation texturaPersonalizada = GerenciadorSkinNpc.getSkinTexture(npc.getId());
        if (texturaPersonalizada != null) resultado.setCustomSkinTexture(texturaPersonalizada);
        GerenciadorSkinNpc.sincronizarCpm(resultado, npc.getId());
        return resultado;
    }

    public void syncFromNpc(EntidadeNpcPersonalizado npc) {
        this.setPos(npc.getX(), npc.getY(), npc.getZ());
        this.setYRot(npc.getYRot());
        this.setXRot(npc.getXRot());
        this.yHeadRot = npc.yHeadRot;
        this.yHeadRotO = npc.yHeadRotO;
        this.yBodyRot = npc.yBodyRot;
        this.yBodyRotO = npc.yBodyRotO;
        this.yRotO = npc.yRotO;
        this.xRotO = npc.xRotO;

        this.skinModel = "slim".equals(npc.getModelType())
                ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack npcItem = npc.getItemBySlot(slot);
            this.setItemSlot(slot, npcItem);
        }

        this.setCustomName(null);
        this.setCustomNameVisible(false);
    }

    public void syncModelType(String modelType) {
        this.skinModel = "slim".equals(modelType) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
    }

    public void setCustomSkinTexture(ResourceLocation texture) {
        this.customSkinTexture = texture;
    }

    public ResourceLocation getCustomSkinTexture() {
        return customSkinTexture;
    }

    public EntidadeNpcPersonalizado getNpcEntity() {
        return npcEntity;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return true;
    }

    @Override
    public PlayerSkin getSkin() {
        ResourceLocation skinTex = customSkinTexture;
        if (skinTex == null) {
            skinTex = skinModel == PlayerSkin.Model.SLIM
                    ? ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png")
                    : ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
        }

        return new PlayerSkin(
                skinTex,
                null,
                null,
                null,
                skinModel,
                false
        );
    }

    @Override
    public boolean isSpectator() { return false; }

    @Override
    public boolean isCreative() { return false; }

    public static void remove(UUID npcUuid) {
        CACHE.remove(npcUuid);
    }

    public static void clearAll() {
        CACHE.clear();
    }
}