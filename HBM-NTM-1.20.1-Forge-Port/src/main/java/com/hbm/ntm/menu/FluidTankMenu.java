package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.fluid.FluidType;
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

public class FluidTankMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final FluidTankBlockEntity blockEntity;
    private int mode;
    private int tankFill;
    private int tankCapacity;
    private int exploded;
    private int onFire;

    public FluidTankMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public FluidTankMenu(int containerId, Inventory playerInventory, FluidTankBlockEntity blockEntity) {
        super(ModMenuTypes.FLUID_TANK.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_TYPE_INPUT, 8, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IFluidIdentifierItem;
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_TYPE_OUTPUT, 8, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_LOAD_INPUT, 35, 17));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_LOAD_OUTPUT, 35, 53));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_UNLOAD_INPUT, 125, 17));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_UNLOAD_OUTPUT, 125, 53));
        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public FluidTankBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getMode() {
        return mode;
    }

    public boolean isExploded() {
        return exploded != 0;
    }

    public boolean isOnFire() {
        return onFire != 0;
    }

    public int getTankFillHeight(int maxHeight) {
        return tankCapacity <= 0 ? 0 : tankFill * maxHeight / tankCapacity;
    }

    public net.minecraft.network.chat.Component getTankInfo() {
        HbmFluidTank tank = blockEntity.getTank();
        FluidType type = tank.getTankType();
        return type.getDisplayName().copy()
                .append(": " + tankFill + " / " + tankCapacity + " mB");
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
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    private void addDataSlots() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getMode();
            }

            @Override
            public void set(int value) {
                mode = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getTank().getFill();
            }

            @Override
            public void set(int value) {
                tankFill = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getTank().getMaxFill();
            }

            @Override
            public void set(int value) {
                tankCapacity = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.isExploded() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                exploded = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.isOnFire() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                onFire = value;
            }
        });
    }

    private static FluidTankBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof FluidTankBlockEntity tank) {
            return tank;
        }
        throw new IllegalStateException("Expected fluid tank block entity at " + pos);
    }
}
