package com.hbm.ntm.menu;

import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.turret.TurretArtyBlockEntity;
import com.hbm.ntm.turret.TurretBlockEntityBase;
import com.hbm.ntm.turret.TurretFritzBlockEntity;
import com.hbm.ntm.turret.TurretHimarsBlockEntity;
import com.hbm.ntm.turret.TurretHowardBlockEntity;
import com.hbm.ntm.turret.TurretRichardBlockEntity;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TurretMenu extends AbstractContainerMenu {
    private static final int PLAYER_INVENTORY_START = TurretBlockEntityBase.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final TurretBlockEntityBase blockEntity;
    private long power;
    private long maxPower;
    private int flags;
    private int stattrak;
    private int artilleryMode = -1;
    private int loadedState = -1;
    @Nullable
    private HbmFluidGuiHelper.TankData tankData;

    public TurretMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public TurretMenu(int containerId, Inventory playerInventory, TurretBlockEntityBase blockEntity) {
        super(ModMenuTypes.TURRET.get(), containerId);
        this.blockEntity = blockEntity;
        IItemHandler items = blockEntity.getItems();

        addSlot(new ChipSlot(items, TurretBlockEntityBase.SLOT_CHIP, 98, 27));
        HbmInventoryMenuHelper.addSlots(this::addSlot, items, TurretBlockEntityBase.SLOT_AMMO_START, 80, 63, 3, 3);
        addSlot(new BatterySlot(items, TurretBlockEntityBase.SLOT_BATTERY, 152, 99));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public TurretBlockEntityBase getBlockEntity() {
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

    public boolean isOn() {
        return (flags & 1) != 0;
    }

    public boolean targetPlayers() {
        return (flags & 2) != 0;
    }

    public boolean targetFriendly() {
        return (flags & 4) != 0;
    }

    public boolean targetHostile() {
        return (flags & 8) != 0;
    }

    public boolean targetMachines() {
        return (flags & 16) != 0;
    }

    public int getStattrak() {
        return stattrak;
    }

    public boolean hasArtilleryMode() {
        return artilleryMode >= 0;
    }

    public int getArtilleryMode() {
        return artilleryMode;
    }

    public boolean hasLoadedState() {
        return loadedState >= 0;
    }

    public int getLoadedState() {
        return loadedState;
    }

    public boolean hasFluidTank() {
        return tankData != null;
    }

    @Nullable
    public HbmFluidGuiHelper.TankData getTankData() {
        return tankData;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tankData == null ? List.of() : tankData.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < PLAYER_INVENTORY_START) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.TURRET_CHIP.get())) {
                if (!moveItemStackTo(stack, TurretBlockEntityBase.SLOT_CHIP, TurretBlockEntityBase.SLOT_CHIP + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
                if (!moveItemStackTo(stack, TurretBlockEntityBase.SLOT_BATTERY, TurretBlockEntityBase.SLOT_BATTERY + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, TurretBlockEntityBase.SLOT_AMMO_START,
                    TurretBlockEntityBase.SLOT_AMMO_END + 1, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, this::packFlags, value -> flags = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getStattrak, value -> stattrak = value);
        if (blockEntity instanceof TurretFritzBlockEntity fritz) {
            tankData = HbmFluidGuiHelper.watchTank(this::addDataSlot, fritz.getTank());
        }
        if (blockEntity instanceof TurretArtyBlockEntity arty) {
            HbmMenuDataSlots.addInt(this::addDataSlot, arty::getMode, value -> artilleryMode = value);
        } else if (blockEntity instanceof TurretHimarsBlockEntity himars) {
            HbmMenuDataSlots.addInt(this::addDataSlot, himars::getMode, value -> artilleryMode = value);
        }
        if (blockEntity instanceof TurretRichardBlockEntity richard) {
            HbmMenuDataSlots.addInt(this::addDataSlot, richard::getLoaded, value -> loadedState = value);
        } else if (blockEntity instanceof TurretHowardBlockEntity howard) {
            HbmMenuDataSlots.addInt(this::addDataSlot, howard::getLoaded, value -> loadedState = value);
        }
    }

    private int packFlags() {
        int packed = 0;
        if (blockEntity.isOn()) {
            packed |= 1;
        }
        if (blockEntity.targetPlayers()) {
            packed |= 2;
        }
        if (blockEntity.targetFriendly()) {
            packed |= 4;
        }
        if (blockEntity.targetHostile()) {
            packed |= 8;
        }
        if (blockEntity.targetMachines()) {
            packed |= 16;
        }
        return packed;
    }

    private static TurretBlockEntityBase getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof TurretBlockEntityBase turret) {
            return turret;
        }
        throw new IllegalStateException("Expected turret block entity at " + pos);
    }

    private static class ChipSlot extends SlotItemHandler {
        private ChipSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.TURRET_CHIP.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static class BatterySlot extends SlotItemHandler {
        private BatterySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return HbmInventoryMenuHelper.isBatteryLike(stack);
        }
    }
}
