package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FusionPlasmaForgeMenu extends AbstractContainerMenu {
    private static final double LEGACY_USE_DISTANCE_SQR = 128.0D;
    private static final int MACHINE_SLOT_COUNT = FusionPlasmaForgeBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final FusionPlasmaForgeBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData inputTank;
    private long power;
    private long maxPower;
    private long plasmaEnergy;
    private double progress;
    private int booster;
    private int maxBooster;
    private int connected;

    public FusionPlasmaForgeMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public FusionPlasmaForgeMenu(int containerId, Inventory inventory, FusionPlasmaForgeBlockEntity blockEntity) {
        super(ModMenuTypes.FUSION_PLASMA_FORGE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                FusionPlasmaForgeBlockEntity.SLOT_BATTERY, 152, 82));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                FusionPlasmaForgeBlockEntity.SLOT_BLUEPRINT, 35, 81));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                FusionPlasmaForgeBlockEntity.SLOT_BOOSTER, 98, 116));
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(),
                FusionPlasmaForgeBlockEntity.SLOT_INPUT_START, 8, 18, 3, 4);
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                FusionPlasmaForgeBlockEntity.SLOT_OUTPUT, 116, 36));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 162, 220);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower,
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPlasmaEnergySync,
                () -> plasmaEnergy, value -> plasmaEnergy = value);
        HbmMenuDataSlots.addDouble(this::addDataSlot, blockEntity::getProgress, () -> progress,
                value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBooster, value -> booster = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxBooster, value -> maxBooster = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isConnected() ? 1 : 0,
                value -> connected = value);
    }

    public FusionPlasmaForgeBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getInputTank() { return inputTank; }
    public long getPower() { return power; }
    public long getMaxPower() { return maxPower; }
    public long getPlasmaEnergy() { return plasmaEnergy; }
    public double getProgress() { return progress; }
    public int getBooster() { return booster; }
    public int getMaxBooster() { return maxBooster; }
    public boolean isConnected() { return connected != 0; }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return (int) Math.ceil(getProgress() * maxWidth);
    }

    public int getBoosterHeight(int maxHeight) {
        return maxBooster <= 0 ? 0 : booster * maxHeight / maxBooster;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, LEGACY_USE_DISTANCE_SQR);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            if (!moveItemStackTo(stack, FusionPlasmaForgeBlockEntity.SLOT_BATTERY,
                    FusionPlasmaForgeBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
        } else if (stack.is(ModItems.BLUEPRINTS.get())) {
            if (!moveItemStackTo(stack, FusionPlasmaForgeBlockEntity.SLOT_BLUEPRINT,
                    FusionPlasmaForgeBlockEntity.SLOT_BLUEPRINT + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, FusionPlasmaForgeBlockEntity.SLOT_BOOSTER,
                FusionPlasmaForgeBlockEntity.SLOT_OUTPUT, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static FusionPlasmaForgeBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof FusionPlasmaForgeBlockEntity forge) return forge;
        throw new IllegalStateException("Expected fusion plasma forge at " + pos);
    }
}
