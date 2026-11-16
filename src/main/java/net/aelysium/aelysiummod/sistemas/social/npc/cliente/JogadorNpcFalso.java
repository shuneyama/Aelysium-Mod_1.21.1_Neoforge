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

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class JogadorNpcFalso extends AbstractClientPlayer {

    private static final Map<UUID, JogadorNpcFalso> CACHE = new ConcurrentHashMap<>();
    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
    private static final ResourceLocation TEXTURA_STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private static final ResourceLocation TEXTURA_ALEX = ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png");

    private EntidadeNpcPersonalizado npcEntity;
    private ResourceLocation customSkinTexture;
    private PlayerSkin.Model skinModel = PlayerSkin.Model.WIDE;
    private PlayerSkin skinCache;
    private final ItemStack[] equipamento = new ItemStack[SLOTS.length];
    boolean cpmAtivo;

    private JogadorNpcFalso(ClientLevel level, GameProfile profile, EntidadeNpcPersonalizado npc) {
        super(level, profile);
        this.npcEntity = npc;
        Arrays.fill(equipamento, ItemStack.EMPTY);
        this.setCustomName(null);
        this.setCustomNameVisible(false);
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

        JogadorNpcFalso resultado = CACHE.get(npc.getUUID());
        if (resultado == null || resultado.level() != level) {
            resultado = new JogadorNpcFalso(level, new GameProfile(npc.getUUID(), ""), npc);
            CACHE.put(npc.getUUID(), resultado);
        }
        resultado.syncFromNpc(npc);
        ResourceLocation texturaPersonalizada = GerenciadorSkinNpc.getSkinTexture(npc.getId());
        if (texturaPersonalizada != null) resultado.setCustomSkinTexture(texturaPersonalizada);
        GerenciadorSkinNpc.sincronizarCpm(resultado, npc.getId());
        return resultado;
    }

    public static void liberarCpm(EntidadeNpcPersonalizado npc) {
        JogadorNpcFalso falso = CACHE.get(npc.getUUID());
        if (falso != null && falso.cpmAtivo) GerenciadorSkinNpc.sincronizarCpm(falso, npc.getId());
    }

    public void syncFromNpc(EntidadeNpcPersonalizado npc) {
        this.npcEntity = npc;
        if (this.getX() != npc.getX() || this.getY() != npc.getY() || this.getZ() != npc.getZ()) {
            this.setPos(npc.getX(), npc.getY(), npc.getZ());
        }
        this.setYRot(npc.getYRot());
        this.setXRot(npc.getXRot());
        this.yHeadRot = npc.yHeadRot;
        this.yHeadRotO = npc.yHeadRotO;
        this.yBodyRot = npc.yBodyRot;
        this.yBodyRotO = npc.yBodyRotO;
        this.yRotO = npc.yRotO;
        this.xRotO = npc.xRotO;

        syncModelType(npc.getModelType());

        for (EquipmentSlot slot : SLOTS) {
            equipamento[slot.ordinal()] = npc.getItemBySlot(slot);
        }
    }

    public void syncModelType(String modelType) {
        PlayerSkin.Model novo = "slim".equals(modelType) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
        if (novo != skinModel) {
            skinModel = novo;
            skinCache = null;
        }
    }

    public void setCustomSkinTexture(ResourceLocation texture) {
        if (texture != customSkinTexture) {
            customSkinTexture = texture;
            skinCache = null;
        }
    }

    public ResourceLocation getCustomSkinTexture() {
        return customSkinTexture;
    }

    public EntidadeNpcPersonalizado getNpcEntity() {
        return npcEntity;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return equipamento[slot.ordinal()];
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        equipamento[slot.ordinal()] = stack;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return true;
    }

    @Override
    public PlayerSkin getSkin() {
        PlayerSkin skin = skinCache;
        if (skin == null) {
            ResourceLocation skinTex = customSkinTexture;
            if (skinTex == null) {
                skinTex = skinModel == PlayerSkin.Model.SLIM ? TEXTURA_ALEX : TEXTURA_STEVE;
            }
            skin = new PlayerSkin(skinTex, null, null, null, skinModel, false);
            skinCache = skin;
        }
        return skin;
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
