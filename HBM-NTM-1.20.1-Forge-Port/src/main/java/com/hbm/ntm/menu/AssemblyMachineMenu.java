package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
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

public class AssemblyMachineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 17;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final AssemblyMachineBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int inputFill;
    private int inputCapacity;
    private int outputFill;
    private int outputCapacity;

    public AssemblyMachineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public AssemblyMachineMenu(int containerId, Inventory playerInventory, AssemblyMachineBlockEntity blockEntity) {
        super(ModMenuTypes.ASSEMBLY_MACHINE.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_BATTERY, 152, 81));
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_BLUEPRINT, 35, 126));
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_UPGRADE_START, 152, 108));
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_UPGRADE_END, 170, 108));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                int slot = AssemblyMachineBlockEntity.SLOT_INPUT_START + column + row * 4;
                addSlot(new SlotItemHandler(blockEntity.getItems(), slot, 8 + column * 18, 18 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyMachineBlockEntity.SLOT_OUTPUT, 98, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public AssemblyMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getProgressWidth(int maxWidth) {
        return progress * maxWidth / 10_000;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getInputTankWidth(int maxWidth) {
        return inputCapacity <= 0 ? 0 : inputFill * maxWidth / inputCapacity;
    }

    public int getOutputTankWidth(int maxWidth) {
        return outputCapacity <= 0 ? 0 : outputFill * maxWidth / outputCapacity;
    }

    public String getInputTankInfo() {
        return tankInfo(blockEntity.getInputTank(), inputFill, inputCapacity);
    }

    public String getOutputTankInfo() {
        return tankInfo(blockEntity.getOutputTank(), outputFill, outputCapacity);
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
            } else if (!moveItemStackTo(stack, AssemblyMachineBlockEntity.SLOT_INPUT_START,
                    AssemblyMachineBlockEntity.SLOT_INPUT_END + 1, false)
                    && !moveItemStackTo(stack, AssemblyMachineBlockEntity.SLOT_BATTERY,
                    AssemblyMachineBlockEntity.SLOT_BATTERY + 1, false)) {
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
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 174 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 232));
        }
    }

    private void addDataSlots() {
        addLongDataSlot(() -> blockEntity.getPower(), () -> power, value -> power = value);
        addLongDataSlot(() -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) Math.round(blockEntity.getProgress() * 10_000.0D);
            }

            @Override
            public void set(int value) {
                progress = value;
            }
        });
        addTankDataSlots(blockEntity.getInputTank(), value -> inputFill = value, value -> inputCapacity = value);
        addTankDataSlots(blockEntity.getOutputTank(), value -> outputFill = value, value -> outputCapacity = value);
    }

    private void addTankDataSlots(HbmFluidTank tank, IntSetter fillSetter, IntSetter capacitySetter) {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return tank.getFill();
            }

            @Override
            public void set(int value) {
                fillSetter.set(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return tank.getMaxFill();
            }

            @Override
            public void set(int value) {
                capacitySetter.set(value);
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

    private static String tankInfo(HbmFluidTank tank, int fill, int capacity) {
        return tank.getTankType().getName() + ": " + fill + " / " + capacity + " mB";
    }

    private static AssemblyMachineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AssemblyMachineBlockEntity assembler) {
            return assembler;
        }
        throw new IllegalStateException("Expected assembly machine block entity at " + pos);
    }

    @FunctionalInterface
    private interface LongGetter {
        long get();
    }

    private interface LongSetter {
        void set(long value);
    }

    private interface IntSetter {
        void set(int value);
    }
}
