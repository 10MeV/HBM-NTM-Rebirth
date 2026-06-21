package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class LaunchPadMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = LaunchPadBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final LaunchPadBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int state;
    private int delay;
    private int fuelState;
    private int oxidizerState;
    private HbmFluidGuiHelper.TankData fuelTank;
    private HbmFluidGuiHelper.TankData oxidizerTank;

    public LaunchPadMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public LaunchPadMenu(int containerId, Inventory playerInventory, LaunchPadBlockEntity blockEntity) {
        super(ModMenuTypes.LAUNCH_PAD.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), LaunchPadBlockEntity.SLOT_MISSILE, 26, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return blockEntity.isMissileValid(stack);
            }
        });
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(),
                LaunchPadBlockEntity.SLOT_DESIGNATOR, 26, 72));
        addSlot(new SlotItemHandler(blockEntity.getItems(), LaunchPadBlockEntity.SLOT_BATTERY, 107, 90) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
            }
        });
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(),
                LaunchPadBlockEntity.SLOT_FUEL_INPUT, 125, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                LaunchPadBlockEntity.SLOT_FUEL_OUTPUT, 125, 108));
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(),
                LaunchPadBlockEntity.SLOT_OXIDIZER_INPUT, 143, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                LaunchPadBlockEntity.SLOT_OXIDIZER_OUTPUT, 143, 108));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 154, 212);
        addDataSlots();
    }

    public LaunchPadBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int height) {
        return maxPower <= 0L ? 0 : (int) (power * height / maxPower);
    }

    public int getState() {
        return state;
    }

    public int getDelay() {
        return delay;
    }

    public int getFuelState() {
        return fuelState;
    }

    public int getOxidizerState() {
        return oxidizerState;
    }

    public HbmFluidGuiHelper.TankData getFuelTankData() {
        return fuelTank;
    }

    public HbmFluidGuiHelper.TankData getOxidizerTankData() {
        return oxidizerTank;
    }

    public List<Component> getFuelTankTooltip(boolean showHidden) {
        return fuelTank.tooltip(showHidden);
    }

    public List<Component> getOxidizerTankTooltip(boolean showHidden) {
        return oxidizerTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                LaunchPadBlockEntity.SLOT_MISSILE, LaunchPadBlockEntity.SLOT_MISSILE + 1,
                LaunchPadBlockEntity.SLOT_DESIGNATOR, LaunchPadBlockEntity.SLOT_DESIGNATOR + 1,
                LaunchPadBlockEntity.SLOT_BATTERY, LaunchPadBlockEntity.SLOT_BATTERY + 1,
                LaunchPadBlockEntity.SLOT_FUEL_INPUT, LaunchPadBlockEntity.SLOT_FUEL_INPUT + 1,
                LaunchPadBlockEntity.SLOT_OXIDIZER_INPUT, LaunchPadBlockEntity.SLOT_OXIDIZER_INPUT + 1);
        return moved;
    }

    public ItemStack getMissileStack() {
        return getSlot(LaunchPadBlockEntity.SLOT_MISSILE).getItem();
    }

    public boolean hasMissileFuelCapacity() {
        ItemStack stack = getMissileStack();
        return stack.getItem() instanceof MissileItem missile && missile.fuelCap() > 0;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getStoredPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxStoredPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getState, value -> state = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getDelay, value -> delay = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFuelState, value -> fuelState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOxidizerState, value -> oxidizerState = value);
        fuelTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.fuelTank());
        oxidizerTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.oxidizerTank());
    }

    private static LaunchPadBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof LaunchPadBlockEntity launchPad) {
            return launchPad;
        }
        throw new IllegalStateException("Expected launch pad block entity at " + pos);
    }
}
