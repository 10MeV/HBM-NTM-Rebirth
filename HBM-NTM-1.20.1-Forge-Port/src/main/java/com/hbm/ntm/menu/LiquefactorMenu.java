package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidTank;
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

public class LiquefactorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 4;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final LiquefactorBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processTime;
    private int usage;
    private int tankFill;
    private int tankCapacity;

    public LiquefactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public LiquefactorMenu(int containerId, Inventory playerInventory, LiquefactorBlockEntity blockEntity) {
        super(ModMenuTypes.LIQUEFACTOR.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_INPUT, 35, 54));
        addSlot(new SlotItemHandler(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_BATTERY, 134, 72));
        addSlot(new SlotItemHandler(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_UPGRADE_SPEED, 98, 36));
        addSlot(new SlotItemHandler(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_UPGRADE_POWER, 98, 54));
        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public LiquefactorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public int getUsage() {
        return usage;
    }

    public int getTankFillHeight(int maxHeight) {
        return tankCapacity <= 0 ? 0 : tankFill * maxHeight / tankCapacity;
    }

    public String getTankInfo() {
        HbmFluidTank tank = blockEntity.getTank();
        return tank.getTankType().getName() + ": " + tankFill + " / " + tankCapacity + " mB";
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
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 122 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 180));
        }
    }

    private void addDataSlots() {
        addLongDataSlot(() -> blockEntity.getPower(), () -> power, value -> power = value);
        addLongDataSlot(() -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProgress();
            }

            @Override
            public void set(int value) {
                progress = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProcessTime();
            }

            @Override
            public void set(int value) {
                processTime = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getUsage();
            }

            @Override
            public void set(int value) {
                usage = value;
            }
        });
        addTankDataSlots(blockEntity.getTank());
    }

    private void addTankDataSlots(HbmFluidTank tank) {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return tank.getFill();
            }

            @Override
            public void set(int value) {
                tankFill = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return tank.getMaxFill();
            }

            @Override
            public void set(int value) {
                tankCapacity = value;
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

    private static LiquefactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof LiquefactorBlockEntity liquefactor) {
            return liquefactor;
        }
        throw new IllegalStateException("Expected liquefactor block entity at " + pos);
    }

    @FunctionalInterface
    private interface LongGetter {
        long get();
    }

    private interface LongSetter {
        void set(long value);
    }
}
