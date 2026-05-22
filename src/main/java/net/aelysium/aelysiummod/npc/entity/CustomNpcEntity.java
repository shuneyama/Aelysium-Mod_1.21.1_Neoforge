package net.aelysium.aelysiummod.npc.entity;

import net.aelysium.aelysiummod.npc.NpcIdManager;
import net.aelysium.aelysiummod.npc.NpcRegistry;
import net.aelysium.aelysiummod.item.custom.CriadorNPC;
import net.aelysium.aelysiummod.item.custom.CriadorRegistro;
import net.aelysium.aelysiummod.menu.gui.NpcMerchant;
import net.aelysium.aelysiummod.npc.util.NpcConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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

public class CustomNpcEntity extends LivingEntity {

    private static final EntityDataAccessor<String> NPC_NAME =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SKIN_MODE =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> MODEL_TYPE =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> LOOK_AT_PLAYERS =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LOOK_RADIUS =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> NPC_INVULNERABLE =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SCALE_X =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE_Y =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE_Z =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_GRAVITY =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> NAME_VISIBLE =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> ROTATION_X =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROTATION_Y =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROTATION_Z =
            SynchedEntityData.defineId(CustomNpcEntity.class, EntityDataSerializers.FLOAT);

    private int npcId = -1;

    private final List<NpcTradeData> trades = new ArrayList<>();
    private byte[] skinData = null;
    private UUID linkedBookUUID = null;
    private final Map<String, Integer> tradeLog = new HashMap<>();
    private final ItemStack[] equipment = new ItemStack[6];
    private boolean idAssigned = false;

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    public int  getNpcId()          { return npcId; }
    public void setNpcId(int id)   { npcId = id; }

    public CustomNpcEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        for (int i = 0; i < equipment.length; i++) {
            equipment[i] = ItemStack.EMPTY;
        }
    }

    public CustomNpcEntity(Level level, double x, double y, double z) {
        this(NpcRegistry.CUSTOM_NPC.get(), level);
        this.setPos(x, y, z);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(NPC_NAME, "§6NPC");
        builder.define(SKIN_MODE, "vanilla");
        builder.define(MODEL_TYPE, "wide");
        builder.define(LOOK_AT_PLAYERS, false);
        builder.define(LOOK_RADIUS, NpcConstants.DEFAULT_LOOK_RADIUS);
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
                net.aelysium.aelysiummod.npc.network.NpcPackets.handleLinkLogBookDirect(
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
            net.aelysium.aelysiummod.npc.network.NpcPackets.sendOpenEditor(serverPlayer, this);
        }
    }

    private void openMerchantForPlayer(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NpcMerchant merchant =
                    new NpcMerchant(this, serverPlayer);
            merchant.openTradingScreen(serverPlayer, getDisplayNameComponent(), 0);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && !idAssigned) {
            idAssigned = true;
            if (npcId == -1) {
                npcId = NpcIdManager.get((ServerLevel) level()).nextId();
            }
        }

        if (getHasGravity()) {
            if (!onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0));
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
            } else {
                this.setDeltaMovement(Vec3.ZERO);
            }
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (!level().isClientSide) {
            long currentTick = level().getGameTime();
            for (NpcTradeData trade : trades) {
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
            if (source.getEntity() instanceof Player player) {
                if (player.hasPermissions(2) && player.isCreative()) {
                    this.discard();
                    return true;
                }
            }
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
        return Component.literal(getNpcName().replace('&', '§'));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        HolderLookup.Provider provider = this.registryAccess();
        tag.putString("NpcName", getNpcName());
        tag.putString("SkinMode", getSkinMode());
        tag.putString("SkinModelType", getModelType());
        tag.putBoolean("NpcInvulnerable", isNpcInvulnerable());
        tag.putBoolean("LookAtPlayers", getLookAtPlayers());
        tag.putFloat("LookRadius", getLookRadius());
        tag.putFloat("ScaleX", getScaleX());
        tag.putFloat("ScaleY", getScaleY());
        tag.putFloat("ScaleZ", getScaleZ());
        tag.putBoolean("HasGravity", getHasGravity());
        tag.putBoolean("NameVisible", getNameVisible());
        tag.putFloat("RotationX", getRotationX());
        tag.putFloat("RotationY", getRotationY());
        tag.putFloat("RotationZ", getRotationZ());
        tag.putInt("NpcId", npcId);
        if (skinData != null) tag.putByteArray("SkinData", skinData);
        if (linkedBookUUID != null) tag.putUUID("LinkedBookUUID", linkedBookUUID);
        ListTag equipTag = new ListTag();
        for (int i = 0; i < equipment.length; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", i);
            if (!equipment[i].isEmpty()) slotTag.put("Item", equipment[i].save(provider));
            equipTag.add(slotTag);
        }
        tag.put("NpcEquipment", equipTag);
        ListTag tradesTag = new ListTag();
        for (NpcTradeData trade : trades) tradesTag.add(trade.save(provider));
        tag.put("Trades", tradesTag);
        CompoundTag logTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : tradeLog.entrySet()) logTag.putInt(entry.getKey(), entry.getValue());
        tag.put("TradeLog", logTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        HolderLookup.Provider provider = this.registryAccess();
        if (tag.contains("NpcId")) { npcId = tag.getInt("NpcId");
            idAssigned = true;
        }
        if (tag.contains("NpcName")) setNpcName(tag.getString("NpcName"));
        if (tag.contains("SkinMode")) setSkinMode(tag.getString("SkinMode"));
        if (tag.contains("SkinModelType")) setModelType(tag.getString("SkinModelType"));
        if (tag.contains("NpcInvulnerable")) setNpcInvulnerable(tag.getBoolean("NpcInvulnerable"));
        if (tag.contains("LookAtPlayers")) setLookAtPlayers(tag.getBoolean("LookAtPlayers"));
        if (tag.contains("LookRadius")) setLookRadius(tag.getFloat("LookRadius"));
        if (tag.contains("ScaleX")) setScaleX(tag.getFloat("ScaleX"));
        if (tag.contains("ScaleY")) setScaleY(tag.getFloat("ScaleY"));
        if (tag.contains("ScaleZ")) setScaleZ(tag.getFloat("ScaleZ"));
        if (tag.contains("HasGravity")) setHasGravity(tag.getBoolean("HasGravity"));
        if (tag.contains("NameVisible")) setNameVisible(tag.getBoolean("NameVisible"));
        if (tag.contains("RotationX")) setRotationX(tag.getFloat("RotationX"));
        if (tag.contains("RotationY")) setRotationY(tag.getFloat("RotationY"));
        if (tag.contains("RotationZ")) setRotationZ(tag.getFloat("RotationZ"));
        if (tag.contains("SkinData")) skinData = tag.getByteArray("SkinData");
        if (tag.contains("LinkedBookUUID")) linkedBookUUID = tag.getUUID("LinkedBookUUID");
        if (tag.contains("NpcEquipment")) {
            ListTag equipTag = tag.getList("NpcEquipment", Tag.TAG_COMPOUND);
            for (int i = 0; i < equipTag.size(); i++) {
                CompoundTag slotTag = equipTag.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < equipment.length && slotTag.contains("Item")) {
                    equipment[slot] = ItemStack.parse(provider, slotTag.getCompound("Item")).orElse(ItemStack.EMPTY);
                }
            }
        }
        trades.clear();
        if (tag.contains("Trades")) {
            ListTag tradesTag = tag.getList("Trades", Tag.TAG_COMPOUND);
            for (int i = 0; i < tradesTag.size(); i++) trades.add(NpcTradeData.load(tradesTag.getCompound(i), provider));
        }
        tradeLog.clear();
        if (tag.contains("TradeLog")) {
            CompoundTag logTag = tag.getCompound("TradeLog");
            for (String key : logTag.getAllKeys()) tradeLog.put(key, logTag.getInt(key));
        }
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

    public List<NpcTradeData> getTrades() { return trades; }
    public void setTrades(List<NpcTradeData> newTrades) { trades.clear(); trades.addAll(newTrades); }
    public byte[] getSkinData() { return skinData; }
    public void setSkinData(byte[] data) { this.skinData = data; }
    public UUID getLinkedBookUUID() { return linkedBookUUID; }
    public void setLinkedBookUUID(UUID uuid) { this.linkedBookUUID = uuid; }
    public Map<String, Integer> getTradeLog() { return tradeLog; }
    public void logTrade(String itemName) { tradeLog.merge(itemName, 1, Integer::sum); }
}
