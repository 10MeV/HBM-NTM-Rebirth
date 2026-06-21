package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.ElectrolyserBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import java.util.List;
import java.util.function.BooleanSupplier;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ElectrolyserMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ElectrolyserBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ElectrolyserBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progressFluid;
    private int processFluidTime;
    private int progressOre;
    private int processOreTime;
    private int usageFluid;
    private int usageOre;
    private int mode;
    private int leftMaterialId = -1;
    private int leftAmount;
    private int leftColor = 0xFFFFFF;
    private int rightMaterialId = -1;
    private int rightAmount;
    private int rightColor = 0xFFFFFF;
    private int maxMaterial = 1;
    private final List<HbmFluidGuiHelper.TankData> tanks;

    public ElectrolyserMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ElectrolyserMenu(int containerId, Inventory playerInventory, ElectrolyserBlockEntity blockEntity) {
        super(ModMenuTypes.ELECTROLYSER.get(), containerId);
        this.blockEntity = blockEntity;
        addFluidSlots();
        addMetalSlots();
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
        tanks = HbmFluidGuiHelper.watchTanks(this::addDataSlot, blockEntity.getAllTanks());
        addDataSlots();
    }

    private void addFluidSlots() {
        addSlot(activeLegacyMachineSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_BATTERY, 186, 109, () -> true));
        addSlot(activeUpgradeSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_UPGRADE_1, 186, 140, () -> true));
        addSlot(activeUpgradeSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_UPGRADE_2, 186, 158, () -> true));
        addSlot(activeLegacyMachineSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_FLUID_ID_INPUT, 6, 18, () -> !isMetalMode()));
        addSlot(activeOutputSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_FLUID_ID_OUTPUT, 6, 54));
        addSlot(activeLegacyMachineSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_INPUT_CONTAINER, 24, 18, () -> !isMetalMode()));
        addSlot(activeOutputSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_INPUT_CONTAINER_OUT, 24, 54));
        addSlot(activeLegacyMachineSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_OUTPUT1_CONTAINER, 78, 18, () -> !isMetalMode()));
        addSlot(activeOutputSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_OUTPUT1_CONTAINER_OUT, 78, 54));
        addSlot(activeLegacyMachineSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_OUTPUT2_CONTAINER, 134, 18, () -> !isMetalMode()));
        addSlot(activeOutputSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_OUTPUT2_CONTAINER_OUT, 134, 54));
        for (int i = 0; i < 3; i++) {
            addSlot(activeOutputSlot(blockEntity.getItems(),
                    ElectrolyserBlockEntity.SLOT_FLUID_BYPRODUCT_START + i, 154, 18 + 18 * i));
        }
    }

    private void addMetalSlots() {
        addSlot(activeLegacyMachineSlot(blockEntity.getItems(),
                ElectrolyserBlockEntity.SLOT_METAL_INPUT, 10, 22, this::isMetalMode));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 2; column++) {
                addSlot(activeOutputSlot(blockEntity.getItems(),
                        ElectrolyserBlockEntity.SLOT_METAL_OUTPUT_START + row * 2 + column,
                        136 + column * 18, 18 + row * 18, this::isMetalMode));
            }
        }
    }

    private SlotItemHandler activeLegacyMachineSlot(IItemHandler items, int slot, int x, int y,
            BooleanSupplier active) {
        return new ActiveSlot(items, slot, x, y, active) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return items.isItemValid(slot, stack);
            }
        };
    }

    private SlotItemHandler activeUpgradeSlot(IItemHandler items, int slot, int x, int y, BooleanSupplier active) {
        return new ActiveSlot(items, slot, x, y, active) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
        };
    }

    private SlotItemHandler activeOutputSlot(IItemHandler items, int slot, int x, int y) {
        return activeOutputSlot(items, slot, x, y, () -> !isMetalMode());
    }

    private SlotItemHandler activeOutputSlot(IItemHandler items, int slot, int x, int y, BooleanSupplier active) {
        return new ActiveSlot(items, slot, x, y, active) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgressFluid, value -> progressFluid = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessFluidTime, value -> processFluidTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgressOre, value -> progressOre = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessOreTime, value -> processOreTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getUsageFluid, value -> usageFluid = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getUsageOre, value -> usageOre = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastSelectedGui, value -> mode = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLeftMaterialId, value -> leftMaterialId = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLeftAmount, value -> leftAmount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLeftColor, value -> leftColor = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRightMaterialId, value -> rightMaterialId = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRightAmount, value -> rightAmount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRightColor, value -> rightColor = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxMaterial, value -> maxMaterial = value);
    }

    public ElectrolyserBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isMetalMode() {
        return mode == ElectrolyserBlockEntity.MODE_METAL;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getUsageFluid() {
        return usageFluid;
    }

    public int getUsageOre() {
        return usageOre;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getFluidProgressHeight(int maxHeight) {
        return processFluidTime <= 0 ? 0 : progressFluid * maxHeight / processFluidTime;
    }

    public int getOreProgressHeight(int maxHeight) {
        return processOreTime <= 0 ? 0 : progressOre * maxHeight / processOreTime;
    }

    public int getLeftMaterialId() {
        return leftMaterialId;
    }

    public int getLeftAmount() {
        return leftAmount;
    }

    public int getLeftColor() {
        return leftColor;
    }

    public int getRightMaterialId() {
        return rightMaterialId;
    }

    public int getRightAmount() {
        return rightAmount;
    }

    public int getRightColor() {
        return rightColor;
    }

    public int getMaxMaterial() {
        return Math.max(1, maxMaterial);
    }

    public int getMaterialHeight(int amount, int maxHeight) {
        return Math.max(0, Math.min(maxHeight, amount * maxHeight / getMaxMaterial()));
    }

    public MutableComponent materialTooltip(int materialId, int amount, boolean showMb) {
        if (materialId < 0 || amount <= 0) {
            return Component.literal("Empty");
        }
        NTMMaterial material = Mats.matById.get(materialId);
        if (material == null) {
            return Component.literal("Unknown: " + Mats.formatAmount(amount, showMb));
        }
        return Component.translatable(material.getUnlocalizedName())
                .append(": " + Mats.formatAmount(amount, showMb));
    }

    public HbmFluidGuiHelper.TankData tank(int index) {
        return index >= 0 && index < tanks.size() ? tanks.get(index) : null;
    }

    public List<Component> tankTooltip(int index, boolean showHidden) {
        HbmFluidGuiHelper.TankData tank = tank(index);
        return tank == null ? List.of() : tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
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
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ElectrolyserBlockEntity.SLOT_BATTERY, ElectrolyserBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ElectrolyserBlockEntity.SLOT_UPGRADE_1, ElectrolyserBlockEntity.SLOT_UPGRADE_2 + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ElectrolyserBlockEntity.SLOT_FLUID_ID_INPUT,
                    ElectrolyserBlockEntity.SLOT_FLUID_ID_INPUT + 1);
        }
        if (blockEntity.getItems().isItemValid(ElectrolyserBlockEntity.SLOT_METAL_INPUT, stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ElectrolyserBlockEntity.SLOT_METAL_INPUT,
                    ElectrolyserBlockEntity.SLOT_METAL_INPUT + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                ElectrolyserBlockEntity.SLOT_INPUT_CONTAINER,
                ElectrolyserBlockEntity.SLOT_INPUT_CONTAINER + 1);
    }

    private static ElectrolyserBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ElectrolyserBlockEntity electrolyser) {
            return electrolyser;
        }
        throw new IllegalStateException("Expected electrolyser block entity at " + pos);
    }

    private static class ActiveSlot extends SlotItemHandler {
        private final BooleanSupplier active;

        private ActiveSlot(IItemHandler items, int slot, int x, int y, BooleanSupplier active) {
            super(items, slot, x, y);
            this.active = active;
        }

        @Override
        public int getMaxStackSize() {
            return Math.max(super.getMaxStackSize(), hasItem() ? getItem().getCount() : 1);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return Math.max(super.getMaxStackSize(stack), hasItem() ? getItem().getCount() : 1);
        }

        @Override
        public boolean isActive() {
            return active.getAsBoolean();
        }
    }
}
