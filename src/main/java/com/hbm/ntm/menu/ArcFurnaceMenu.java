package com.hbm.ntm.menu;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
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

public class ArcFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ArcFurnaceBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int HOTBAR_END = PLAYER_INVENTORY_START + 36;

    private final ArcFurnaceBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processTime;
    private int consumption;
    private int lid;
    private int liquidMode;
    private int progressing;
    private int hasMaterial;
    private int liquidAmount;
    private int liquidColor = 0xFFFFFF;
    private int liquidMaterialId = -1;
    private int maxLiquid = 1;

    public ArcFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ArcFurnaceMenu(int containerId, Inventory playerInventory, ArcFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.ARC_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(playerInventory);
        addDataSlots();
    }

    private void addMachineSlots(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), i, 62 + i * 18, 22));
        }
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ArcFurnaceBlockEntity.SLOT_BATTERY, 8, 108));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(),
                ArcFurnaceBlockEntity.SLOT_UPGRADE, 152, 108));
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 5; column++) {
                int slot = ArcFurnaceBlockEntity.SLOT_GRID_START + column + row * 5;
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), slot,
                        44 + column * 18, 54 + row * 18));
            }
        }
        for (int i = 0; i < 5; i++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                    ArcFurnaceBlockEntity.SLOT_QUEUE_START + i, 44 + i * 18, 129));
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 174, 232);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower,
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessTime, value -> processTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::consumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> Math.round(blockEntity.getLid() * 1000.0F),
                value -> lid = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isLiquidMode() ? 1 : 0,
                value -> liquidMode = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isProgressing() ? 1 : 0,
                value -> progressing = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.hasMaterial() ? 1 : 0,
                value -> hasMaterial = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLiquidAmount, value -> liquidAmount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLiquidColor, value -> liquidColor = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLiquidMaterialId,
                value -> liquidMaterialId = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxLiquid, value -> maxLiquid = value);
    }

    public ArcFurnaceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getConsumption() {
        return consumption;
    }

    public boolean isLiquidMode() {
        return liquidMode != 0;
    }

    public boolean isProgressing() {
        return progressing != 0;
    }

    public boolean hasMaterial() {
        return hasMaterial != 0;
    }

    public float getLid() {
        return lid / 1000.0F;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressBarHeight(int maxHeight) {
        return processTime <= 0 ? 0 : progress * maxHeight / processTime;
    }

    public int getLiquidAmount() {
        return liquidAmount;
    }

    public int getLiquidColor() {
        return liquidColor;
    }

    public int getLiquidHeight(int maxHeight) {
        return Math.max(0, Math.min(maxHeight, liquidAmount * maxHeight / Math.max(1, maxLiquid)));
    }

    public MutableComponent liquidTooltip(boolean showMb) {
        if (liquidMaterialId < 0 || liquidAmount <= 0) {
            return Component.literal("Empty");
        }
        NTMMaterial material = Mats.matById.get(liquidMaterialId);
        if (material == null) {
            return Component.literal("Unknown: " + Mats.formatAmount(liquidAmount, showMb));
        }
        return Component.translatable(material.getUnlocalizedName())
                .append(": " + Mats.formatAmount(liquidAmount, showMb));
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
                    ArcFurnaceBlockEntity.SLOT_BATTERY, ArcFurnaceBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof ArcElectrodeItem electrode && !electrode.burnt()) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ArcFurnaceBlockEntity.SLOT_ELECTRODE_0, ArcFurnaceBlockEntity.SLOT_ELECTRODE_2 + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ArcFurnaceBlockEntity.SLOT_UPGRADE, ArcFurnaceBlockEntity.SLOT_UPGRADE + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                ArcFurnaceBlockEntity.SLOT_QUEUE_START, ArcFurnaceBlockEntity.SLOT_QUEUE_END + 1);
    }

    private static ArcFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ArcFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected arc furnace block entity at " + pos);
    }
}
