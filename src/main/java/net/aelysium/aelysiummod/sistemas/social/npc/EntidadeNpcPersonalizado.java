package net.aelysium.aelysiummod.sistemas.social.npc;

import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import net.aelysium.aelysiummod.sistemas.social.npc.GerenciadorIdNpc;
import net.aelysium.aelysiummod.registro.RegistroNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorNPC;
import net.aelysium.aelysiummod.sistemas.social.npc.item.CriadorRegistro;
import net.aelysium.aelysiummod.sistemas.social.npc.MercadorNpc;
import net.aelysium.aelysiummod.sistemas.social.npc.ConstantesNpc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class EntidadeNpcPersonalizado extends LivingEntity {

    private static final EntityDataAccessor<String> NPC_NAME =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SKIN_MODE =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> MODEL_TYPE =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> LOOK_AT_PLAYERS =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LOOK_RADIUS =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> NPC_INVULNERABLE =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SCALE_X =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE_Y =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE_Z =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_GRAVITY =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> NAME_VISIBLE =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> ROTATION_X =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROTATION_Y =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROTATION_Z =
            SynchedEntityData.defineId(EntidadeNpcPersonalizado.class, EntityDataSerializers.FLOAT);

    int npcId = -1;

    final List<DadosTrocaNpc> trades = new ArrayList<>();
    byte[] skinData = null;
    UUID linkedBookUUID = null;
    final Map<String, Integer> tradeLog = new HashMap<>();
    final ItemStack[] equipment = new ItemStack[6];
    boolean idAssigned = false;
    private String nomeExibicaoOrigem;
    private Component nomeExibicaoCache;

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    public int  getNpcId()          { return npcId; }
    public void setNpcId(int id)   { npcId = id; }

    public EntidadeNpcPersonalizado(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        for (int i = 0; i < equipment.length; i++) {
            equipment[i] = ItemStack.EMPTY;
        }
    }

    public EntidadeNpcPersonalizado(Level level, double x, double y, double z) {
        this(RegistroNpc.CUSTOM_NPC.get(), level);
        this.setPos(x, y, z);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(NPC_NAME, "§6NPC");
        builder.define(SKIN_MODE, "vanilla");
        builder.define(MODEL_TYPE, "wide");
        builder.define(LOOK_AT_PLAYERS, false);
        builder.define(LOOK_RADIUS, ConstantesNpc.DEFAULT_LOOK_RADIUS);
        builder.define(NPC_INVULNERABLE, true);
        builder.define(SCALE_X, 1.0f);
        builder.define(SCALE_Y, 1.0f);
        builder.define(SCALE_Z, 1.0f);
        builder.define(HAS_GRAVITY, false);
        builder.define(NAME_VISIBLE, true);
        builder.define(ROTATION_X, 0.0f);
        builder.define(ROTATION_Y, 0.0f);
        builder.define(ROTATION_Z, 0.0f);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide) {
            boolean isOpCreative = player.hasPermissions(2) && player.isCreative();
            ItemStack heldItem = player.getItemInHand(hand);
            boolean holdingCreator = heldItem.getItem() instanceof CriadorNPC;
            boolean holdingLogBook = heldItem.getItem() instanceof CriadorRegistro;
            if (holdingCreator && isOpCreative) {
                openEditorForPlayer(player);
                return InteractionResult.SUCCESS;
            } else if (holdingLogBook && isOpCreative) {
                net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.handleLinkLogBookDirect(
                        (net.minecraft.server.level.ServerPlayer) player, this);
                return InteractionResult.SUCCESS;
            } else {
                openMerchantForPlayer(player);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void openEditorForPlayer(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.aelysium.aelysiummod.sistemas.social.npc.rede.PacotesNpc.sendOpenEditor(serverPlayer, this);
        }
    }

    private void openMerchantForPlayer(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            MercadorNpc merchant =
                    new MercadorNpc(this, serverPlayer);
            merchant.openTradingScreen(serverPlayer, getDisplayNameComponent(), 0);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && !idAssigned) {
            idAssigned = true;
            if (npcId == -1) {
                npcId = GerenciadorIdNpc.get((ServerLevel) level()).nextId();
            }
        }

        if (getHasGravity()) {
            Vec3 movimento = this.getDeltaMovement();
            this.setDeltaMovement(0, onGround() ? 0 : movimento.y, 0);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (!level().isClientSide) {
            long currentTick = level().getGameTime();
            for (DadosTrocaNpc trade : trades) {
                trade.checkAndRestock(currentTick);
            }
            if (getLookAtPlayers()) {
                Player nearest = level().getNearestPlayer(this, getLookRadius());
                if (nearest != null) {
                    lookAtEntity(nearest);
                }
            }

        }
    }

    private void lookAtEntity(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dy = target.getEyeY() - this.getEyeY();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horizontalDist) * (180.0 / Math.PI)));
        this.setYHeadRot(yaw);
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isNpcInvulnerable()) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeCollidedWith() { return true; }
    @Override public void push(Entity entity) { }
    @Override public void knockback(double strength, double x, double z) { }
    @Override public boolean shouldBeSaved() { return true; }

    private int slotIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0; case CHEST -> 1; case LEGS -> 2;
            case FEET -> 3; case MAINHAND -> 4; case OFFHAND -> 5;
            default -> -1;
        };
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        List<ItemStack> armor = new ArrayList<>();
        armor.add(equipment[3]);
        armor.add(equipment[2]);
        armor.add(equipment[1]);
        armor.add(equipment[0]);
        return armor;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        int idx = slotIndex(slot);
        if (idx < 0) return ItemStack.EMPTY;
        return equipment[idx];
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        int idx = slotIndex(slot);
        if (idx >= 0) equipment[idx] = stack.copy();
    }

    public ItemStack getNpcEquipment(int slotIdx) {
        if (slotIdx < 0 || slotIdx >= equipment.length) return ItemStack.EMPTY;
        return equipment[slotIdx];
    }

    public void setNpcEquipment(int slotIdx, ItemStack stack) {
        if (slotIdx >= 0 && slotIdx < equipment.length) equipment[slotIdx] = stack.copy();
    }

    @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }

    @Override
    public Component getDisplayName() { return getDisplayNameComponent(); }

    public Component getDisplayNameComponent() {
        String nome = getNpcName();
        if (nome != nomeExibicaoOrigem || nomeExibicaoCache == null) {
            nomeExibicaoCache = CodigosEstilo.componente(nome);
            nomeExibicaoOrigem = nome;
        }
        return nomeExibicaoCache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        boolean arquivoGravado = !level().isClientSide && ArquivoNpcMundo.salvar(this);
        if (arquivoGravado) {
            tag.putBoolean("AelysiumNpcEmArquivo", true);
        } else {
            PersistenciaNpc.salvar(this, tag);
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!level().isClientSide) ArquivoNpcMundo.salvar(this);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag doArquivo = level() instanceof ServerLevel nivel ? ArquivoNpcMundo.lerCompleto(nivel, getUUID()) : null;
        PersistenciaNpc.carregar(this, doArquivo != null ? doArquivo : tag);
    }

    public String getNpcName() { return entityData.get(NPC_NAME); }
    public void setNpcName(String name) { entityData.set(NPC_NAME, name); }
    public String getSkinMode() { return entityData.get(SKIN_MODE); }
    public void setSkinMode(String mode) { entityData.set(SKIN_MODE, mode); }
    public String getModelType() { return entityData.get(MODEL_TYPE); }
    public void setModelType(String type) { entityData.set(MODEL_TYPE, type); }
    public boolean getLookAtPlayers() { return entityData.get(LOOK_AT_PLAYERS); }
    public void setLookAtPlayers(boolean val) { entityData.set(LOOK_AT_PLAYERS, val); }
    public float getLookRadius() { return entityData.get(LOOK_RADIUS); }
    public void setLookRadius(float val) { entityData.set(LOOK_RADIUS, val); }
    public boolean isNpcInvulnerable() { return entityData.get(NPC_INVULNERABLE); }
    public void setNpcInvulnerable(boolean val) { entityData.set(NPC_INVULNERABLE, val); }
    public float getScaleX() { return entityData.get(SCALE_X); }
    public void setScaleX(float val) { entityData.set(SCALE_X, Math.max(0.1f, Math.min(val, 5.0f))); }
    public float getScaleY() { return entityData.get(SCALE_Y); }
    public void setScaleY(float val) { entityData.set(SCALE_Y, Math.max(0.1f, Math.min(val, 5.0f))); }
    public float getScaleZ() { return entityData.get(SCALE_Z); }
    public void setScaleZ(float val) { entityData.set(SCALE_Z, Math.max(0.1f, Math.min(val, 5.0f))); }
    public boolean getHasGravity() { return entityData.get(HAS_GRAVITY); }
    public void setHasGravity(boolean val) { entityData.set(HAS_GRAVITY, val); this.setNoGravity(!val); this.noPhysics = !val; }
    public boolean getNameVisible() { return entityData.get(NAME_VISIBLE); }
    public void setNameVisible(boolean val) { entityData.set(NAME_VISIBLE, val); }
    public float getRotationX() { return entityData.get(ROTATION_X); }
    public void setRotationX(float val) { entityData.set(ROTATION_X, val); }
    public float getRotationY() { return entityData.get(ROTATION_Y); }
    public void setRotationY(float val) { entityData.set(ROTATION_Y, val); }
    public float getRotationZ() { return entityData.get(ROTATION_Z); }
    public void setRotationZ(float val) { entityData.set(ROTATION_Z, val); }

    public List<DadosTrocaNpc> getTrades() { return trades; }
    public void setTrades(List<DadosTrocaNpc> newTrades) { trades.clear(); trades.addAll(newTrades); }
    public byte[] getSkinData() { return skinData; }
    public void setSkinData(byte[] data) { this.skinData = data; }
    public UUID getLinkedBookUUID() { return linkedBookUUID; }
    public void setLinkedBookUUID(UUID uuid) { this.linkedBookUUID = uuid; }
    public Map<String, Integer> getTradeLog() { return tradeLog; }
    public void logTrade(String itemName) { tradeLog.merge(itemName, 1, Integer::sum); }
}
