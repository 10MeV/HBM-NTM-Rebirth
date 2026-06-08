package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
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
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

public class AssemblyFactoryMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 60;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final AssemblyFactoryBlockEntity blockEntity;
    private final int[] progress = new int[4];
    private final HbmFluidGuiHelper.TankData[] inputTanks = new HbmFluidGuiHelper.TankData[4];
    private final HbmFluidGuiHelper.TankData[] outputTanks = new HbmFluidGuiHelper.TankData[4];
    private HbmFluidGuiHelper.TankData waterTank;
    private HbmFluidGuiHelper.TankData spentSteamTank;
    private long power;
    private long maxPower;
    private int canCool;

    public AssemblyFactoryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public AssemblyFactoryMenu(int containerId, Inventory playerInventory, AssemblyFactoryBlockEntity blockEntity) {
        super(ModMenuTypes.ASSEMBLY_FACTORY.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyFactoryBlockEntity.SLOT_BATTERY, 234, 112));
        HbmInventoryMenuHelper.addUpgradeSlots(this::addSlot, blockEntity.getItems(),
                AssemblyFactoryBlockEntity.SLOT_UPGRADE_START, 214, 149, 1, 3);
        for (int module = 0; module < 4; module++) {
            int ox = (module % 2) * 109;
            int oy = (module / 2) * 56;
            addSlot(new SlotItemHandler(blockEntity.getItems(), AssemblyFactoryBlockEntity.blueprintSlot(module),
                    25 + ox, 54 + oy));
            for (int row = 0; row < 2; row++) {
                for (int column = 0; column < 6; column++) {
                    int slot = AssemblyFactoryBlockEntity.inputStart(module) + column + row * 6;
                    addSlot(new SlotItemHandler(blockEntity.getItems(), slot, 7 + ox + column * 16, 20 + oy + row * 16));
                }
            }
            addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), AssemblyFactoryBlockEntity.outputSlot(module),
                    87 + ox, 54 + oy));
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 33, 158, 216);
        addDataSlots();
    }

    public AssemblyFactoryBlockEntity getBlockEntity() {
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

    public int getProgressWidth(int module, int maxWidth) {
        return progress[module] * maxWidth / 10_000;
    }

    public boolean canCool() {
        return canCool != 0;
    }

    public HbmFluidGuiHelper.TankData getInputTankData(int module) {
        return inputTanks[module];
    }

    public HbmFluidGuiHelper.TankData getOutputTankData(int module) {
        return outputTanks[module];
    }

    public HbmFluidGuiHelper.TankData getWaterTankData() {
        return waterTank;
    }

    public HbmFluidGuiHelper.TankData getSpentSteamTankData() {
        return spentSteamTank;
    }

    public List<Component> getInputTankTooltip(int module, boolean showHidden) {
        return inputTanks[module].tooltip(showHidden);
    }

    public List<Component> getOutputTankTooltip(int module, boolean showHidden) {
        return outputTanks[module].tooltip(showHidden);
    }

    public List<Component> getWaterTankTooltip(boolean showHidden) {
        return waterTank.tooltip(showHidden);
    }

    public List<Component> getSpentSteamTankTooltip(boolean showHidden) {
        return spentSteamTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!movePlayerStackToMachine(stack)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    AssemblyFactoryBlockEntity.SLOT_BATTERY, AssemblyFactoryBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof ItemBlueprints) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    AssemblyFactoryBlockEntity.blueprintSlot(0), AssemblyFactoryBlockEntity.blueprintSlot(0) + 1,
                    AssemblyFactoryBlockEntity.blueprintSlot(1), AssemblyFactoryBlockEntity.blueprintSlot(1) + 1,
                    AssemblyFactoryBlockEntity.blueprintSlot(2), AssemblyFactoryBlockEntity.blueprintSlot(2) + 1,
                    AssemblyFactoryBlockEntity.blueprintSlot(3), AssemblyFactoryBlockEntity.blueprintSlot(3) + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    AssemblyFactoryBlockEntity.SLOT_UPGRADE_START, AssemblyFactoryBlockEntity.SLOT_UPGRADE_END + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                AssemblyFactoryBlockEntity.inputStart(0), AssemblyFactoryBlockEntity.inputEnd(0) + 1,
                AssemblyFactoryBlockEntity.inputStart(1), AssemblyFactoryBlockEntity.inputEnd(1) + 1,
                AssemblyFactoryBlockEntity.inputStart(2), AssemblyFactoryBlockEntity.inputEnd(2) + 1,
                AssemblyFactoryBlockEntity.inputStart(3), AssemblyFactoryBlockEntity.inputEnd(3) + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        for (int i = 0; i < 4; i++) {
            int module = i;
            HbmMenuDataSlots.addProgress(this::addDataSlot, () -> blockEntity.getProgress(module), value -> progress[module] = value);
            inputTanks[i] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank(i));
            outputTanks[i] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank(i));
        }
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getWaterTank());
        spentSteamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSpentSteamTank());
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override
            public int get() {
                return blockEntity.getWaterTank().getFill() >= 100
                        && blockEntity.getSpentSteamTank().getFill() <= blockEntity.getSpentSteamTank().getMaxFill() - 100 ? 1 : 0;
            }

            @Override
            public void set(int value) {
                canCool = value;
            }
        });
    }

    private static AssemblyFactoryBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AssemblyFactoryBlockEntity factory) {
            return factory;
        }
        throw new IllegalStateException("Expected assembly factory block entity at " + pos);
    }
}
