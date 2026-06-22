package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.WatzReactorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.recipe.WatzFuelRuntime;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WatzReactorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = WatzReactorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final WatzReactorBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData coolantTank;
    private final HbmFluidGuiHelper.TankData hotCoolantTank;
    private final HbmFluidGuiHelper.TankData mudTank;
    private int heat;
    private int fluxDisplayScaled;
    private boolean on;
    private boolean locked;

    public WatzReactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public WatzReactorMenu(int containerId, Inventory playerInventory, WatzReactorBlockEntity blockEntity) {
        super(ModMenuTypes.WATZ_REACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        int index = 0;
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 6; i++) {
                if (i + j > 1 && i + j < 9 && 5 - i + j > 1 && i + 5 - j > 1) {
                    addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), index,
                            17 + i * 18, 8 + j * 18));
                    index++;
                }
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 147, 205);
        coolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCoolantDisplayTank());
        hotCoolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getHotCoolantDisplayTank());
        mudTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getMudDisplayTank());
        addDataSlots();
    }

    public WatzReactorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getCoolantTank() {
        return coolantTank;
    }

    public HbmFluidGuiHelper.TankData getHotCoolantTank() {
        return hotCoolantTank;
    }

    public HbmFluidGuiHelper.TankData getMudTank() {
        return mudTank;
    }

    public int getHeat() {
        return heat;
    }

    public double getFluxDisplay() {
        return fluxDisplayScaled / 1000.0D;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity,
                WatzReactorBlockEntity.USE_DISTANCE_SQR);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (WatzFuelRuntime.isPellet(stack)) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0,
                    WatzReactorBlockEntity.PELLET_SLOT_COUNT)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFluxDisplayScaled,
                value -> fluxDisplayScaled = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.isOn() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                on = value != 0;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.isLocked() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                locked = value != 0;
            }
        });
    }

    private static WatzReactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof WatzReactorBlockEntity watz) {
            return watz;
        }
        throw new IllegalStateException("Expected watz reactor block entity at " + pos);
    }
}
