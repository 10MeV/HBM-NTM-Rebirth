package com.hbm.ntm.menu;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.blockentity.PABlockEntity;
import com.hbm.ntm.blockentity.PADetectorBlockEntity;
import com.hbm.ntm.blockentity.PADipoleBlockEntity;
import com.hbm.ntm.blockentity.PAQuadrupoleBlockEntity;
import com.hbm.ntm.blockentity.PASourceBlockEntity;
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

public class ParticleAcceleratorMenu extends AbstractContainerMenu {
    private final PABlockEntity blockEntity;
    private final int machineSlotCount;
    private long power;
    private long maxPower;
    private int temperatureTimesTen;
    private int usageLow;
    private int stateOrdinal;
    private int lastSpeed;
    private int dirLower;
    private int dirUpper;
    private int dirRedstone;
    private int threshold;
    private HbmFluidGuiHelper.TankData coldCoolant;
    private HbmFluidGuiHelper.TankData hotCoolant;

    public ParticleAcceleratorMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public ParticleAcceleratorMenu(int containerId, Inventory inventory, PABlockEntity blockEntity) {
        super(ModMenuTypes.PARTICLE_ACCELERATOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.machineSlotCount = blockEntity.getItems().getSlots();
        addMachineSlots(inventory);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 122, 180);
        addDataSlots();
    }

    public PABlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ParticleAcceleratorBlock.Variant getVariant() {
        return blockEntity.getVariant();
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getTemperatureKelvin() {
        return Math.round(temperatureTimesTen / 10.0F);
    }

    public int getUsageLow() {
        return usageLow;
    }

    public int getStateOrdinal() {
        return stateOrdinal;
    }

    public int getLastSpeed() {
        return lastSpeed;
    }

    public int getDirLower() {
        return dirLower;
    }

    public int getDirUpper() {
        return dirUpper;
    }

    public int getDirRedstone() {
        return dirRedstone;
    }

    public int getThreshold() {
        return threshold;
    }

    public HbmFluidGuiHelper.TankData getColdCoolant() {
        return coldCoolant;
    }

    public HbmFluidGuiHelper.TankData getHotCoolant() {
        return hotCoolant;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int playerStart = machineSlotCount;
        int hotbarEnd = playerStart + 36;
        if (index < machineSlotCount) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    machineSlotCount, playerStart, hotbarEnd, 0, machineSlotCount);
        }
        ItemStack stack = slots.get(index).getItem();
        if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, 1)
                    ? finishMove(index, stack) : ItemStack.EMPTY;
        }
        int inputStart = switch (getVariant()) {
            case SOURCE, DETECTOR -> 1;
            case QUADRUPOLE, DIPOLE -> 1;
            default -> 0;
        };
        int inputEnd = switch (getVariant()) {
            case SOURCE, DETECTOR -> 3;
            case QUADRUPOLE, DIPOLE -> 2;
            default -> 1;
        };
        return inputEnd > inputStart && HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, inputStart, inputEnd)
                ? finishMove(index, stack) : ItemStack.EMPTY;
    }

    private ItemStack finishMove(int index, ItemStack stack) {
        Slot slot = slots.get(index);
        ItemStack original = stack.copy();
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addMachineSlots(Inventory inventory) {
        switch (getVariant()) {
            case SOURCE -> {
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 0, 8, 72));
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 1, 62, 18));
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 2, 80, 18));
                addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(), 3, 62, 45));
                addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(), 4, 80, 45));
            }
            case DETECTOR -> {
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 0, 8, 72));
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 1, 62, 18));
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 2, 80, 18));
                addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 3, 62, 45));
                addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 4, 80, 45));
            }
            case RFC -> addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 0, 53, 72));
            case QUADRUPOLE -> {
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 0, 26, 72));
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 1, 71, 36));
            }
            case DIPOLE -> {
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 0, 8, 72));
                addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), 1, 89, 26));
            }
            case BEAMLINE -> {
            }
        }
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> Math.round(blockEntity.getTemperature() * 10.0F),
                value -> temperatureTimesTen = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.min(Integer.MAX_VALUE, blockEntity.getUsage()),
                value -> usageLow = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity instanceof PASourceBlockEntity source ? source.getState().ordinal() : 0,
                value -> stateOrdinal = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity instanceof PASourceBlockEntity source ? source.getLastSpeed() : 0,
                value -> lastSpeed = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity instanceof PADipoleBlockEntity dipole ? dipole.getDirLower() : 0,
                value -> dirLower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity instanceof PADipoleBlockEntity dipole ? dipole.getDirUpper() : 0,
                value -> dirUpper = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity instanceof PADipoleBlockEntity dipole ? dipole.getDirRedstone() : 0,
                value -> dirRedstone = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity instanceof PADipoleBlockEntity dipole ? dipole.getThreshold() : 0,
                value -> threshold = value);
        coldCoolant = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getColdCoolant());
        hotCoolant = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getHotCoolant());
    }

    private static PABlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof PABlockEntity pa) {
            return pa;
        }
        throw new IllegalStateException("Expected PA block entity at " + pos);
    }
}
