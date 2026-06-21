package com.hbm.ntm.blockentity;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.menu.BalefireBombMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BalefireBombBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyButtonReceiver {
    public static final int SLOT_COUNT = 2;
    public static final int CONTROL_START = 0;
    public static final int CONTROL_TIMER_SECONDS = 1;
    public static final int DEFAULT_TIMER = 18000;
    public static final int DEFAULT_RANGE = 250;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final String TAG_MODERN_INVENTORY = "Inventory";
    private static final String TAG_STARTED = "started";
    private static final String TAG_TIMER = "timer";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            syncChanged();
        }
    };
    private boolean loaded;
    private boolean started;
    private int timer = DEFAULT_TIMER;
    @Nullable
    private String customName;

    public BalefireBombBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BALEFIRE_BOMB.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isLoaded() {
        return hasEgg() && hasBattery();
    }

    public boolean isLoadedSynced() {
        return loaded;
    }

    public boolean isStarted() {
        return started;
    }

    public int getTimer() {
        return timer;
    }

    public int getTimerSeconds() {
        return Math.max(0, timer / 20);
    }

    public boolean hasEgg() {
        return isLegacyItem(items.getStackInSlot(0), "egg_balefire");
    }

    public boolean hasBattery() {
        return getBattery() > 0;
    }

    public int getBattery() {
        if (isLegacyItem(items.getStackInSlot(1), "battery_spark")) {
            return 1;
        }
        if (isLegacyItem(items.getStackInSlot(1), "battery_trixite")) {
            return 2;
        }
        return 0;
    }

    public String getMinutesText() {
        int minutes = Math.max(0, timer) / 1200;
        return minutes < 10 ? "0" + minutes : Integer.toString(minutes);
    }

    public String getSecondsText() {
        int seconds = (Math.max(0, timer) / 20) % 60;
        return seconds < 10 ? "0" + seconds : Integer.toString(seconds);
    }

    public void spillDrops(Level level, BlockPos pos) {
        HbmItemStackUtil.spillItems(level, pos, items);
    }

    public void clearSlots() {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        syncChanged();
    }

    public void explode() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        clearSlots();
        level.removeBlock(getBlockPos(), false);
        WeaponExplosionUtil.spawnBalefire(level, getBlockPos().getX() + 0.5D, getBlockPos().getY() + 0.5D,
                getBlockPos().getZ() + 0.5D, DEFAULT_RANGE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BalefireBombBlockEntity blockEntity) {
        boolean oldLoaded = blockEntity.loaded;
        boolean oldStarted = blockEntity.started;
        int oldTimerSeconds = blockEntity.getTimerSeconds();

        blockEntity.loaded = blockEntity.isLoaded();
        if (!blockEntity.loaded) {
            blockEntity.started = false;
        }
        if (blockEntity.started) {
            blockEntity.timer--;
            if (blockEntity.timer % 20 == 0) {
                LegacySoundPlayer.playLegacyFstbmbPing(level, pos, 5.0F, 1.0F);
            }
        }
        if (blockEntity.timer <= 0) {
            blockEntity.explode();
            return;
        }
        if (blockEntity.loaded != oldLoaded || blockEntity.started != oldStarted
                || blockEntity.getTimerSeconds() != oldTimerSeconds) {
            blockEntity.syncChanged();
        }
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_START || id == CONTROL_TIMER_SECONDS;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_START && isLoaded()) {
            Level level = getLevel();
            if (level != null) {
                LegacySoundPlayer.playLegacyFstbmbStart(level, getBlockPos(), 5.0F, 1.0F);
            }
            started = true;
            syncChanged();
        }
        if (id == CONTROL_TIMER_SECONDS) {
            timer = Math.max(1, Math.min(999, value)) * 20;
            syncChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putBoolean(TAG_STARTED, started);
        tag.putInt(TAG_TIMER, timer);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            HbmItemStackUtil.loadLegacyItems(tag, TAG_ITEMS, items);
        } else if (tag.contains(TAG_MODERN_INVENTORY)) {
            HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_INVENTORY, items);
        }
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        started = tag.getBoolean(TAG_STARTED);
        timer = tag.contains(TAG_TIMER, Tag.TAG_INT) ? tag.getInt(TAG_TIMER) : DEFAULT_TIMER;
        loaded = isLoaded();
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isBlank();
    }

    public void setCustomName(@Nullable String customName) {
        this.customName = customName == null || customName.isBlank() ? null : customName;
        setChanged();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.offset(-5, -1, -5), pos.offset(5, 3, 5));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? Component.literal(customName) : Component.translatable("container.nukeFstbmb");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BalefireBombMenu(containerId, inventory, this);
    }

    private static boolean isLegacyItem(ItemStack stack, String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && !stack.isEmpty() && stack.is(item.get());
    }

    private void syncChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
