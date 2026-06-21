package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.menu.ForceFieldMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForceFieldBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider, HbmLegacyButtonReceiver, LegacyLookOverlayProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_RADIUS = 1;
    public static final int SLOT_HEALTH = 2;
    public static final int SLOT_COUNT = 3;
    public static final int BASE_CONSUMPTION = 1_000;
    public static final int RADIUS_CONSUMPTION = 500;
    public static final int SHIELD_CONSUMPTION = 250;
    public static final long MAX_POWER = 1_000_000L;
    public static final int BASE_RADIUS = 16;
    public static final int RADIUS_UPGRADE = 16;
    public static final int SHIELD_UPGRADE = 50;
    public static final double COOLDOWN_MODIFIER = 1.0D;
    public static final double HEALTH_REGEN_MODIFIER = 1.0D;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_POWER_TIME = "powerTime";
    private static final String TAG_HEALTH = "health";
    private static final String TAG_MAX_HEALTH = "maxHealth";
    private static final String TAG_COOLDOWN = "cooldown";
    private static final String TAG_BLINK = "blink";
    private static final String TAG_RADIUS = "radius";
    private static final String TAG_ON = "isOn";
    private static final String TAG_NAME = "name";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_RADIUS, SLOT_HEALTH -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler(items));
    private final List<Entity> outside = new ArrayList<>();
    private final List<Entity> inside = new ArrayList<>();
    private int health = 100;
    private int maxHealth = 100;
    private int powerCons;
    private int cooldown;
    private int blink;
    private float radius = BASE_RADIUS;
    private boolean on;
    private int color = 0x0000FF;
    @Nullable
    private String customName;

    public ForceFieldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORCE_FIELD.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForceFieldBlockEntity forceField) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = forceField.getPower();
        int oldHealth = forceField.health;
        int oldMaxHealth = forceField.maxHealth;
        int oldCooldown = forceField.cooldown;
        int oldColor = forceField.color;
        float oldRadius = forceField.radius;

        forceField.subscribeEnergyReceiverToAllSides();
        forceField.recalculateUpgrades();
        HbmEnergyUtil.chargeStorageFromItem(forceField.items.getStackInSlot(SLOT_BATTERY), forceField.energy,
                forceField.energy.getReceiverSpeed());
        forceField.tickShield(level);

        if (forceField.getPower() < forceField.powerCons) {
            forceField.setPower(0L);
        }
        ModMessages.sendForceFieldState(forceField, forceField.radius, forceField.health, forceField.maxHealth,
                (int) Math.min(Integer.MAX_VALUE, forceField.getPower()), forceField.on, forceField.color,
                forceField.cooldown);
        if (oldPower != forceField.getPower() || oldHealth != forceField.health
                || oldMaxHealth != forceField.maxHealth || oldCooldown != forceField.cooldown
                || oldColor != forceField.color || oldRadius != forceField.radius
                || level.getGameTime() % 20L == 0L) {
            forceField.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void recalculateUpgrades() {
        int radiusStack = 0;
        int healthStack = 0;
        radius = BASE_RADIUS;
        maxHealth = 100;
        ItemStack radiusUpgrade = items.getStackInSlot(SLOT_RADIUS);
        if (!radiusUpgrade.isEmpty() && radiusUpgrade.is(ModItems.UPGRADE_RADIUS.get())) {
            radiusStack = radiusUpgrade.getCount();
            radius += radiusStack * RADIUS_UPGRADE;
        }
        ItemStack healthUpgrade = items.getStackInSlot(SLOT_HEALTH);
        if (!healthUpgrade.isEmpty() && healthUpgrade.is(ModItems.UPGRADE_HEALTH.get())) {
            healthStack = healthUpgrade.getCount();
            maxHealth += healthStack * SHIELD_UPGRADE;
        }
        powerCons = BASE_CONSUMPTION + radiusStack * RADIUS_CONSUMPTION + healthStack * SHIELD_CONSUMPTION;
        if (blink > 0) {
            blink--;
            color = 0xFF0000;
        } else {
            color = 0x00FF00;
        }
    }

    private void tickShield(Level level) {
        if (cooldown > 0) {
            cooldown--;
        } else if (health < maxHealth) {
            health += (int) ((maxHealth / 100) * HEALTH_REGEN_MODIFIER);
            if (health > maxHealth) {
                health = maxHealth;
            }
        }
        if (isFieldActive()) {
            doField(level, radius);
            setPower(getPower() - powerCons);
        } else {
            outside.clear();
            inside.clear();
        }
    }

    private boolean isFieldActive() {
        return on && cooldown == 0 && health > 0 && getPower() >= powerCons;
    }

    private int impact(Entity entity) {
        double mass = entity.getBbHeight() * entity.getBbWidth() * entity.getBbWidth();
        double speed = getMotionWithFallback(entity);
        return (int) (mass * speed * 50.0D);
    }

    private void damage(int amount) {
        health -= amount;
        if (amount >= maxHealth / 250) {
            blink = 5;
        }
        if (health <= 0) {
            health = 0;
            cooldown = (int) (100 + radius * (float) COOLDOWN_MODIFIER);
        }
    }

    private void doField(Level level, float rad) {
        List<Entity> oldOutside = new ArrayList<>(outside);
        List<Entity> oldInside = new ArrayList<>(inside);
        outside.clear();
        inside.clear();
        Vec3 center = Vec3.atCenterOf(worldPosition);
        AABB box = new AABB(
                center.x - (rad + 25.0D), center.y - (rad + 25.0D), center.z - (rad + 25.0D),
                center.x + (rad + 25.0D), center.y + (rad + 25.0D), center.z + (rad + 25.0D));
        for (Entity entity : level.getEntities((Entity) null, box, entity -> !(entity instanceof Player))) {
            double dist = center.distanceTo(entity.position());
            boolean out = dist > rad;
            if (!oldOutside.contains(entity) && !oldInside.contains(entity)) {
                (out ? outside : inside).add(entity);
            } else if (oldOutside.contains(entity) && !out) {
                bounce(level, entity, center, rad + 1.0D, true);
                outside.add(entity);
            } else if (oldInside.contains(entity) && out) {
                bounce(level, entity, center, rad - 1.0D, false);
                inside.add(entity);
            } else {
                (out ? outside : inside).add(entity);
            }
        }
    }

    private void bounce(Level level, Entity entity, Vec3 center, double boundaryRadius, boolean inward) {
        Vec3 normal = center.subtract(entity.position()).normalize();
        Vec3 boundary = center.add(normal.scale(-boundaryRadius));
        entity.moveTo(boundary.x, boundary.y, boundary.z, 0.0F, 0.0F);
        double motionLength = entity.getDeltaMovement().length();
        Vec3 reflected = normal.scale(inward ? -motionLength : motionLength);
        entity.setDeltaMovement(reflected);
        entity.setPos(entity.getX() - reflected.x, entity.getY() - reflected.y, entity.getZ() - reflected.z);
        if (!isMuffled()) {
            level.playSound(null, entity.blockPosition(), ModSounds.WEAPON_SPARK_SHOOT.get(), SoundSource.BLOCKS,
                    2.5F, 1.0F);
        }
        damage(impact(entity));
    }

    private double getMotionWithFallback(Entity entity) {
        Vec3 current = entity.getDeltaMovement();
        Vec3 fallback = new Vec3(entity.getX() - entity.yo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        double currentSpeed = current.length();
        double fallbackSpeed = fallback.length();
        if (currentSpeed == 0.0D) {
            return fallbackSpeed;
        }
        if (fallbackSpeed == 0.0D) {
            return currentSpeed;
        }
        return Math.min(currentSpeed, fallbackSpeed);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCooldown() {
        return cooldown;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isOn() {
        return on;
    }

    public int getColor() {
        return color;
    }

    public int getPowerCons() {
        return powerCons;
    }

    public int getPowerBarHeight(int maxHeight) {
        return getMaxPower() <= 0L ? 0 : (int) (getPower() * maxHeight / getMaxPower());
    }

    public int getHealthBarHeight(int maxHeight) {
        return maxHealth <= 0 ? 0 : Mth.clamp(health * maxHeight / maxHealth, 0, maxHeight);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == Direction.UP ? HbmEnergySideMode.NONE : HbmEnergySideMode.INPUT;
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.forceField", "Forcefield Emitter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ForceFieldMenu(containerId, inventory, this);
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (value == 0 && id == 0) {
            on = !on;
            setChanged();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER_TIME, getPower());
        tag.putInt(TAG_HEALTH, health);
        tag.putInt(TAG_MAX_HEALTH, maxHealth);
        tag.putInt(TAG_COOLDOWN, cooldown);
        tag.putInt(TAG_BLINK, blink);
        tag.putFloat(TAG_RADIUS, radius);
        tag.putBoolean(TAG_ON, on);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains(TAG_POWER_TIME)) {
            setPower(tag.getLong(TAG_POWER_TIME));
        }
        health = tag.contains(TAG_HEALTH) ? tag.getInt(TAG_HEALTH) : 100;
        maxHealth = tag.contains(TAG_MAX_HEALTH) ? tag.getInt(TAG_MAX_HEALTH) : 100;
        cooldown = tag.getInt(TAG_COOLDOWN);
        blink = tag.getInt(TAG_BLINK);
        radius = tag.contains(TAG_RADIUS) ? tag.getFloat(TAG_RADIUS) : BASE_RADIUS;
        on = tag.getBoolean(TAG_ON);
        customName = tag.contains(TAG_NAME) ? tag.getString(TAG_NAME) : null;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER_TIME, getPower());
        tag.putInt(TAG_HEALTH, health);
        tag.putInt(TAG_MAX_HEALTH, maxHealth);
        tag.putInt(TAG_COOLDOWN, cooldown);
        tag.putInt(TAG_BLINK, blink);
        tag.putFloat(TAG_RADIUS, radius);
        tag.putBoolean(TAG_ON, on);
        tag.putInt("color", color);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_POWER_TIME)) {
            setPower(tag.getLong(TAG_POWER_TIME));
        } else if (tag.contains("power")) {
            setPower(tag.getInt("power"));
        }
        if (tag.contains(TAG_HEALTH)) {
            health = tag.getInt(TAG_HEALTH);
        }
        if (tag.contains(TAG_MAX_HEALTH)) {
            maxHealth = tag.getInt(TAG_MAX_HEALTH);
        }
        if (tag.contains(TAG_COOLDOWN)) {
            cooldown = tag.getInt(TAG_COOLDOWN);
        }
        if (tag.contains(TAG_BLINK)) {
            blink = tag.getInt(TAG_BLINK);
        }
        if (tag.contains(TAG_RADIUS)) {
            radius = tag.getFloat(TAG_RADIUS);
        }
        if (tag.contains(TAG_ON)) {
            on = tag.getBoolean(TAG_ON);
        } else if (tag.contains("active")) {
            on = tag.getBoolean("active");
        }
        if (tag.contains("color")) {
            color = tag.getInt("color");
        }
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.energyStorage(getPower(), getMaxPower()));
    }

    private static final class AccessibleItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private AccessibleItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_BATTERY) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 && HbmInventoryMenuHelper.isBatteryLike(stack)
                    ? items.insertItem(SLOT_BATTERY, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(SLOT_BATTERY) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && HbmInventoryMenuHelper.isBatteryLike(stack);
        }
    }
}
