package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class RadarMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = RadarBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final RadarBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int scanMissiles;
    private int scanShells;
    private int scanPlayers;
    private int smartMode;
    private int redMode;
    private int showMap;
    private int jammed;
    private int redPower;

    public RadarMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RadarMenu(int containerId, Inventory playerInventory, RadarBlockEntity blockEntity) {
        super(ModMenuTypes.RADAR.get(), containerId);
        this.blockEntity = blockEntity;

        for (int slot = 0; slot < 8; slot++) {
            addSlot(new SlotItemHandler(blockEntity.getItems(), slot, 26 + slot * 18, 17));
        }
        addSlot(new SlotItemHandler(blockEntity.getItems(), RadarBlockEntity.SLOT_LINKER, 26, 44));
        addSlot(new SlotItemHandler(blockEntity.getItems(), RadarBlockEntity.SLOT_BATTERY, 152, 44));
        addPlayerInventory(playerInventory);
        addRadarDataSlots();
    }

    public RadarBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public boolean scanMissiles() {
        return scanMissiles != 0;
    }

    public boolean scanShells() {
        return scanShells != 0;
    }

    public boolean scanPlayers() {
        return scanPlayers != 0;
    }

    public boolean smartMode() {
        return smartMode != 0;
    }

    public boolean redMode() {
        return redMode != 0;
    }

    public boolean showMap() {
        return showMap != 0;
    }

    public boolean jammed() {
        return jammed != 0;
    }

    public int getRedPower() {
        return redPower;
    }

    public int getPowerBarWidth(int maxWidth) {
        return maxPower <= 0L ? 0 : (int) (power * maxWidth / maxPower);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        if (index < 0 || index >= slots.size()) {
            return result;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.RADAR_LINKER.get())) {
            if (!moveItemStackTo(stack, RadarBlockEntity.SLOT_LINKER, RadarBlockEntity.SLOT_LINKER + 1, false)
                    && !moveItemStackTo(stack, 0, RadarBlockEntity.SLOT_LINKER, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.SAT_RELAY.get())) {
            if (!moveItemStackTo(stack, 0, RadarBlockEntity.SLOT_LINKER, false)) {
                return ItemStack.EMPTY;
            }
        } else if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            if (!moveItemStackTo(stack, RadarBlockEntity.SLOT_BATTERY, RadarBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 161));
        }
    }

    private void addRadarDataSlots() {
        addLongDataSlot(() -> blockEntity.getPower(), () -> power, value -> power = value);
        addLongDataSlot(() -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        addBooleanDataSlot(() -> blockEntity.isScanMissiles(), value -> scanMissiles = value);
        addBooleanDataSlot(() -> blockEntity.isScanShells(), value -> scanShells = value);
        addBooleanDataSlot(() -> blockEntity.isScanPlayers(), value -> scanPlayers = value);
        addBooleanDataSlot(() -> blockEntity.isSmartMode(), value -> smartMode = value);
        addBooleanDataSlot(() -> blockEntity.isRedMode(), value -> redMode = value);
        addBooleanDataSlot(() -> blockEntity.isShowMap(), value -> showMap = value);
        addBooleanDataSlot(() -> blockEntity.isJammed(), value -> jammed = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getLastRedPower();
            }

            @Override
            public void set(int value) {
                redPower = value;
            }
        });
    }

    private void addBooleanDataSlot(BooleanGetter serverGetter, IntSetter setter) {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return serverGetter.get() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                setter.set(value);
            }
        });
    }

    private void addLongDataSlot(LongGetter serverGetter, LongGetter clientGetter, LongSetter setter) {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (serverGetter.get() & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~0xFFFFL) | (value & 0xFFFFL));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((serverGetter.get() >>> 16) & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~(0xFFFFL << 16)) | ((long) (value & 0xFFFF) << 16));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((serverGetter.get() >>> 32) & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~(0xFFFFL << 32)) | ((long) (value & 0xFFFF) << 32));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((serverGetter.get() >>> 48) & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~(0xFFFFL << 48)) | ((long) (value & 0xFFFF) << 48));
            }
        });
    }

    private static RadarBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RadarBlockEntity radar) {
            return radar;
        }
        throw new IllegalStateException("Expected radar block entity at " + pos);
    }

    @FunctionalInterface
    private interface BooleanGetter {
        boolean get();
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }

    @FunctionalInterface
    private interface LongGetter {
        long get();
    }

    private interface LongSetter {
        void set(long value);
    }
}
