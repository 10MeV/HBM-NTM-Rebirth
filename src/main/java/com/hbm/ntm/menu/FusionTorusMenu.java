package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FusionTorusBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

public class FusionTorusMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = FusionTorusBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final FusionTorusBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData coolantTank;
    private final HbmFluidGuiHelper.TankData hotCoolantTank;
    private final HbmFluidGuiHelper.TankData[] recipeTanks = new HbmFluidGuiHelper.TankData[4];
    private long power;
    private long klystronEnergy;
    private long plasmaEnergy;
    private int progressMilli;
    private int bonusMilli;
    private int fuelMilli;
    private int temperature;

    public FusionTorusMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public FusionTorusMenu(int containerId, Inventory inventory, FusionTorusBlockEntity blockEntity) {
        super(ModMenuTypes.FUSION_TORUS.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FusionTorusBlockEntity.SLOT_BATTERY, 8, 82));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FusionTorusBlockEntity.SLOT_BLUEPRINT, 71, 81));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), FusionTorusBlockEntity.SLOT_OUTPUT, 130, 36));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 35, 162, 220);
        coolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCoolantTank());
        hotCoolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getHotCoolantTank());
        for (int i = 0; i < recipeTanks.length; i++) {
            recipeTanks[i] = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getRecipeTank(i));
        }
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getKlystronEnergy,
                () -> klystronEnergy, value -> klystronEnergy = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPlasmaEnergy,
                () -> plasmaEnergy, value -> plasmaEnergy = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.round(blockEntity.getProgress() * 1000.0D),
                value -> progressMilli = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.round(blockEntity.getBonus() * 1000.0D),
                value -> bonusMilli = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.round(blockEntity.getFuelConsumption() * 1000.0D),
                value -> fuelMilli = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.round(blockEntity.getTemperature()),
                value -> temperature = value);
    }

    public FusionTorusBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getCoolantTank() { return coolantTank; }
    public HbmFluidGuiHelper.TankData getHotCoolantTank() { return hotCoolantTank; }
    public HbmFluidGuiHelper.TankData getRecipeTank(int index) { return recipeTanks[index]; }
    public long getPower() { return power; }
    public long getKlystronEnergy() { return klystronEnergy; }
    public long getPlasmaEnergy() { return plasmaEnergy; }
    public double getProgress() { return progressMilli / 1000.0D; }
    public double getBonus() { return bonusMilli / 1000.0D; }
    public double getFuelConsumption() { return fuelMilli / 1000.0D; }
    public int getTemperature() { return temperature; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 1024.0D);
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
        } else if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            if (!moveItemStackTo(stack, FusionTorusBlockEntity.SLOT_BATTERY, FusionTorusBlockEntity.SLOT_BATTERY + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, FusionTorusBlockEntity.SLOT_BLUEPRINT, FusionTorusBlockEntity.SLOT_BLUEPRINT + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static FusionTorusBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof FusionTorusBlockEntity torus) return torus;
        throw new IllegalStateException("Expected fusion torus at " + pos);
    }
}
