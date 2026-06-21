package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RtgBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.util.RtgPelletRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RtgMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = RtgBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final RtgBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int heat;

    public RtgMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RtgMenu(int containerId, Inventory playerInventory, RtgBlockEntity blockEntity) {
        super(ModMenuTypes.RTG.get(), containerId);
        this.blockEntity = blockEntity;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                        column + row * 5, 16 + column * 18, 18 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 106, 164);
        addDataSlots();
    }

    public RtgBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getHeat() {
        return heat;
    }

    public long getProduction() {
        return heat * 5L;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getHeatBarHeight(int maxHeight) {
        return RtgPelletRuntime.heatMax() <= 0 ? 0 : heat * maxHeight / RtgPelletRuntime.heatMax();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (RtgBlockEntity.isRtgPellet(stack)) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    0, MACHINE_SLOT_COUNT);
        }
        return ItemStack.EMPTY;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower,
                value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
    }

    private static RtgBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RtgBlockEntity rtg) {
            return rtg;
        }
        throw new IllegalStateException("Expected RTG block entity at " + pos);
    }
}
