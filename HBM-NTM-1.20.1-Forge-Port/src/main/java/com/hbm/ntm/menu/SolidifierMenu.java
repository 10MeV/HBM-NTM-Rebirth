package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SolidifierMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SolidifierBlockEntity.ITEM_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SolidifierBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processTime;
    private int usage;
    private HbmFluidGuiHelper.TankData tank;

    public SolidifierMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SolidifierMenu(int containerId, Inventory playerInventory, SolidifierBlockEntity blockEntity) {
        super(ModMenuTypes.SOLIDIFIER.get(), containerId);
        this.blockEntity = blockEntity;
        ItemStackHandler items = blockEntity.getItems();
        addSlot(HbmInventoryMenuHelper.outputSlot(items, SolidifierBlockEntity.SLOT_OUTPUT, 71, 45));
        addSlot(new SlotItemHandler(items, SolidifierBlockEntity.SLOT_BATTERY, 134, 72) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return HbmInventoryMenuHelper.isBatteryLike(stack);
            }
        });
        addSlot(upgradeSlot(items, SolidifierBlockEntity.SLOT_UPGRADE_SPEED, 98, 36));
        addSlot(upgradeSlot(items, SolidifierBlockEntity.SLOT_UPGRADE_POWER, 98, 54));
        addSlot(new SlotItemHandler(items, SolidifierBlockEntity.SLOT_IDENTIFIER, 71, 72) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IFluidIdentifierItem;
            }
        });
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
        addDataSlots();
    }

    public SolidifierBlockEntity getBlockEntity() {
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

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                SolidifierBlockEntity.SLOT_BATTERY, SolidifierBlockEntity.SLOT_BATTERY + 1,
                SolidifierBlockEntity.SLOT_UPGRADE_SPEED, SolidifierBlockEntity.SLOT_UPGRADE_POWER + 1,
                SolidifierBlockEntity.SLOT_IDENTIFIER, SolidifierBlockEntity.SLOT_IDENTIFIER + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
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

    private static SlotItemHandler upgradeSlot(ItemStackHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
        };
    }

    private static SolidifierBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SolidifierBlockEntity solidifier) {
            return solidifier;
        }
        throw new IllegalStateException("Expected solidifier block entity at " + pos);
    }
}
