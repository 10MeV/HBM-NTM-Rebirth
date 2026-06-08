package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class LiquefactorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 4;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final LiquefactorBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processTime;
    private int usage;
    private HbmFluidGuiHelper.TankData tank;

    public LiquefactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public LiquefactorMenu(int containerId, Inventory playerInventory, LiquefactorBlockEntity blockEntity) {
        super(ModMenuTypes.LIQUEFACTOR.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_INPUT, 35, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_BATTERY, 134, 72));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_UPGRADE_SPEED, 98, 36));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), LiquefactorBlockEntity.SLOT_UPGRADE_POWER, 98, 54));
        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public LiquefactorBlockEntity getBlockEntity() {
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

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public int getUsage() {
        return usage;
    }

    public int getTankFillHeight(int maxHeight) {
        return tank.scaledFill(maxHeight);
    }

    public Component getTankInfo() {
        return tank.info();
    }

    public int getTankTint() {
        return tank.guiTint();
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip() {
        return tank.tooltip();
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START,
                HOTBAR_END,
                0, 3);
    }

    private void addPlayerInventory(Inventory inventory) {
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 122, 180);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProgress();
            }

            @Override
            public void set(int value) {
                progress = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProcessTime();
            }

            @Override
            public void set(int value) {
                processTime = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getUsage();
            }

            @Override
            public void set(int value) {
                usage = value;
            }
        });
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static LiquefactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof LiquefactorBlockEntity liquefactor) {
            return liquefactor;
        }
        throw new IllegalStateException("Expected liquefactor block entity at " + pos);
    }
}
