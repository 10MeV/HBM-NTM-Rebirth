package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CyclotronBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.recipe.CyclotronRecipeRuntime;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CyclotronMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = CyclotronBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final CyclotronBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData waterTank;
    private HbmFluidGuiHelper.TankData spentSteamTank;
    private HbmFluidGuiHelper.TankData amatTank;
    private long power;
    private long maxPower;
    private int progress;
    private int plugs;

    public CyclotronMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public CyclotronMenu(int containerId, Inventory inventory, CyclotronBlockEntity blockEntity) {
        super(ModMenuTypes.CYCLOTRON.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 11, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 11, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 2, 11, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 3, 101, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 4, 101, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 5, 101, 54));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 6, 131, 18));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 7, 131, 36));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 8, 131, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 9, 168, 83));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 10, 60, 81));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 11, 78, 81));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 15, 133, 191);

        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower,
                value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getPlugs, value -> plugs = value);
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.water());
        spentSteamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.spentSteam());
        amatTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.amat());
    }

    public CyclotronBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getWaterTank() {
        return waterTank;
    }

    public HbmFluidGuiHelper.TankData getSpentSteamTank() {
        return spentSteamTank;
    }

    public HbmFluidGuiHelper.TankData getAmatTank() {
        return amatTank;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return progress * maxWidth / CyclotronBlockEntity.DURATION;
    }

    public boolean hasAllPlugs() {
        return (plugs & 0x0F) == 0x0F;
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
        } else if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            if (!moveItemStackTo(stack, CyclotronBlockEntity.SLOT_BATTERY,
                    CyclotronBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ItemMachineUpgrade) {
            if (!moveItemStackTo(stack, CyclotronBlockEntity.SLOT_UPGRADE_0,
                    CyclotronBlockEntity.SLOT_UPGRADE_1 + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (CyclotronRecipeRuntime.isValidParticle(stack)) {
            if (!moveItemStackTo(stack, CyclotronBlockEntity.SLOT_PARTICLE_START,
                    CyclotronBlockEntity.SLOT_PARTICLE_START + 3, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, CyclotronBlockEntity.SLOT_TARGET_START,
                CyclotronBlockEntity.SLOT_TARGET_START + 3, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static CyclotronBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CyclotronBlockEntity cyclotron) {
            return cyclotron;
        }
        throw new IllegalStateException("Expected Cyclotron block entity at " + pos);
    }
}
