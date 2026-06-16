package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.GasCentBlockEntity;
import com.hbm.ntm.blockentity.GasCentBlockEntity.PseudoFluidType;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GasCentMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = GasCentBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final GasCentBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processingSpeed;
    private int isProgressing;
    private final HbmFluidGuiHelper.TankData tank;
    private final PseudoTankData inputTank;
    private final PseudoTankData outputTank;

    public GasCentMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public GasCentMenu(int containerId, Inventory inventory, GasCentBlockEntity blockEntity) {
        super(ModMenuTypes.GAS_CENT.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(inventory);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
        inputTank = watchPseudoTank(blockEntity.getInputPseudoTank());
        outputTank = watchPseudoTank(blockEntity.getOutputPseudoTank());
        addDataSlots();
    }

    private void addMachineSlots(Inventory inventory) {
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                        column + row * 2, 71 + column * 18, 53 + row * 18));
            }
        }
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), GasCentBlockEntity.SLOT_BATTERY,
                182, 71));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), GasCentBlockEntity.SLOT_IDENTIFIER,
                91, 15));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), GasCentBlockEntity.SLOT_UPGRADE,
                69, 15));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 122, 180);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessingSpeed, value -> processingSpeed = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isProgressing() ? 1 : 0,
                value -> isProgressing = value);
    }

    private PseudoTankData watchPseudoTank(GasCentBlockEntity.PseudoFluidTank source) {
        PseudoTankData data = new PseudoTankData();
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return source.getFill();
            }

            @Override
            public void set(int value) {
                data.fill = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return source.getMaxFill();
            }

            @Override
            public void set(int value) {
                data.capacity = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return source.getTankType().ordinal();
            }

            @Override
            public void set(int value) {
                data.typeOrdinal = value;
            }
        });
        return data;
    }

    public GasCentBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public boolean isProgressing() {
        return isProgressing != 0;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return processingSpeed <= 0 ? 0 : progress * maxWidth / processingSpeed;
    }

    public HbmFluidGuiHelper.TankData getTank() {
        return tank;
    }

    public PseudoTankData getInputTank() {
        return inputTank;
    }

    public PseudoTankData getOutputTank() {
        return outputTank;
    }

    public List<Component> pseudoTankTooltip(PseudoTankData pseudo, boolean input, boolean showHidden) {
        PseudoFluidType type = pseudo.type();
        Component name = Component.translatableWithFallback(type.translationKey(), type.legacyName());
        if (type.requiresHighSpeed()) {
            name = name.copy().withStyle(input && !hasSpeedUpgrade() ? ChatFormatting.DARK_RED : ChatFormatting.GOLD);
        }
        return List.of(
                name,
                Component.literal(pseudo.fill() + " / " + pseudo.capacity() + " mB"),
                getTank().type().getDisplayName());
    }

    public List<Component> realTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    private boolean hasSpeedUpgrade() {
        ItemStack stack = blockEntity.getItems().getStackInSlot(GasCentBlockEntity.SLOT_UPGRADE);
        return stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == UpgradeType.SPEED;
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
                    GasCentBlockEntity.SLOT_BATTERY, GasCentBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    GasCentBlockEntity.SLOT_IDENTIFIER, GasCentBlockEntity.SLOT_IDENTIFIER + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == UpgradeType.SPEED) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    GasCentBlockEntity.SLOT_UPGRADE, GasCentBlockEntity.SLOT_UPGRADE + 1);
        }
        return false;
    }

    private static GasCentBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof GasCentBlockEntity gasCent) {
            return gasCent;
        }
        throw new IllegalStateException("Expected gas centrifuge block entity at " + pos);
    }

    public static final class PseudoTankData {
        private int fill;
        private int capacity;
        private int typeOrdinal;

        public int fill() {
            return fill;
        }

        public int capacity() {
            return capacity;
        }

        public PseudoFluidType type() {
            PseudoFluidType[] values = PseudoFluidType.values();
            return typeOrdinal >= 0 && typeOrdinal < values.length ? values[typeOrdinal] : PseudoFluidType.NONE;
        }

        public boolean isEmpty() {
            return type() == PseudoFluidType.NONE || fill <= 0;
        }

        public int scaledFill(int maxHeight) {
            return capacity <= 0 || fill <= 0 || maxHeight <= 0 ? 0 : fill * maxHeight / capacity;
        }
    }
}
