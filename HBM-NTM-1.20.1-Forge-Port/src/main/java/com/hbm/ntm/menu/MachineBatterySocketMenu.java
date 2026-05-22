package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.registry.ModMenuTypes;
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

public class MachineBatterySocketMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final MachineBatterySocketBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private long delta;
    private int redLow;
    private int redHigh;
    private int priorityOrdinal;

    public MachineBatterySocketMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MachineBatterySocketMenu(int containerId, Inventory playerInventory, MachineBatterySocketBlockEntity blockEntity) {
        super(ModMenuTypes.MACHINE_BATTERY_SOCKET.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), MachineBatterySocketBlockEntity.SLOT_BATTERY, 35, 35));
        addPlayerInventory(playerInventory);
        addBatteryDataSlots();
    }

    public MachineBatterySocketBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public long getDelta() {
        return delta;
    }

    public int getRedLow() {
        return redLow;
    }

    public int getRedHigh() {
        return redHigh;
    }

    public HbmEnergyReceiver.ConnectionPriority getPriority() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        return priorityOrdinal >= 0 && priorityOrdinal < values.length ? values[priorityOrdinal] : HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < MACHINE_SLOT_COUNT) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 99 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 157));
        }
    }

    private void addBatteryDataSlots() {
        addLongDataSlot(() -> blockEntity.getPower(), () -> power, value -> power = value);
        addLongDataSlot(() -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        addLongDataSlot(() -> blockEntity.getDeltaPerSecond(), () -> delta, value -> delta = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getRedLow();
            }

            @Override
            public void set(int value) {
                redLow = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getRedHigh();
            }

            @Override
            public void set(int value) {
                redHigh = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getBatteryPriority().ordinal();
            }

            @Override
            public void set(int value) {
                priorityOrdinal = value;
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

    private static MachineBatterySocketBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MachineBatterySocketBlockEntity socket) {
            return socket;
        }
        throw new IllegalStateException("Expected machine battery socket block entity at " + pos);
    }

    @FunctionalInterface
    private interface LongGetter {
        long get();
    }

    private interface LongSetter {
        void set(long value);
    }
}
